package com.kapture.employee.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kapture.employee.dto.EmployeeDto;
import com.kapture.employee.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisService {


    private static RedissonClient redissonClient;

    public static final String EMPLOYEE_CACHE_PREFIX = "employee::";

    @Autowired
    public RedisService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public static void saveEmployee(Employee employee){
        try {
            RBucket<Employee> loginRBucket = redissonClient.getBucket(EMPLOYEE_CACHE_PREFIX+ employee.getId());
            loginRBucket.set(employee);
        } catch (Exception e) {
            log.error("Error saving employee to Redis: {}", e.getMessage());
        }
    }

    public static Employee getCachedEmployee(Long clientId, int page, int size) {
        try {
            RBucket<Employee> EmployeeRBucket = redissonClient.getBucket(EMPLOYEE_CACHE_PREFIX + clientId);
            return EmployeeRBucket.get();
        } catch (Exception e) {
            log.error("Error retrieving employee  from Redis: {}", e.getMessage());
            return null;
        }
    }


    public static Employee getCachedDesignationResponse(String designation, int page, int size) {
        try {
            RBucket<Employee> EmployeeRBucket = redissonClient.getBucket(EMPLOYEE_CACHE_PREFIX + designation);
            return EmployeeRBucket.get();
        } catch (Exception e) {
            log.error("Error retrieving employee  from Redis: {}", e.getMessage());
            return null;
        }
    }



    public static List<EmployeeDto> getCachedAllEmployees() {
        String cacheKey = EMPLOYEE_CACHE_PREFIX + "all";
        RBucket<List<EmployeeDto>> bucket = redissonClient.getBucket(cacheKey);
        return bucket.get();
    }



    public static Employee getCachedEmployeeByEmpCode(String empCode) {
        try {
            RBucket<Employee> EmployeeRBucket = redissonClient.getBucket(EMPLOYEE_CACHE_PREFIX + empCode);
            return EmployeeRBucket.get();
        } catch (Exception e) {
            log.error("Error retrieving employee  from Redis: {}", e.getMessage());
            return null;
        }
    }




    public static void deleteEmployeeCacheById(int id) {
        String cacheKey = EMPLOYEE_CACHE_PREFIX + "id::" + id;
        RBucket<ResponseEntity<ObjectNode>> bucket = redissonClient.getBucket(cacheKey);
        bucket.delete();
    }

    public static void deleteEmployeeCacheByEmpCode(String empCode) {
        String cacheKey = EMPLOYEE_CACHE_PREFIX + "empcode::" + empCode + "::deleted";
        RBucket<ResponseEntity<ObjectNode>> bucket = redissonClient.getBucket(cacheKey);
        bucket.delete();
    }

    public static Employee getCachedEmployeeById(int id) {
        try {
            RBucket<Employee> EmployeeRBucket = redissonClient.getBucket(EMPLOYEE_CACHE_PREFIX + id);
            return EmployeeRBucket.get();
        } catch (Exception e) {
            log.error("Error retrieving employee  from Redis: {}", e.getMessage());
            return null;
        }
    }


}
