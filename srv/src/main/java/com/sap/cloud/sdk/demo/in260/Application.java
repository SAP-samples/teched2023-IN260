package com.sap.cloud.sdk.demo.in260;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceDecorator;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		// Disclaimer: This is temporary code and not required when using a released version of the SAP Cloud SDK
		ResilienceDecorator.setDecorationStrategy(
				new com.sap.cloud.sdk.cloudplatform.resilience4j.Resilience4jDecorationStrategy());
		SpringApplication.run(Application.class, args);
	}

}
