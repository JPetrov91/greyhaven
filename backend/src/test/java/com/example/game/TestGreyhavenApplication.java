package com.example.game;

import org.springframework.boot.SpringApplication;

public class TestGreyhavenApplication {

	public static void main(String[] args) {
		SpringApplication.from(GreyhavenApplication::main).with(TestcontainersConfiguration.class).run(args);
	}
}
