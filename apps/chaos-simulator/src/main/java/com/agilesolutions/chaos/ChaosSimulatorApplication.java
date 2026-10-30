package com.agilesolutions.chaos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChaosSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChaosSimulatorApplication.class, args);
    }
}
