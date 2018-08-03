package com.redhat.solutions.fsi.samples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.redhat.solutions.fsi.samples"})
public class CfKieServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfKieServerApplication.class, args);
	}
}
