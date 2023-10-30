# Exercise 2 - Use SAP Cloud SDK to make your Application Resilient

In this exercise, we will learn about the various resilience patterns the SAP Cloud SDK offers to make your application more robust.

The following resilience patterns are available: 

* Time Limiter
* Retry
* Rate Limiter
* Circuit Breaker
* Bulkhead
* Fallback
* (Caching)

In this exercise we'll explore how the first three patterns help your application become more resilient and how you can use them effectively.

## Motivation

Resilience is an important property of any modern cloud application and even more so for microservice based systems.
We'll go over some of the benefits on the individual patterns.

But there are two aspects that the SAP Cloud SDK does across all patterns:

1. It integrates the current tenant/user context. 
   - This is required for all resilience patterns that hold some form of state to prevent interference between tenants/users.
   - This also includes passing on the context across different threads, if needed.
2. It provides a relatively simple API that can be used around any block of code.
   - That means the resilience can be used around arbitrary code, not just with SAP Cloud SDK APIs or HTTP requests. 

## 2.1 - Add the Required Dependencies to Your Project

- [ ] 🔨 **In your project's application [`pom.xml`](../../srv/pom.xml) file add the following dependencies to the dependencies section**:
    ```xml
     <!-- SAP Cloud SDK Resilience -->
     <dependency>
         <groupId>com.sap.cloud.sdk.cloudplatform</groupId>
         <artifactId>resilience</artifactId>
     </dependency>
     <dependency>
         <groupId>com.sap.cloud.sdk.cloudplatform</groupId>
         <artifactId>resilience4j</artifactId>
     </dependency>
    ```

While the first dependency offers the API to interact with the SAP Cloud SDK, the second one actually provides the internal implementation of the resilience patterns.

## 2.2 - Use the Resilience API

In general the API works as follows:

- Use the `ResilienceConfiguration` class to build a resilience configuration according to your needs.
- Use the `ResilienceDecorator` class to run any block of code with a given resilience configuration.

We'll first enhance `SignupHandler` and apply some resilience patterns to the `updateSFSF` method.

- [ ] 🔨 **Create a `ResilienceConfiguration` with default values and use it with the execution of the `updateSFSF` method.**

<details><summary>Click here to view a solution.</summary>

```java
var config = ResilienceConfiguration.of(SignupHandler.class);

var goal = ResilienceDecorator.executeSupplier(goalService::getLearningGoal, config);
```

- `.of(..)` simply provides a default configuration.
  - Check the Javadoc to see what is configured by default. 
- The identifier (in this cass the class object) is used to uniquely identify this configuration across different invocations.
  - This is required to keep any state across multiple executions
- The `executeSupplier` then takes care of actually running the code

> **Tip:** You can use `.empty(...)` to get a configuration without any defaults.
> 
</details>

If you like you can run your application at this point.
You should not notice any difference except for some additional logs.

## 2.3 - Use the Time Limiter Pattern

Imagine a remote service that your application interacts with experiences some temporary performance issues and is slow to respond.
Your application does not have a timeout. 
_Now your application is slow to respond as well._

So a timeout can help preventing faults in one system to propagate to other systems.
In our case the interaction with SAP SuccessFactors is less important than the actual registration.
So we don't want to keep the user waiting too long for a response on the signup request. 
The default configuration we used above already comes with a timeout of 30 seconds.
To better demonstrate the effect let's reduce that.

- [ ] 🔨 **Modify the `ResilienceConfiguration` to apply a two-second timeout.**

<details><summary>Click here to view a solution.</summary>

 ```java
var timeout = TimeLimiterConfiguration.of(Duration.ofSeconds(2));
var config = ResilienceConfiguration.of(SignupHandler.class)
                .timeLimiterConfiguration(timeout);
  ```

> **Tip:** You can add imports for the nested classes inside `ResilienceConfiguration` to make the code a bit shorter in case your IDE by default uses the full name.

</details>

Now let's test the timeout actually takes effect if the system does not respond in time.

## 2.3 - Locally Test the Resilience Patterns

