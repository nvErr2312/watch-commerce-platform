package com.fullstack.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// scanBasePackages required: commonservice's shared beans (ExceptionAdvice, etc.)
// live under com.fullstack.commonservice, a sibling package Spring won't
// scan by default from com.fullstack.productservice.
@SpringBootApplication(scanBasePackages = "com.fullstack")
@EnableDiscoveryClient
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

}
