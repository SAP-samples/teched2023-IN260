# Exercise 4 - (Optional) Asynchronous Operations

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

//TODO

## Summary

You've now successfully learned how to use asynchronous abstractions provided by the SAP Cloud SDK.

Continue to - [Exercise 5 (Optional) - Deploying the application to SAP Business Technology Platform](exercises/ex5/) if you are interested in learning how to deploy your application to BTP.
