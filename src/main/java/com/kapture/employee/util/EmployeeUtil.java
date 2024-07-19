package com.kapture.employee.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kapture.employee.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static com.kapture.employee.constants.EmployeeConstants.PROVIDE_VALID_DESIGNATION;

public class EmployeeUtil {


    private static final Logger logger = LoggerFactory.getLogger(EmployeeUtil.class);

    public static String generateEmpCode() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    public static ObjectNode createSuccessResponse(String message) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("message", message);
        return response;
    }

    public static ObjectNode createErrorResponse(String message) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("error", message);
        return response;
    }

    public static ObjectNode createResponseObject(int status, String message) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode responseObject = mapper.createObjectNode();
        responseObject.put("status", status);
        responseObject.put("message", message);
        logger.info(message);
        return responseObject;
    }

    public static ResponseEntity<?> createInvalidDesignationResponse() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode responseObject = mapper.createObjectNode();
        responseObject.put("status", HttpStatus.BAD_REQUEST.value());
        responseObject.put("message", PROVIDE_VALID_DESIGNATION);
        logger.error(PROVIDE_VALID_DESIGNATION);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseObject);
    }

    public static ResponseEntity<?> handleEmployeeNotFound(int id) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode responseObject = mapper.createObjectNode();
        responseObject.put("status", HttpStatus.NOT_FOUND.value());
        responseObject.put("message", "Employee not found with ID: " + id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseObject);
    }

}
