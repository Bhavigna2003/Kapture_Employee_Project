package com.kapture.employee.validation;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kapture.employee.dto.EmployeeDto;
import com.kapture.employee.entity.Employee;
import com.kapture.employee.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.kapture.employee.constants.EmployeeConstants.*;
import static com.kapture.employee.constants.EmployeeConstants.NOT_VALID_ENABLE;

public class EmployeeValidation {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeValidation.class);

    public static boolean isNotNullNotEmpty(String s) {
        return s != null;
    }


    public static boolean isValidClientId(int clientId) {
        return clientId != 0 && (clientId == 1 || clientId == 2);
    }


    public static boolean isValidDesignation(String designation) {
        // Convert to lowercase to handle case insensitivity
        String lowerCaseDesignation = designation.toLowerCase();
        // Check if the lowercase designation matches any valid values
        return "a".equals(lowerCaseDesignation) ||
                "m".equals(lowerCaseDesignation) ||
                "e".equals(lowerCaseDesignation);
    }

    public static boolean isValidEnableFlag(int enable) {
        return (enable == 1 || enable == 0);
    }

    public static boolean isNullValidEnableFlag(Integer enable) {
        return enable != null ;
    }

    public static boolean isValidEmployeeDetails(EmployeeDto employeeDto) {
        if (!isNotNullNotEmpty(employeeDto.getName()) || !isValidClientId(employeeDto.getClientId())) {
            logger.error(PROVIDE_VALID_DETAILS);
            return false;
        }

        if (employeeDto.getDesignation() != null && !isValidDesignation(employeeDto.getDesignation())) {
            logger.error(PROVIDE_VALID_DESIGNATION);
            return false;
        }

        return true;
    }

    public static void checkupdateEmployeeFields(Employee employee, EmployeeDto request) {

        if (request.getName() != null) {
            employee.setName(request.getName());
        }

        if (isNullValidEnableFlag(request.getEnable())) {
            if (isValidEnableFlag(request.getEnable())) {
                employee.setEnable(request.getEnable());
                logger.info("Employee Enable is updated");
            } else {
                logger.error(NOT_VALID_ENABLE);
                throw new IllegalArgumentException(NOT_VALID_ENABLE);
            }
        }

        if (request.getDesignation() != null) {
            if (isValidDesignation(request.getDesignation())) {
                employee.setDesignation(request.getDesignation().toUpperCase());
                logger.info("Employee Designation updated");
            } else {
                logger.error(PROVIDE_VALID_DESIGNATION);
                throw new IllegalArgumentException(PROVIDE_VALID_DESIGNATION);
            }
        }

    }

}

