package com.sap.cloud.sdk.demo.in260;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceDecorator;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		ResilienceDecorator.setDecorationStrategy(
				new com.sap.cloud.sdk.cloudplatform.resilience4j.Resilience4jDecorationStrategy());
		SpringApplication.run(Application.class, args);
	}

}
