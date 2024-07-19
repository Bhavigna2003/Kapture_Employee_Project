package com.kapture.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kapture.employee.dto.EmployeeDto;
import com.kapture.employee.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.kapture.employee.constants.EmployeeConstants.PROVIDE_VALID_DETAILS;
import static com.kapture.employee.util.EmployeeUtil.createErrorResponse;
import static com.kapture.employee.util.EmployeeUtil.createInvalidDesignationResponse;
import static com.kapture.employee.validation.EmployeeValidation.isValidDesignation;
import static com.kapture.employee.validation.EmployeeValidation.isValidEmployeeDetails;

@RestController
@RequestMapping("api/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper mapper;

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @PostMapping
    public ResponseEntity<ObjectNode> addEmployee(@RequestBody EmployeeDto employeeDto) {
        logger.info("Received Request to add an Employee: {} ", employeeDto);
        if (!isValidEmployeeDetails(employeeDto)) {
            return ResponseEntity.badRequest().body(createErrorResponse(PROVIDE_VALID_DETAILS));
        }
        return employeeService.addEmployee(employeeDto);
    }



    @PostMapping("/addBulkEmployees")
    public ResponseEntity<?> addBulkEmployees(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return employeeService.uploadBulkEmployees(file, request);
    }



    @GetMapping("/getEmployees/ByClientId/{clientId}")
    public ResponseEntity<Object> getAllEmployeesByClientId(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Received Request to Fetch all Employees whose client Id :  {}", clientId);
        return employeeService.getAllEmployeesByClientId(clientId, page, size);
    }


    @GetMapping("/getEmployees/ByDesignation/{designation}")
    public ResponseEntity<?> getAllEmployeesByDesignation(
            @PathVariable String designation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Received Request to Fetch all Employees whose designation : {}", designation);
        if (!isValidDesignation(designation)) {
            return createInvalidDesignationResponse();
        }
        return employeeService.getAllEmployeesByDesignation(designation, page, size);
    }

    @GetMapping("/getAllEmployees")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.getAllEmployees();
        logger.info("Received Request to Fetch all Employees ");
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @GetMapping("/getEmployee/ById/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable int id) {
        ResponseEntity<?> responseEntity = employeeService.getEmployeeById(id);
        logger.info("Received Request to Fetch an Employee whose Id : {}", id);
        return responseEntity;
    }

    @GetMapping("/getEmployee/ByEmpCode/{empCode}")
    public ResponseEntity<?> getEmployeeByEmpCode(@PathVariable String empCode) {
        logger.info("Received Request to Fetch an Employee whose Empcode : {}", empCode);
        ResponseEntity<?> responseEntity = employeeService.getEmployeeByEmpCode(empCode);
        return responseEntity;
    }

    @PutMapping("/update/byId/{id}")
    public ResponseEntity<ObjectNode> updateEmployeeById(
            @PathVariable int id,
            @RequestBody EmployeeDto employeeDto) {
        logger.info("Received Request to update  Employee whose Id : {}", employeeDto);
        return employeeService.updateEmployeeById(id, employeeDto);
    }

    @PutMapping("/update/byEmpCode/{empCode}")
    public ResponseEntity<ObjectNode> updateEmployeeByEmpCode(
            @PathVariable String empCode,
            @RequestBody EmployeeDto employeeDto) {
        logger.info("Received Request to update Employee whose Empcode : {}", empCode);
        return employeeService.updateEmployeeByEmpCode(empCode, employeeDto);
    }

    @DeleteMapping("/delete/byId/{id}")
    public ResponseEntity<ObjectNode> deleteEmployeeById(@PathVariable int id) {
        logger.info("Received Request to Delete Employee whose Id : {}", id);
        return employeeService.deleteEmployeeById(id);
    }

    @DeleteMapping("/delete/byEmpCode/{empCode}")
    public ResponseEntity<ObjectNode> deleteEmployeeByEmpCode(@PathVariable String empCode) {
        logger.info("Received Request to Delete Employee whose Empcode : {}", empCode);
        return employeeService.deleteEmployeeByEmpCode(empCode);
    }

}