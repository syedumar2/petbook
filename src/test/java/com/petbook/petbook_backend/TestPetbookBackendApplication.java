package com.petbook.petbook_backend;

import org.springframework.boot.SpringApplication;

public class TestPetbookBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(PetbookBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