- [ ] 🔨 **Run the application locally with `mvn spring-boot:run` or by running/debugging the [`Application`](../../srv/src/main/java/com/sap/cloud/sdk/demo/in260/Application.java) class.**
- [ ] 🔨 **Open the application frontend at http://localhost:8080/#resilience.**

Notice the minor URL change.
In addition to the list of sessions, you will see two adjustable input fields for artificial **delay** and **fault rate**.
Those values will be interpreted by the mock server to simulate a slow or faulty backend system.

- [ ] 🔨 **Use these tools to trigger the timeout in the backend.**
 
When clicking on a session you should see the loading indicator for 2 seconds, after which an error message for `TimeoutException` should appear.

> **Tip:** You can reset the UI by reloading the page.

Congratulations, you have successfully tested the timeout resilience pattern.
Feel free to play around with the delay and resilience timeout configuration to see how the application behaves.

> **Tip:** The `TimelimiterConfiguration` has one further option that can be set. Also, internally the timeout requires the operation to run in a separate thread. The thread pool for this can be configured as well. You can learn more about the async execution options in [exercise 4](../ex4/README.md).

## 2.4 - The Retry Pattern

Next, we'll look at the retry pattern.
Retries can be a useful extension to timeouts: If something didn't work, try again later.

Retries are particularly applicable to transient errors, i.e. errors that are not permanent and might go away after a while. Also, they are well suited for idempotent operations, i.e. operations that can be repeated without any side effects. For operations that potentially change state, be mindful whether performing an operation again is safe to do.

> **Tip:** Because retries are not always safe to be applied they are disabled by default when using the default configuration.

In our case the request to obtain the learning goal does not modify any state in the SAP SuccessFactors system, so it should be safe to repeat the request in case of a failure.

- [ ] 🔨 **Extend the resilience configuration to add a retry behaviour**.
  - Try three total attempts and one second of delay for starters.
  - Use the webpage to run with `100%` failure rate an no delay.
  - Check the application logs to see the retries in action. 
    - (If you like add some logging statements to make it more visible among the other debug logs.)
  - Play around with different values to see how the behaviour changes.

<details><summary>Click here to view a solution.</summary>

 ```java
var retry = RetryConfiguration.of(3, Duration.ofSeconds(1));
var config = ResilienceConfiguration.of(SignupHandler.class)
        .timeLimiterConfiguration(timeout)
        .retryConfiguration(retry);
```

> **Tip:** Did you notice what happened to our timeout? It is still working, but it is getting applied per individual retry. Keep this in mind when configuring retries -- you may need to adjust your timeout settings (or apply an additional overall timeout).
> //TODO verify
> 
> This is not the only interaction between different resilience patterns, and you'll learn more about this in exercise 2.6.
> //TODO

</details>

