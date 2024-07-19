package com.kapture.employee.dto;

import com.kapture.employee.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private int id;
    private int clientId;
    private String name;
    private Date lastModifiedDate;
    private String designation;
    private String empCode;
    private Integer enable;

}