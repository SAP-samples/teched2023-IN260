# Exercise 2 - Use SAP Cloud SDK to make your Application Resilient

In this exercise, we will learn about the SAP Cloud SDK offering for various resilience patterns to make your application more robust.

Applying patterns like the following will help to make your code more resilient against failures it might encounter:
* Time Limiter
* Retry
* Rate Limiter
* Circuit Breaker
* Bulkhead

In this exercise, we'll focus on the first two patterns.

### Motivation

//TODO update this to better match slides <-- (change for visibility only)

This exercise is about optional code improvements.
The benefits may not be directly visible, but they are important for a productive application runtime.
If your application serves multiple tenants or principals, or if it interacts with a multitude of external systems, then it will probably receive unbalanced computational load - depending on the context.
You will want to avoid the situation where one operation with error-prone context disrupts the session for other customers and users, or worst case - brings the whole application to a halt.

In order to protect yourself from this, you can use the resilience patterns provided by the SAP Cloud SDK.

### Prerequisites

- You can start the application locally with `mvn spring-boot:run` or by running/debugging the `Application` class.
- You can access the running application frontend at `http://localhost:8080` and it displays a list of sessions.

## 2.1 - Add the Required Dependencies to Your Project

- [ ] In your project's application [`pom.xml`](../../srv/pom.xml) file add the following dependencies to the dependencies section:
    ```xml
     <!-- SAP Cloud SDK Resilience -->
     <dependency>
         <groupId>com.sap.cloud.sdk.cloudplatform</groupId>
         <artifactId>resilience</artifactId>
     </dependency>
     <dependency>
         <groupId>com.sap.cloud.sdk.frameworks</groupId>
         <artifactId>resilience4j</artifactId>
     </dependency>
    ```
   While the first dependency offers the API to interact with the SAP Cloud SDK, the second one actually provides the internal implementation of the resilience patterns.

## 2.2 - Use the Resilience API

Imagine, the interaction with SAP SuccessFactors is not very reliable, maybe it loads too long without any result.
To improve user experience in the web application, it would be worthwhile to interrupt the lasting request and show a message to the user. 

//TODO add disclaimer that this is a temporary workaround <-- (change for visibility only)

- [ ] Extend the `main` method in [`Application`](../../srv/src/main/java/com/sap/cloud/sdk/demo/in260/Application.java) class by:
   ```diff
   + ResilienceDecorator.setDecorationStrategy(new Resilience4jDecorationStrategy());
   ```
   This will activate the correct resilience decorator for the whole application.

- [ ] Create a `ResilienceConfiguration` configured with a `TimeLimiterConfiguration` with a timeout of 2 seconds in [`SignupHandler`](../../srv/src/main/java/com/sap/cloud/sdk/demo/in260/SignupHandler.java) class in your application.
   Declare a static field on the class to hold the resilience configuration:
   ```java
   private static final ResilienceConfiguration RESILIENCE_CONFIG = ResilienceConfiguration.of("get-goals")
       .timeLimiterConfiguration(ResilienceConfiguration.TimeLimiterConfiguration.of(Duration.ofSeconds(2)));
   ```
   For every Resilience API usage, providing an operation identifier is a requirement.
   The identifier is used to provide a unique context for the resilience patterns.
   By default, the resilience properties are applied with tenant and principal isolation activated.
   The configuration above is named "get-goals" and will limit the execution of the decorated operation to 2 seconds.

- [ ] Improve the `updateSFSF` method:
   ```diff
     var goal =
   -   goalService.getLearningGoal();
   +   ResilienceDecorator.executeSupplier(() -> goalService.getLearningGoal(), RESILIENCE_CONFIG);
   ```
  With the improvement from above, the retrieval of the learning goal from SAP SuccessFactors is limited to 2 seconds. If the operation takes longer, it will be aborted and the user will be notified.

The resilience configuration allows for more configuration.
But for now let's focus on the timeout.

## 2.3 - Locally Test the Resilience Patterns

In order to test the resilience patterns locally, we need to direct our requests from SAP SuccessFactors to a locally provided mock server.

