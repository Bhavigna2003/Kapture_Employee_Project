package com.kapture.employee.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kapture.employee.dao.EmployeeDao;
import com.kapture.employee.dto.EmployeeDto;
import com.kapture.employee.entity.Employee;
import com.kapture.employee.mapper.EmployeeMapper;
import com.kapture.employee.util.EmployeeUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.RequestContext;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import static com.kapture.employee.constants.EmployeeConstants.*;
import static com.kapture.employee.service.RedisService.*;
import static com.kapture.employee.util.EmployeeUtil.*;
import static com.kapture.employee.validation.EmployeeValidation.*;


@Service
@Transactional
public class EmployeeService {

    @Autowired
    private ObjectMapper mapper;

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private EmployeeDao employeeRepository;

    private static final String TOPIC = "Employee-Details";

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    public ResponseEntity<ObjectNode> addEmployee(EmployeeDto employeeDto) {
        HttpStatus httpStatus = HttpStatus.OK;
        ObjectNode responseObject = mapper.createObjectNode();
        try {
            Employee employee = createEmployeeFromDto(employeeDto);
            employeeRepository.saveEmployee(employee);
            kafkaTemplate.send(TOPIC, employee);
            logger.info("Employee added successfully with ID : {}", employee.getId());
            saveEmployee(employee);
            responseObject.put("message", EMPLOYEE_CREATED_SUCCESSFULLY);
            responseObject.put("status", true);
            responseObject.set("details", mapper.valueToTree(employee));

        } catch (Exception e) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            logger.error("Error Occured", e);
            e.printStackTrace();
        }

