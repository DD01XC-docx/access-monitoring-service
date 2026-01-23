package com.dd01xc.service;

import org.springframework.boot.SpringApplication;

public class TestAmcApplication {

	public static void main(String[] args) {
		SpringApplication.from(AmcApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