> **Tip:** In case you run into an exception regarding a `CircuitBreaker`: You have overdone it with the retries and discovered yet another resilience pattern 😉
> You can learn more about the circuit breaker in the optional [exercise 2.7](#26) below.
> //TODO

Now, you might wonder how the retry pattern decides what is a success and what is a failure that needs to be re-run.

> ℹ️ In general, all resilience patterns of the SAP Cloud SDK for Java consider _**any exception**_ that is not caught and handled by the operation to be a failure.

However, not all failures are transient in nature and can be expected to resolve over time.
If you know certain exceptions are not worthwhile retrying you can exclude them from the retry behaviour.

- [ ] 🔨 (optional) **Extend the resilience configuration to exclude a specific exception type from the retry behaviour.**
  - Use any exception type you like and make sure it is thrown inside the resilient code.
  - Before you move on with the next exercise make sure to revert this change.

<details><summary>Click here to view a solution.</summary>
    
```java
static class NoBuenoException extends RuntimeException {
    public NoBuenoException(String message) {
        super(message);
    }
}

private void updateSFSF(String session) {
    // create a goal and related tasks in SFSF
    var timeout = TimeLimiterConfiguration.of(Duration.ofSeconds(2));
    var retry = RetryConfiguration.of(3, Duration.ofSeconds(1))
            .retryOnExceptionPredicate(e -> !(e instanceof NoBuenoException));
    var config = ResilienceConfiguration.of(SignupHandler.class)
            .timeLimiterConfiguration(timeout)
            .retryConfiguration(retry);

    Goal101 goal = ResilienceDecorator.executeSupplier(() -> {
        throw new NoBuenoException("No bueno, no point in trying again :/");
    }, config);
```

</details>

> **Tip:** Alternatively, you can also make sure you handle the exceptions not to be retried within the resilience block.

While retries can be useful, we also need to be careful not to overdo it.
If we perform long-running, unreliable or expensive computations too often we'll put additional load on our system and on other systems we communicate with.

Fortunately, there is a pattern that deals with this aspect as well.

## 2.5 - The Rate-Limiter Pattern

A rate limit is a restriction on the number of times a certain operation can be performed in a given time window.

- [ ] 🔨 **Add a rate limiter to the resilience configuration.**
  - Explore the API and choose a configuration that makes sense to you.
  - Again, play around with the values (including the other patterns) to try to exceed the rate limit and see how other patterns might interact with the rate limit.

<details><summary>Click here to view a solution.</summary>

```java
 var rateLimit = RateLimiterConfiguration.of(
                 Duration.ofSeconds(10),
                 Duration.ofSeconds(30),
                 10);
var config = ResilienceConfiguration.of(SignupHandler.class)
                .timeLimiterConfiguration(timeout)
                .retryConfiguration(retry)
                .rateLimiterConfiguration(rateLimit);
```

</details>

// TODO add section on isolation levels <-- (change for visibility only)

## Summary

You've now successfully learned how to use resilience patterns of the SAP Cloud SDK to improve application robustness.
There are more patterns available, like the circuit breaker or bulkhead.
You can learn more in the following optional exercises or continue to the next exercise:  [Exercise 3 - Caching with the SAP Cloud SDK](../ex3/README.md)

## 2.6 - (optional) The Circuit Breaker Pattern

A circuit breaker is a pattern that can be used to prevent an application from repeatedly trying to perform an operation that is likely to fail.

This is useful in various ways.
For example, it ensures that a system which is experiencing issues is not overloaded with requests and has time to recover.
Also it helps with making sure an application doesn't spend too much time waiting for an operation that is unlikely to finish successfully.

The circuit breakers achieves this by measuring the percentage of failures over a given time window.
If it exceeds a threshold it slows down the rate of operations (similar to the rate-limited) until the failre rate drops down again.

- [ ] 🔨 **Add a circuit breaker to the resilience configuration.**
  - Leverage the failure-rate setting of the frontend to force the circuit breaker to open.
  - Again, play around with the values (including the other patterns) to try to exceed the rate limit and see how other patterns might interact with the rate limit.

<details><summary>Click here to view a solution.</summary>

```java
var circuitBreaker = CircuitBreakerConfiguration.of()
        .failureRateThreshold(50)
        .waitDuration(Duration.ofSeconds(10));
var config = ResilienceConfiguration.of(SignupHandler.class)
        .timeLimiterConfiguration(timeout)
        .retryConfiguration(retry)
        .rateLimiterConfiguration(rateLimit)
        .circuitBreakerConfiguration(circuitBreaker);
```

This example configures a circuit breaker that will prevent requests for 10 seconds if the failure rate exceeds 50%.

> **Tip:** The circuit breaker can be a bit tricky to understand and configure.
> You can find more information on how it works in detail in the [resilience4j documentation](https://resilience4j.readme.io/docs/circuitbreaker).

</details>

> **Tip:** When the failure rate is too high the circuit breaker will transition into the `OPEN` state. This may sound a bit counterintuitive, as the word "open" may suggest requests are permitted. 
> 
> However, the opposite is true: An open circuit breaker does not permit further requests.
> The term comes from electrical engineering where an open circuit is one that does not allow any electrical current to flow.

## 2.7 - (optional) Interactions between Resilience Patterns

// TODO