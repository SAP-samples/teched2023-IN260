# Exercise 4 (Optional) - Asynchronous Operations

One can make an application more resilient by running some tasks asynchronously. This often applies to tasks that don't need to succeed immediately.

In our use case, adding the goal or task to SuccessFactors is not a critical operation. We can run it asynchronously and if it fails, we can retry it a few times later.

## 4.1 Making the SuccessFactors Update Asynchronous

- [ ] 🔨 **Adjust the `signUp` method in the `SignupHandler` class to run the `updateSFSF` asynchronously in another thread.**
  - Check the application logs to confirm the SuccessFactors update is running after the response is sent to the user. 

<details><summary>Click here to view one possible solution.</summary>

```java
Executors.newCachedThreadPool()
        .execute(() -> updateSFSF(session));
```

</details>

> **Tip:** You can use the `Executors` class to create a thread pool and run a lambda in a new thread. 
> 
> Alternatively, you can also use Spring's `@Async` annotation on the `updateSFSF`.
> However, for that you first need to enable async processing.
> You can find more information on that e.g. in this [tutorial](https://www.baeldung.com/spring-async).

## 4.2 The Problem with this Approach

While the above seems to work, there is a fundamental problem with this approach.
We'll add a bit of code to demonstrate the problem.

- [ ] 🔨 **Add the following line anywhere in the `signUp` method in the `SignupHandler`:**
   
  ```java
   RequestContext.getCurrent(null);
   ```

While it looks a bit odd, this line will obtain the current [request context](https://cap.cloud.sap/docs/java/request-contexts).
The request context is a thread-local object that holds information such as the current user, tenant, locale, headers etc.

Now let's see how this becomes a problem.

[ ] 🔨 **Copy the same line into the `updateSFSF` method.** 

When you try to run this code, you'll see that the `updateSFSF` method fails with a `NullPointerException`.

The root cause of this problem is that the `RequestContext` is always bound to the original thread.
Any new thread we create will not have the context attached. So when we run the `updateSFSF` method in a different thread, the request context is not available anymore.

⚠️ This is a problem, even if we are not using the `RequestContext` explicitly in our code.
Various CAP and Cloud SDK features rely on the request context to be set correctly.

## 4.3 How to Fix the Problem

To fix this problem, we need to propagate the request context to the new thread.
This can be done quite easily using the `ThreadContextExecutors` class of the SAP Cloud SDK.

[ ] 🔨 **Replace `Executors.newCachedThreadPool()` with the `ThreadContextExecutors` class.** 

Now, the `updateSFSF` method should run successfully again.

> **Tip:** You can also easily adjust the `@Async` behaviour to use the `ThreadContextExecutors` class. This is documented [here](https://sap.github.io/cloud-sdk/docs/java/features/multi-tenancy/thread-context#spring-integration).

The `ThreadContextExecutors` is essentially the same as the `Executors` class.
But in addition to just running the new thread it also propagates the relevant context objects to the new thread.
Additionally, it takes care of cleaning up the context before the thread is eventually recycled for the next async operation.

> **Tip:** You can also extend the `ThreadContextExecutors` class to register your own context objects that need to be propagated to new threads. This is documented [here](https://sap.github.io/cloud-sdk/docs/java/features/multi-tenancy/thread-context#passing-on-other-threadlocals).

## 4.4 Using Resilience Patterns with Async Operations

For further convenience the `ResilienceDecorator` offers `queueSupplier`.
This will use a default `ThreadContextExecutor` to run your operation asynchronously and also apply the configured resilience patterns at the same time.
It returns a `CompletableFuture` which you can use e.g. to register a callback function.

You can configure the default `ExecutorService` the SAP Cloud SDK uses for the async execution.
For example, to increase the default thread pool used to 50 use:

```java
ThreadContextExecutors.setExecutor(DefaultThreadContextExecutorService.of(Executors.newFixedThreadPool(50)));
```

## 4.5 Understanding Multi Tenancy and Isolation Options

In case your application is multi tenant some of the resilience patterns become an issue for tenant isolation.
Specifically, all patterns that hold state across multiple executions need to be tenant-aware.

Based on what you've learned so far, can you tell which of these patterns may need to be isolated between tenants, assuming your operation performs tenant specific computation?

* Time Limiter
* Retry
* Rate Limiter
* Circuit Breaker
* Bulkhead
* Caching

<details><summary>Click here to see if you got it correct.</summary>

* Caching
* Rate Limiter
  * If one tenant performs an excessive amount of operations we should only limit that tenant and not degrade performance for all tenants.  
* Circuit Breaker
  * A similar argument can be made  
* Bulkhead

Caching obviously holds a tenants data, so that one is a must-have.
The rate limiter should also be applied per tenant.
If one tenant performs an excessive amount of operations we should only limit that tenant and not degrade performance for all tenants.
Similar arguments can be made for the circuit breaker and bulkhead.

In contrast, a timeout or retry only affects the tenant the operation is currently running for and has no side effects for other tenants.

</details>

> ℹ️ By default, the SAP Cloud SDK will isolate the relevant patterns based on the current tenant, if it exists.

Let's test this out by artificially setting a tenant to be defined in our context.

- [ ] 🔨 **Copy the below code into the `SignupHandler` class:**
  
   ```java
   private void run( SignUpContext context, String tenant, Runnable r) {
       Consumer<RequestContext> c = any -> r.run();
       context.getCdsRuntime().requestContext().modifyUser(u -> u.setTenant(tenant))
               .run(c);
   }
   ```

While this looks a bit complicated it just changes the tenant context for the duration of the operation we want to run.

> **Tip:** In a typical application the tenant context is derived from the authorization information of an incoming request.
> For example a JWT token from an XSUAA or IAS token service. 

We'll use this helper now to run the same operation with the same resilience config multiple times for different tenants, including not having any tenant defined:

- [ ] 🔨 **Replace the `updateSFSF(session)` method call as follows:**

   ```
   updateSFSF(session);
   run(context, "A", () -> updateSFSF(session));
   run(context, "B", () -> updateSFSF(session));
   run(context, "C", () -> updateSFSF(session));
   ```

- [ ] 🔨 **Disable all resilience patterns except for the rate limiter and set it to allow **_one_** request every 30 seconds.

When testing this now you should see the computation succeed, even though we run 4 times within the 30 second time frame.
But since we are setting a different tenant for each execution and the isolation is applied per tenant level by default all four calls are permitted.

Of course, it depends on your business logic whether an operation is specific to a tenant (or even a specific user), or if the operation is independent of the current tenant.
Fortunately, the isolation strategy can be configured.

Let's assume we want the rate limit to be applied across all tenants.

- [ ] 🔨 **Change the isolation level to not isolate between tenants and run the code again.** 

<details><summary>Click here to view the solution.</summary>

```java
var config = ResilienceConfiguration.of(SignupHandler.class)
                    .isolationMode(ResilienceIsolationMode.NO_ISOLATION)
```

> **Tip:** The default value is `TENANT_OPTIONAL`. 
> Among other strategies, you can also enforce that a tenant **must** be present.
> This can be done via the `TENANT_REQUIRED` option.
> This would throw an exception in case the current tenant could not be determined.

</details>

## Summary

You've now successfully learned how to use asynchronous abstractions provided by the SAP Cloud SDK.

Continue to - [Exercise 5 - Multi-Tenancy and Isolation](../ex5/README.md) to learn about how the SAP Cloud SDK handles multi-tenancy with respect to resilience.
