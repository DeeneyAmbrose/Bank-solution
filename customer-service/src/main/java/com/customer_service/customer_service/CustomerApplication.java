package com.customer_service.customer_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class CustomerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(CustomerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String eurekaUrl = "http://localhost:8761/eureka/apps";
		RestTemplate restTemplate = new RestTemplate();

		int maxRetries = 3;
		int retryCount = 0;
		boolean eurekaAvailable = false;

		while (retryCount < maxRetries) {
			try {
				restTemplate.getForObject(eurekaUrl, String.class);
				eurekaAvailable = true;
				break;
			} catch (Exception e) {
				System.out.println("Waiting for Eureka Server to start... Attempt " + (retryCount + 1));
				Thread.sleep(7000);
				retryCount++;
			}
		}

		if (!eurekaAvailable) {
			System.err.println("Eureka Server is not available. Shutting down...");
			System.exit(1);
		}
	}
}
