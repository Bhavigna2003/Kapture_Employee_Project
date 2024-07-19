package com.kapture.employee.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        int redisPort = 6379;
        String redisHostName = "localhost";
        config.useSingleServer().setAddress("redis://" + redisHostName + ":" + redisPort);
        config.setNettyThreads(0);
        config.setThreads(0);
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
