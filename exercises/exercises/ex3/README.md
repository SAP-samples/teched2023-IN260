# Exercise 3 - Consuming the Registration API using the SAP Cloud SDK

The [`SignupHandler`](../../srv/src/main/java/com/sap/cloud/sdk/demo/ad266/SignupHandler.java) is the entry point of the application. And `signUp` is the action that will be called when a user signs up for an event or a session.
The first step to take when a user signs up for an event is to register them for the event.
As discussed in the previous exercise, for registering the user for an event/session, we will use a synthetic remote OpenAPI service.

In this exercise, we will look at adapting the [`RegistrationServiceHandler`](../../srv/src/main/java/com/sap/cloud/sdk/demo/ad266/remote/RegistrationServiceHandler.java) to handle all communication with the remote OpenAPI service.
and take care of registering the user. We would be interacting with a synthetic OpenAPI service to achieve this.

Let's learn how you can leverage the SAP Cloud SDK to consume a remote OpenAPI service.

## 3.1 Familiarising yourself with the remote OpenAPI service

 The OpenAPI service is available at `https://ad266-registration.cfapps.eu10-004.hana.ondemand.com`. For the sake of simplicity, we will assume that you don't have to authenticate yourself to access the service.

1. [ ] Head to https://ad266-registration.cfapps.eu10-004.hana.ondemand.com/api-docs and explore the OpenAPI specification of the service.

The most important endpoints, that we will be consuming in our application are:
   1. `/events`: Lists all the available events.
   2. `/events/{eventId}/register`: Allows you to register for an event.
   3. `/events/{eventId}/sessions/{sessionId}/register`: Allows you to register for a session.

Next, we will use the SAP Cloud SDK to consume this remote OpenAPI service.

## 3.2 Add SAP Cloud SDK to your project and generate a typed OpenAPI client

