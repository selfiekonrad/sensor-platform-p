package com.ceniuch.sensormanagementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.ceniuch.db.model")
@EnableJpaRepositories(basePackages = {
        "com.ceniuch.common.db",
        "com.ceniuch.sensormanagementservice.repository"
})
public class SensorManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensorManagementServiceApplication.class, args);
    }

}
