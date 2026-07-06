package com.fullstack.adminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// scanBasePackages required: commonservice's shared beans (ExceptionAdvice, etc.)
// live under com.fullstack.commonservice, a sibling package Spring won't
// scan by default from com.fullstack.adminservice.
@SpringBootApplication(scanBasePackages = "com.fullstack")
@EnableDiscoveryClient
public class AdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminServiceApplication.class, args);
	}

}