In order to connect to the remote OpenAPI service we will generate a [typed OpenAPI client](https://sap.github.io/cloud-sdk/docs/java/v5/features/rest/overview).

//TODO adjust initial branch for these depenndency additions

- [ ] 🔨Head to the `<plugin>` section of the `srv/pom.xml` file and add the following plugin configuration:

   ```xml
   <!-- Cloud SDK OData VDM Generator -->
   <plugin>
      <groupId>com.sap.cloud.sdk.datamodel</groupId>
      <artifactId>openapi-generator-maven-plugin</artifactId>
      <version>5.0.0-SNAPSHOT</version>
      <executions>
         <execution>
            <id>generate-registration-service</id>
            <phase>generate-sources</phase>
            <goals>
               <goal>generate</goal>
            </goals>
            <configuration>
               <inputSpec>${project.basedir}/external/registration.json</inputSpec>
               <outputDirectory>${project.basedir}/src/gen/java</outputDirectory>
               <deleteOutputDirectory>false</deleteOutputDirectory>
               <apiPackage>cloudsdk.gen.registrationservice</apiPackage>
               <modelPackage>cloudsdk.gen.registrationservice</modelPackage>
               <compileScope>COMPILE</compileScope>
            </configuration>
         </execution>
      </executions>
   </plugin>
   ```

This maven plugin will generate a set of classes into the `<outputDirectory>`.
Those classes can then be used to build and execute HTTP requests against the registration service.

Take note of the parameters in the `<configuration>` section above:

- `<inputSpec>`: This points to the OpenAPI specification of the remote service which is already included under `external/registration.json` in your project.
- `<outputDirectory>`: The output directory is the directory where the generated classes will be placed. We are using the `src/gen/java` directory of the project to indicate those are generated classes.
- `<apiPackage>` and `<modelPackage>`: The package names for the generated classes.
The input specification file is the OpenAPI specification of the remote service and is already available under `external/registration.json` in your project.

> **Tip**: You can find more details about the plugin parameters [here](https://sap.github.io/cloud-sdk/docs/java/v5/features/rest/generate-rest-client#available-parameters).

Next, we have to add some dependencies to the project to ensure these generated classes can be compiled and used.

- [ ] 🔨Add the following Cloud SDK dependencies to the dependency section of your `srv/pom.xml` file:
   
   ```xml
   <!-- Cloud SDK OpenAPI & Destinations -->
   <dependency>
      <groupId>com.sap.cloud.sdk.datamodel</groupId>
      <artifactId>openapi-core</artifactId>
   </dependency>
   <dependency>
       <groupId>com.sap.cloud.sdk.cloudplatform</groupId>
       <artifactId>connectivity-apache-httpclient5</artifactId>
   </dependency>
   <dependency>
       <groupId>com.sap.cloud.sdk.cloudplatform</groupId>
       <artifactId>cloudplatform-connectivity</artifactId>
   </dependency>
   ```

> **Tip:** We don't need to specify a `<version>` here, because we are already managing the versions of all relevant dependencies via a set of BOMs in the `<dependencyManagement>` section in the root `pom.xml` file.

Now the project is ready to be built.

- [ ] 🔨Compile the application using `mvn compile`.
 
You should see the generated classes under the new `srv/src/gen/java/cloudsdk.gen.registrationservice` directory.

In order for the IDE to recognise the new directory as source code we need to mark it as such.

- [ ] 🔨For the IntelliJ IDE: right-click the directory `srv/src/gen/java` and select `Mark Directory as` -> `Generated Sources Root`.

> **Tip:** The generated sources are excluded from Git by the current `.gitignore` file.
> Generally this is typically a matter of preference and may also depend on how you set up the CI/CD of your project.

In the next step we will use the generated client to  write and run queries for the remote OpenAPI service.

## 3.3 Use the typed client to consume remote OpenAPI service

### 3.3.1 Writing the query

Let's start using the generated client in the `RegistrationServiceHandler`.
The generated code comprises two parts:

- API classes that provide one method for each API operation
- Model classes that represent the data structures used by the API

In our case we have just one API class and two model classes:

- API class: `EventRegistrationApi`
- Model classes: `Event` and `Session`

We'll make use of the API class to obtain the list of available events and select the event we are interested in.

- [ ] 🔨Implement the code using the `EventRegistrationApi` class to get a list of events from the remote service. Add your code to the `getTechEdEvent` method inside the `EventRegistrationApi` class.
- [ ] 🔨Filter the list and return only the single event named `"TechEd 2023"`.

<details> <summary>Click here to view the solution.</summary>

```java
@GetMapping( path = "/rest/v1/getTechEdEvent", produces = "application/json")
public Event getTechEdEvent() {
     var api = new EventRegistrationApi(getDestination());
   
     List<Event> events =  api.getEvents();
 
     return events
         .stream()
         .filter(e -> e.getName().equals("TechEd 2023"))
         .findFirst()
         .orElseThrow();
}
 ```
1. First we create an instance of the API class, passing in a destination object that will inform the API class on how exactly to connect to the remote service.    
2. The `getEvents()` method will perform the actual HTTP request to the `/events` endpoint of the remote service.
3. Finally, we filter for the specific event. Here we make use of the generated model class `Event` to access the `name` property of the event.
</details>

> **Tip:** You'll need to use the `getDestination()` method that is already prepared and will be filled with content in the next step.

### 3.3.2 Using a Destination

In order for the above code to function at runtime we'll need to provide a **_destination_** object to the API class.

> A **_destination_** is a configuration object that contains all the information (e.g. URL, authorization information, additional headers etc.) required to connect to a remote service.

Further resources:
- [BTP Connectivity: Configuring Destinations in the BTP Cockpit](https://help.sap.com/docs/connectivity/sap-btp-connectivity-cf/connectivity-administration?q=Destination%20Service)
- [SAP Cloud SDK: Using Destinations](https://sap.github.io/cloud-sdk/docs/java/features/connectivity/destination-service)

Destinations are typically maintained in the BTP Cockpit and are made available to the application via the [Destination Service](https://api.sap.com/api/SAP_CP_CF_Connectivity_Destination/resource/Find_a_Destination). The service offers a variety of different authentication mechanisms, including connectivity options for on-premise systems.
The SAP Cloud SDK automatically interacts with the Destination Service to load the destination configuration at runtime.

For local testing destinations may also be provided via environment variables.

- [ ] 🔨Create an environment variable in your terminal window named `destinations` as follows:

  For CMD:
  ```cmd
  set destinations=[{name: "Registration-Service", url: "https://ad266-registration.cfapps.eu10-004.hana.ondemand.com/"}]
  ```
  For PowerShell:
  ```ps
  $env:destinations='[{name: "Registration-Service", url: "https://ad266-registration.cfapps.eu10-004.hana.ondemand.com/"}]'
  ```
  
> **Tip:** If you prefer to run the application from within your IDE, you can also configure the environment variable in the IDE. For example, in IntelliJ you can achieve this via the `Run` -> `Edit Configurations` menu.

Now we can replace the stub of `getDestination()` in `RegistrationServiceHandler` to actually load and return the destination.

- [ ] 🔨Leverage the `DestinationAccessor` class to load the destination by its name.

<details> <summary>Click here to view the solution.</summary>

```java
private Destination getDestination() {
    return DestinationAccessor.getDestination("Registration-Service");
}
```

</details>

With these changes in place we can now run the application and test the endpoint.

- [ ] 🔨Run the application with `mvn spring-boot:run` or from within your IDE.
- [ ] 🔨Test the endpoint `http://localhost:8080/rest/v1/getTechEdEvent` in your browser or via `curl` from your terminal.
  - [ ] 🔨Compare that it returns the same result as provided by the remote service at `https://ad266-registration.cfapps.eu10-004.hana.ondemand.com/events/1`. 

> **Tip:** Inspect the application logs to see more details on what is happening under the hood while loading the destination and calling the registration service.

## 3.4 Completing the Registration Flow

Now that we successfully implemented our first remote services call let's complete the registration flow.

To recap: We want to register our user for an event and associated sessions.
This is already sketched out in the `register(String session)` method of the `SignupHandler` class.

- [ ] 🔨Implement the logic for `signUpForTechEd()` and `signUpForSession(String sessionName)` in the `RegistrationServiceHandler` class.
  - Make use of the `EventRegistrationApi` as in the previous exercise
  - For now we'll always assume the user is signing up for TechEd
  - If none of the TechEd sessions match the `sessionName` we should throw an exception
  - Tip: You can invoke registrations as often as you like, there is no actual state change on the server side.
  - Tip: You can add `@GetMapping( path = "/rest/v1/<methodName>")` to the methods to invoke them individually via your browser.

<details> <summary>Click here to view the solution.</summary>

```java
public void signUpForTechEd() {
    var event = getTechEdEvent();
    var api = new EventRegistrationApi(getDestination());
    api.registerForEvent(event.getId()); 
}

public void signUpForSession(String sessionName) {
    var event = getTechEdEvent();

    var api = new EventRegistrationApi(getDestination());
        
    var session = api.getSessions(event.getId())
        .stream()
        .filter(s -> s.getTitle().equalsIgnoreCase(sessionName))
        .findFirst()
        .orElseThrow();

    api.registerForSession(event.getId(), session.getId());
}
```

</details>

> **Tip:** You may be tempted to extract the `api` variable or the result of the `getDestination()` call to a field of the class. However, this is generally not recommended. 
> The reason is that the destination objects often have state attached and can expire. For example, if the authentication is OAuth based, an attached JWT token will expire after some time.
> 
> So it is recommended to always obtain a fresh destination object before making a remote call. Don't worry, the SAP Cloud SDK caches the destination objects internally, so this does not come at a performance loss. You can read more about the caching strategy [here](https://sap.github.io/cloud-sdk/docs/java/features/connectivity/destination-service#configuring-caching-when-querying-the-destination-service-on-cloud-foundry).

- [ ] 🔨(optional) Verify the solution works by running `curl -XPOST localhost:8080/odata/v4/SignupService/signUp` in your terminal.

## Summary

You've now successfully learned how to use the SAP Cloud SDK to consume a remote OpenAPI service in a type safe manner.

Continue to - [Exercise 4 - Consuming the SAP SuccessFactors Goal API using the CAP Remote Services Feature](../ex4/README.md)
