# Exercise 3 - Caching with the SAP Cloud SDK

Caching is a common pattern used to improve the performance of applications.
It allows us to store and retrieve frequently accessed data quickly, reducing the load on backend systems and improving the overall user experience.

In this exercise, we will learn how to enable caching in the existing application using the caching abstractions provided by the SAP Cloud SDK.

In the application we built, we register a user for an event or session with the help of a synthetic OpenAPI service.
The business logic of registering for the session or event is implemented in the [`RegistrationServiceHandler`](../../srv/src/main/java/com/sap/cloud/sdk/demo/in260/remote/RegistrationServiceHandler.java).
Let's cache the results of fetching the TechEdEvent from the OpenAPI service. 

## 3.1 - Add the Required Dependencies to Your Project

The SAP Cloud SDK relies on the `JCache` Service Provider Interface to create and manage cache instances and so, it is required to provide an implementation of the `JCache` interface for using the SDK's caching abstractions.

We use [`Caffeine`](https://github.com/ben-manes/caffeine) as an implementation for this exercise.

- [ ] 🔨 **Head to your project's application [`pom.xml`](../../srv/pom.xml) file and add the following dependency to the dependencies section:**
    ```xml
     <!-- SAP Cloud SDK Resilience -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>jcache</artifactId>
        <version>3.1.8</version>
        <scope>runtime</scope>
    </dependency>
    ```
In the next step, we will create a cache configuration that will be used for caching the results of the OpenAPI service.

## 3.2 - Create a Cache Configuration

The `CacheConfiguration` API is similar to the resilience patterns API that you learned about in the previous exercise.

It allows you to configure three things:

- Cache Duration (Required): The duration after which the cache entry will be invalidated.
- Expiration Strategy (Optional): The strategy to be used for invalidating the cache entry. 
- Parameters (Optional): Additional parameters added to the cache key.

- [ ] 🔨 **Create a static `CacheConfiguration` without any parameters and an expiration duration of one day.**
- [ ] 🔨 **Create a static `ResilienceConfiguration` with the cache configuration created in the previous step and add them both in the [`RegistrationServiceHandler`](../../srv/src/main/java/com/sap/cloud/sdk/demo/in260/remote/RegistrationServiceHandler.java) class.**

<details> <summary>Click here to view the solution.</summary>

  ```java
  private static final ResilienceConfiguration.CacheConfiguration cacheConfiguration = ResilienceConfiguration.CacheConfiguration
          .of(Duration.ofDays(1)).withoutParameters();
  private static final ResilienceConfiguration resilienceConfiguration = ResilienceConfiguration.empty("caching-config")
          .cacheConfiguration(cacheConfiguration);
   ```
</details>

> **Tip:** The cache functionality is tenant and user aware by default.
> Even if no parameters are provided for the cache key, the tenant and principal, if available, will be used as a parameter for the cache key.

Let's now apply the resilience configuration inside the `getTechEdEvent()` method.

## 3.3 - Apply the Cache Configuration

The `ResilienceDecorator` API allows you to apply the resilience configuration to a method call.

- [ ] 🔨 **Apply the `ResilienceDecorator` to cache `api.getEvents()` in the `getTechEdEvent()` method call in the [`RegistrationServiceHandler`](../../srv/src/main/java/com/sap/cloud/sdk/demo/in260/remote/RegistrationServiceHandler.java) class.**

<details> <summary>Click here to view the solution.</summary>

```java
    @GetMapping( path = "/rest/v1/getTechEdEvent", produces = "application/json")
    public Event getTechEdEvent() {
        var api = new EventRegistrationApi(getDestination());

        List<Event> events = ResilienceDecorator.executeSupplier(() -> api.getEvents(), resilienceConfiguration);

        return events
        .stream()
        .filter(e -> e.getName().equals("TechEd 2023"))
        .findFirst()
        .orElseThrow();

    }
 ```
</details>

## 3.4 (Optional) - Access the Created Cache

- [ ] 🔨 **You can use the JCache API to access the created cache and examine its contents. Try logging the contents of the cache to the console.**

> **Tip:** You can get hold of the cache by using:
> ```java
> Caching.getCachingProvider().getCacheManager().getCache("caching-config")
> ```

<details> <summary>Click here to view the solution.</summary>

```java
  //Place the code inside getTechEdEvent() method
  final Cache<Object, Object> cache = Caching.getCachingProvider().getCacheManager().getCache("caching-config");
  if (cache != null) {
      for (Cache.Entry<Object, Object> entry : cache) {
                  log.info("The cached is " + entry.getKey() + ":" + entry.getValue());
      }
  }
 ```
</details>

## Summary

You've now successfully learned how to use caching abstraction provided by the SAP Cloud SDK.

Continue to - [Exercise 4 - (Optional) Asynchronous Operations](exercises/ex4/) if you are interested to learn about how to asynchronously execute your operations.
