package org.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Stylo Customer microservice.
 * This is the entry point for the Spring Boot application that handles
 * user management, authentication, and authorization for the Stylo platform.
 */
@SpringBootApplication
public class StyloCustomerApplication {

	/**
	 * The main method that starts the Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application.
	 */
	public static void main(String[] args) {
		SpringApplication.run(StyloCustomerApplication.class, args);
	}
}
