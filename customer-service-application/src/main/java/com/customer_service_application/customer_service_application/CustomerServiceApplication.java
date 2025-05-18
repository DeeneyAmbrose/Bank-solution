package com.customer_service_application.customer_service_application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomerServiceApplication {


	public static void main(String[] args) {
		String date = java.time.LocalDate.now().toString(); // e.g. 2025-05-18
		System.setProperty("log.folder", "customer-service/logs");
		System.setProperty("log.filename", date);

		SpringApplication.run(CustomerServiceApplication.class, args);
	}

}
