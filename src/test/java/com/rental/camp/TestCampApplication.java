package com.rental.camp;

import org.springframework.boot.SpringApplication;

public class TestCampApplication {

	public static void main(String[] args) {
		SpringApplication.from(CampApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
