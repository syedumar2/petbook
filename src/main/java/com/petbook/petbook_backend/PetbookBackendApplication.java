package com.petbook.petbook_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class PetbookBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetbookBackendApplication.class, args);
	}

}