        return new ResponseEntity<>(responseObject, httpStatus);
    }

    public ResponseEntity<?> uploadBulkEmployees(MultipartFile file, HttpServletRequest request) {
        XSSFWorkbook workbook = null;
        ObjectNode responseObject = mapper.createObjectNode();
        HttpStatus httpStatus = HttpStatus.OK;
        List<Employee> employeesToSave = new ArrayList<>();

        try (InputStream stream = file.getInputStream()) {
            workbook = new XSSFWorkbook(stream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            if (sheet != null) {
                int lastRowNum = sheet.getLastRowNum();
                for (int rowIdx = 1; rowIdx <= 5; rowIdx++) {
                    XSSFRow row = sheet.getRow(rowIdx);
                    if (row == null) {
                        continue;
                    }
                    String name = getCellText(row.getCell(1));
                    int clientId = 1;
                    // Validate clientId
                    if (!isValidClientId(clientId)) {
                        setCellValue(row, 2, "Client ID is not valid");
                        continue;
                    }
                    // Validate name
                    if (!isNotNullNotEmpty(name)) {
                        setCellValue(row, 2, "Name is mandatory field");
                        continue;
                    }

                    if(!isValidClientId(clientId) && !isNotNullNotEmpty(name)){
                        sheet=null;
                    }
                    // Create Employee object and add to list
                    Employee employee = new Employee(clientId, name);
                    employeesToSave.add(employee);
                    // Set status cell
                    setCellValue(row, 3, "DATA_ADDED_SUCCESSFULLY");
                }
            }
            // Save employees using repository or DAO method
            employeeRepository.saveBulkEmployees(employeesToSave);
            responseObject.put("message", "Bulk employees saved successfully");
            responseObject.put("status", true);
            responseObject.set("details", mapper.valueToTree(employeesToSave));
        } catch (Exception e) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            logger.error("Error in uploadBulkEmployees() method", e);
            responseObject.put("message", "Internal server error occurred");
            responseObject.put("status", false);
        }

        return new ResponseEntity<>(responseObject, httpStatus);
    }

    public static String getCellText(Cell cell) {
        String text = "";
        try {
            if (cell != null) {
                if (cell.getCellType() == CellType.STRING) {
                    text = cell.getStringCellValue();
                }
            }
        } catch (Exception e) {
            logger.error("Error in getCellText() method !!", e);
        }
        return text;
    }

    public static int getCellInt(Cell cell) {
        int num=0;
        try {
            if (cell != null) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    num = (int) cell.getNumericCellValue();
                }
            }
        } catch (Exception e) {
            logger.error("Error in getCellText() method !!", e);
        }
        return num;
    }

    public void setCellValue(Row row, int cellIndex, String value) {
        Cell cell = row.createCell(cellIndex);
        cell.setCellValue(value);
    }

    public ResponseEntity<Object> getAllEmployeesByClientId(Long clientId, int page, int size) {
        Employee cachedEmployee = getCachedEmployee(clientId, page, size);
        if (cachedEmployee != null) {
            return ResponseEntity.ok(cachedEmployee);
        }
        List<Employee> employees = getEmployeesByClientId(clientId, page, size);
        if (employees == null) {
            ObjectNode responseObject = createResponseObject(HttpStatus.OK.value(), EMPLOYEE_NOT_FOUND_BY_EMP_CODE);
            responseObject.put("status", false);
            responseObject.put("message", "Employees Not found");
            return ResponseEntity.ok(responseObject);
        }
        ObjectNode responseObject = createResponseObject(HttpStatus.OK.value(), EMPLOYEE_FETCHED_SUCCESSFULLY);
        responseObject.set("data", mapper.valueToTree(employees));
        return ResponseEntity.ok(responseObject);
    }

    private List<Employee> getEmployeesByClientId(Long clientId, int page, int size) {
        return employeeRepository.findByClientId(clientId, page, size);
    }

    public static Employee createEmployeeFromDto(EmployeeDto employeeDto) {
        Employee employee = new Employee(employeeDto.getClientId(), employeeDto.getName());
        if (employeeDto.getDesignation() != null) {
            employee.setDesignation(employeeDto.getDesignation().toUpperCase());
        }
        return employee;
    }


    public ResponseEntity<?> getAllEmployeesByDesignation(String designation, int page, int size) {
        Employee cachedResponse = getCachedDesignationResponse(designation, page, size);
        if (cachedResponse != null) {
            return ResponseEntity.ok(cachedResponse);
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Employee> employees = employeeRepository.findByDesignation(designation, pageRequest);
        ResponseEntity<?> responseEntity = ResponseEntity.ok(employees);
        return responseEntity;
    }

    public List<EmployeeDto> getAllEmployees() {
        List<EmployeeDto> cachedEmployees = getCachedAllEmployees();
        if (cachedEmployees != null) {
            return cachedEmployees;
        }
        List<Employee> employees = employeeRepository.findAllEmployees();
        List<EmployeeDto> employeeDtos = employees.stream()
                .map(EmployeeMapper::mapToEmployeeDto)
                .collect(Collectors.toList());
        return employeeDtos;
    }

    public ResponseEntity<?> getEmployeeById(int id) {
        Employee cachedResponse = getCachedEmployeeById(id);
        if (cachedResponse != null) {
            return ResponseEntity.ok(cachedResponse);
        }
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (optionalEmployee == null) {
            return handleEmployeeNotFound(id);
        }
        EmployeeDto employeeDto = EmployeeMapper.mapToEmployeeDto(optionalEmployee.get());
        Employee employee = EmployeeMapper.mapToEmployee(employeeDto);
        ResponseEntity<?> responseEntity = ResponseEntity.ok(employeeDto);
        return responseEntity;
    }

    public ResponseEntity<?> getEmployeeByEmpCode(String empCode) {
        Employee cachedResponse = getCachedEmployeeByEmpCode(empCode);
        if (cachedResponse != null) {
            return ResponseEntity.ok(cachedResponse);
        }
        Optional<Employee> optionalEmployee = employeeRepository.findByEmpCode(empCode);
        if (optionalEmployee.isEmpty()) {
            ObjectNode responseObject = mapper.createObjectNode();
            responseObject.put("status", HttpStatus.NOT_FOUND.value());
            responseObject.put("message", EMPLOYEE_NOT_FOUND_BY_EMP_CODE + empCode);
            logger.error(EMPLOYEE_NOT_FOUND_BY_EMP_CODE + empCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseObject);
        }
        EmployeeDto employeeDto = EmployeeMapper.mapToEmployeeDto(optionalEmployee.get());
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(optionalEmployee.get());
        return ResponseEntity.ok(employeeDto);
    }


    public ResponseEntity<ObjectNode> updateEmployeeById(int id, EmployeeDto request) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            checkupdateEmployeeFields(employee, request);
            employeeRepository.updateEmployee(employee);
            kafkaTemplate.send(TOPIC, employee);
            logger.info(EMPLOYEE_UPDATED);
            saveEmployee(employee);
            return ResponseEntity.ok().body(EmployeeUtil.createSuccessResponse(EMPLOYEE_UPDATED));
        } else {
            logger.error(EMPLOYEE_NOT_FOUND_BY_ID);
            return ResponseEntity.badRequest().body(EmployeeUtil.createErrorResponse(EMPLOYEE_NOT_FOUND_BY_ID + id));
        }
    }


    public ResponseEntity<ObjectNode> updateEmployeeByEmpCode(String empCode, EmployeeDto request) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmpCode(empCode);
        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            checkupdateEmployeeFields(employee, request);
            employeeRepository.updateEmployee(employee);
            kafkaTemplate.send(TOPIC, employee);
            saveEmployee(employee);
            logger.info(EMPLOYEE_UPDATED);
            return ResponseEntity.ok().body(EmployeeUtil.createSuccessResponse(EMPLOYEE_UPDATED));
        } else {
            logger.error(EMPLOYEE_NOT_FOUND_BY_EMP_CODE);
            return ResponseEntity.badRequest().body(EmployeeUtil.createErrorResponse(EMPLOYEE_NOT_FOUND_BY_EMP_CODE + empCode));
        }
    }


    public ResponseEntity<ObjectNode> deleteEmployeeById(int id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
        if (optionalEmployee.isPresent()) {
            employeeRepository.deleteEmployee(optionalEmployee.get());
            deleteEmployeeCacheById(id);
            logger.info(EMPLOYEE_DELETED);
            kafkaTemplate.send(TOPIC, optionalEmployee.get());
            return ResponseEntity.ok().body(EmployeeUtil.createSuccessResponse(EMPLOYEE_DELETED));
        } else {
            logger.error(EMPLOYEE_NOT_FOUND_BY_ID);
            return ResponseEntity.badRequest().body(EmployeeUtil.createErrorResponse(EMPLOYEE_NOT_FOUND_BY_ID + id));
        }
    }

    public ResponseEntity<ObjectNode> deleteEmployeeByEmpCode(String empCode) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmpCode(empCode);
        if (optionalEmployee.isPresent()) {
            employeeRepository.deleteEmployee(optionalEmployee.get());
            deleteEmployeeCacheByEmpCode(empCode);
            logger.info(EMPLOYEE_DELETED);
            kafkaTemplate.send(TOPIC, optionalEmployee.get());
            return ResponseEntity.ok().body(EmployeeUtil.createSuccessResponse(EMPLOYEE_DELETED));
        } else {
            logger.error(EMPLOYEE_NOT_FOUND_BY_EMP_CODE);
            return ResponseEntity.badRequest().body(EmployeeUtil.createErrorResponse(EMPLOYEE_NOT_FOUND_BY_EMP_CODE + empCode));
        }
    }


}