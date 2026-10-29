package com.agilesolutions.incident;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class IncidentApplication {

	public static void main(String[] args) {
		SpringApplication.run(IncidentApplication.class, args);
	}

}
