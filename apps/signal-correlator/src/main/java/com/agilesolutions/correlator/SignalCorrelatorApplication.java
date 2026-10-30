package com.agilesolutions.correlator;

import com.agilesolutions.correlator.config.CorrelatorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
@EnableConfigurationProperties(CorrelatorProperties.class)
public class SignalCorrelatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(SignalCorrelatorApplication.class, args);
    }
}
