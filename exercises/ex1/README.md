# Exercise 1 - Understand the Existing Project Setup

To better understand the initial state of the project and where we want to go, let's examine the current state of the application.

## 1.1 Understanding the Project Structure

The project structure is as follows:

```txt
├── srv
│   ├── external
│   │   └── ...  // here we will add the metadata files of the remote services
│   ├── src
│   │   ├── gen
│   │   |   └── ...  // generated Java classes will be here
│   │   ├── main
│   │   |   |── Application.java
│   │   |   |── SignupHandler.java  // the main entry point for our business logic
│   │   |   |── remote
│   │   |   |   └── GoalServiceHandler.java         // handles all communication to SuccessFactors
│   │   |   |   └── RegistrationServiceHandler.java // handles all communication to the Registration API
|   └── service.cds  // here we defined our CDS models and services
|   └── pom.xml  // all our dependencies and plugins are here
├── pom.xml // here we manage our dependency versions
```

There are a few more files present, but the above are mostly what we will be working with during the exercises.

## 1.2 Understanding Service Definitions

Services are one of the core concepts of CAP.
They are declared in [CDS](https://cap.cloud.sap/docs/about/#service-definitions-in-cds) and dispatch events to `Event Handlers`.
Let's examine the [service.cds](../../srv/service.cds) file, which defines the services exposed by our application: It defines two services, `SignupService` and the `GoalService`.

- The `SignupService` is our main entry point to perform our business logic.
- The `GoalService` is just for testing individual aspects of the application later, it is not required for the main use case.

The `SignupService` exposes one action called `signUp`. It takes a `String` session, which is the name of the session a user intends to sign up for.

```cds
@path: 'SignupService'
service SignupService {
  action signUp(session: String) returns String;
}
```

The `@path` argument allows you to provide a custom path for the exposed service.
In this example, we are providing the value _"SignupService"_, which means that this particular service will be available at `{application-hostname}/odata/v4/SignupService/` once our application runs.

Let's understand what artifacts are generated based on the services we defined in the next step. 

## 1.3 CDS Maven Plugin

In your application's [pom.xml](../../srv/pom.xml), under the `plugins` section you can see the [`cds-maven-plugin`](https://cap.cloud.sap/docs/java/assets/cds-maven-plugin-site/plugin-info.html) entry.
The interesting part here is the `generate` goal, which is responsible for scanning project directories for CDS files and generating Java POJOs for type-safe access to the CDS model.

- [ ] 🔨 **From your project's root directory, run `mvn clean compile`.**

You can see artifacts being generated for the services we defined in the `service.cds` under the [srv/src/gen/java/cds/gen](../../srv/src/gen/java/cds/gen) folder.

In order for the IDE to recognise the new directory as source code we need to mark it as such.

- [ ] 🔨**For the IntelliJ IDE: right-click the directory [srv/src/gen/java](../../srv/src/gen/java) and select `Mark Directory as` -> `Generated Sources Root`.**

> **Tip:** The generated sources are excluded from Git by the current `.gitignore` file.
> Generally this is typically a matter of preference and may also depend on how you set up the CI/CD of your project.

## 1.4 Understanding EventHandlers

In a previous section, we learned that `Service`s dispatch events to `Event Handlers`.
Event handlers are the ones that then implement the behaviour of the service.
Let's examine the event handler for the `SignupService` in the file [SignupHandler.java](../../srv/src/main/java/com/sap/cloud/sdk/demo/in260/SignupHandler.java).

- The `@ServiceName(SignupService_.CDS_NAME)` annotation at the top of the class specifies the service, which the event handler is registered on. 

- The `@On` annotation on top of the method `signUp(context)` specifies the `Event Phase` at which the method would be called.
   - An `Event` can be processed in three phases: `Before`, `On`, and `After`. As we are defining the core business logic of the action, we are using the `On` phase.
   - What this means is that everytime the `signUp(session)` action is called, an event is triggered and the `signUp(context)` method is called.

- `Event Contexts` provide a way to access the parameters and return values. `SignUpContext` is the event context here, which helps us to access the action parameter, additional query parameters, and other information of the incoming request.
   It would also be eventually used to set the return value of the action.

- Note that some imports used in the class like `SignupService_` and `SignUpContext` were all generated by the CDS Maven Plugin in the previous step.

Let's try running our application now.

## 1.5 Run Your Application Locally

Before we run our application we'll create destinations for the remote services.

- [ ] 🔨 **Create an environment variable in your terminal window named `destinations` as follows:**

  For CMD:
  ```cmd
  set destinations=[{name: "SFSF-BASIC-ADMIN", url:"http://localhost:8080", User:""}, {name: "Registration-Service", url: "https://ad266-registration.cfapps.eu10-004.hana.ondemand.com/"}]
  ```
  For PowerShell:
  ```ps
  $env:destinations='[{name: "SFSF-BASIC-ADMIN", url:"http://localhost:8080", User:""}, {name: "Registration-Service", url: "https://ad266-registration.cfapps.eu10-004.hana.ondemand.com/"}]'
  ```

> **Note:** In order to test the resilience patterns locally we'll use a locally provided mock server instead of real systems.
> That way we can artificially inject faults in any form we need to test our resilience patterns.
> This is why we have the destination `SFSF-BASIC-ADMIN` with url:`http://localhost:8080` as we have a mock server that runs internally inside our application.

Now run the application.

- [ ] 🔨 **From the root directory of your project, in your IDE's terminal, run `mvn spring-boot:run` to start the application locally.**

Examine the logs of the application, you should see something like this:
```json
INFO 57513 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
INFO 57513 --- [  restartedMain] c.sap.cloud.sdk.demo.in260.Application   : Started Application in 2.348 seconds (process running for 2.759)
```

- [ ] 🔨 **You can now access the application endpoints:**
  - Frontend: http://localhost:8080 
  - Metadata: http://localhost:8080/odata/v4/SignupService/$metadata

You can stop the application by pressing `Ctrl+C` in the terminal.

## Summary

You've now successfully understood the existing files in your project. Let's now go add some code.

Continue to - [Exercise 2 - Use SAP Cloud SDK to make your Application Resilient](../ex2/README.md)
