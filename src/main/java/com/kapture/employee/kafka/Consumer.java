package com.kapture.employee.kafka;


import com.kapture.employee.entity.Employee;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {
    @KafkaListener(topics = "Employee-Details",groupId = "group",containerFactory = "userKafkaListenerContainerFactory")
    public void consumeJson(Object employee){
        System.out.println("Consumed JSON Message "+ employee);
    }
}