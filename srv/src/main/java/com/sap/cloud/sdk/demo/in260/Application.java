package com.sap.cloud.sdk.demo.in260;

import com.sap.cloud.sdk.cloudplatform.security.principal.DefaultPrincipalFacade;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceDecorator;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		setupCloudSdk();

		SpringApplication.run(Application.class, args);
	}

	private static void setupCloudSdk()
	{
		ResilienceDecorator.setDecorationStrategy(
				new com.sap.cloud.sdk.cloudplatform.resilience4j.Resilience4jDecorationStrategy());
		PrincipalAccessor.setPrincipalFacade(new DefaultPrincipalFacade());
	}

}
