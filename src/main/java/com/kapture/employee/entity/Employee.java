package com.kapture.employee.entity;

import com.kapture.employee.util.EmployeeUtil;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import com.kapture.employee.util.EmployeeUtil.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity

@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "client_id",nullable = false)
    private int clientId;

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name = "last_modified_date")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date lastModifiedDate;

    @Column(name = "designation")
    private String designation;

    @Column(name = "emp_code", unique = true)
    private String empCode;

    @Column(name = "enable", columnDefinition = "tinyint(1)")
    private Integer enable;

    public Employee(int clientId, String name) {
        this.clientId = clientId;
        this.name = name;
        this.designation = "E"; // Default designation
        this.enable = 1; // Default enable value
        this.empCode = EmployeeUtil.generateEmpCode();// Generate empCode
    }



}
