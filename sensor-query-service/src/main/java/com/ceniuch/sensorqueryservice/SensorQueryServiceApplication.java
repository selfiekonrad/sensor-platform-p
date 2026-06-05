package com.ceniuch.sensorqueryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.ceniuch.db.model")
@EnableJpaRepositories("com.ceniuch.sensorqueryservice.repository")
public class SensorQueryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensorQueryServiceApplication.class, args);
    }
}