- Change the destination url of `SFSF-BASIC-ADMIN` to `http://localhost:8080`.
- Run the application locally with `mvn spring-boot:run` or by running/debugging the [`Application`](../../srv/src/main/java/com/sap/cloud/sdk/demo/in260/Application.java) class.
- Open the application frontend at http://localhost:8080/#resilience.
   Notice the minor URL change.
   In addition to the list of sessions, you will see two adjustable input fields for artificial **delay** and **fault rate**.
- Enter a delay of `3000` and keep the fault rate at `0`.
- When clicking on one session, you will see the loading indicator for 2 seconds, after which an error message for `TimeoutException` appears.

Congratulations, you have successfully tested the timeout resilience pattern.
Please feel free to play around with the delay and resilience timeout configuration to see how the application behaves.

## 2.4 - Use the Retry Pattern

- [ ] Extend the `RESILIENCE_CONFIG` declaration to add retry configuration in [`SignupHandler`](../../srv/src/main/java/com/sap/cloud/sdk/demo/in260/SignupHandler.java).
   ```diff
     private static final ResilienceConfiguration RESILIENCE_CONFIG = ResilienceConfiguration.of("get-goals")
         .timeLimiterConfiguration(ResilienceConfiguration.TimeLimiterConfiguration.of(Duration.ofSeconds(2)))
   +     .retryConfiguration(ResilienceConfiguration.RetryConfiguration.of(3, Duration.ofSeconds(1)));
   ```
   The configuration above adds a retry mechanism to repeat the operation upon failure. 
   The number of invocations is limited to 3.
   Between each invocation, there will be a 1-second delay.

That's it, now let's test the retry resilience pattern.

- Open the application frontend at http://localhost:8080/#resilience.
- Enter a failure rate percentage of `100` and keep the delay at `0`. 
- When clicking on a session, you will receive an error message.

In the application logs you will notice the additional retries.
Please feel free to play around with the failure rate, to make the effect more visible.
If you add delay, you can check which one fails first - the retry or time-limiter.
In production, you will need to make a reasonable decision for these settings, depending on the target system.

// TODO add optional section on choosing exceptions to retry <-- (change for visibility only)

## 2.5 - Use the Rate-Limiter Pattern

// TODO motivation <-- (change for visibility only)

- [ ] Extend the `RESILIENCE_CONFIG` declaration to add a rate limiter configuration in [`SignupHandler`](../../srv/src/main/java/com/sap/cloud/sdk/demo/in260/SignupHandler.java).
   ```diff
     private static final ResilienceConfiguration RESILIENCE_CONFIG = ResilienceConfiguration.of("get-goals")
         .timeLimiterConfiguration(ResilienceConfiguration.TimeLimiterConfiguration.of(Duration.ofSeconds(2)))
         .retryConfiguration(ResilienceConfiguration.RetryConfiguration.of(3, Duration.ofSeconds(1)))
   +     .rateLimiterConfiguration(ResilienceConfiguration.RateLimiterConfiguration.of(Duration.ofSeconds(1), Duration.ofSeconds(2), 10));
   ```
   The configuration above adds a rate limiter, to limit the number of times the decorated operation is invoked in a moving time window.
   The number of invocations is limited to 10.
   The moving time window has a duration of 30 seconds.
   The punishment for exceeding the limit is a 1-second delay.

That's it, now let's test the rate limiter resilience pattern.

// TODO formatting <-- (change for visibility only)

- Open the application frontend at http://localhost:8080/#resilience.
- Keep failure rate and delay at `0`.
- Click and register for all sessions, reload the website if necessary.
   At some point, you will notice a 1s delay.

Please feel free to play around with the time-limiter and rate-limiter configuration to make this effect more visible.

// TODO add section on isolation levels <-- (change for visibility only)

// TODO add optional section on circuit breaker <-- (change for visibility only)

## Summary

You've now successfully learned how to use resilience patterns of the SAP Cloud SDK to improve application robustness.
There are more patterns available, like the circuit breaker or bulkhead.
Also caching can be used with the same `Resilience API`, let's have a look at that in the next exercise.

Continue to - [Exercise 3 - Caching with the SAP Cloud SDK](../ex3/README.md)
