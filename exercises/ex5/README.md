# Exercise 5 - Multi-Tenancy and Isolation

Now let's explore the case where your application is multi tenant.

Some of the resilience patterns become an issue for tenant isolation.
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

You've now learned how the SAP Cloud SDK applies tenant and user isolation and how you can configure the behaviour to your needs.

Continue to - [Exercise 6 (Optional) - Deploying the application to SAP Business Technology Platform](../ex6/README.md) if you are interested in learning how to deploy your application to BTP.
