package dev.dd01xc.monitoring.access_monitoring_service;

import org.springframework.boot.SpringApplication;

public class TestAccessMonitoringServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(AccessMonitoringServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
