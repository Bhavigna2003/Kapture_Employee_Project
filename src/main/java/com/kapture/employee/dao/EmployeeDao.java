package com.kapture.employee.dao;

import com.kapture.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeDao {

    void saveEmployee(Employee employee);

   void saveBulkEmployees(List<Employee> employees);

    List<Employee> findByClientId(Long clientId, int page, int size);

    Page<Employee> findByDesignation(String designation, Pageable pageable);

    List<Employee> findAllEmployees();

    Optional<Employee> findById(int id);

    Optional<Employee> findByEmpCode(String empCode);

    void updateEmployee(Employee employee);

    void deleteEmployee(Employee employee);

}
