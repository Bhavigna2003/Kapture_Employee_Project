package com.kapture.employee.mapper;

import com.kapture.employee.dto.EmployeeDto;
import com.kapture.employee.entity.Employee;

public class EmployeeMapper {
    public static EmployeeDto mapToEmployeeDto(Employee employee){
        return new EmployeeDto(
                employee.getId(),
                employee.getClientId(),
                employee.getName(),
                employee.getLastModifiedDate(),
                employee.getDesignation(),
                employee.getEmpCode(),
                employee.getEnable()
        );
    }

    public static Employee mapToEmployee(EmployeeDto employeeDto){
        return new Employee(
          employeeDto.getId(),
          employeeDto.getClientId(),
          employeeDto.getName(),
          employeeDto.getLastModifiedDate(),
          employeeDto.getDesignation(),
          employeeDto.getEmpCode(),
          employeeDto.getEnable()
        );
    }
}
