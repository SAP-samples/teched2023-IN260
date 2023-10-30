[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/teched2023-AD266)](https://api.reuse.software/info/github.com/SAP-samples/teched2023-AD266)

# IN260 - Resilient Integration of SAP Solutions on SAP BTP

## Description

This repository contains the material for the SAP TechEd 2023 session IN260 - Resilient Integration of SAP Solutions on SAP BTP.

## Overview

This session dives into how tools such as SAP Cloud Application Programming Model, SAP Cloud SDK, and others can be used to develop applications that connect to SAP applications of any kind through APIs.

SAP Cloud Application Programming Model or [CAP](https://cap.cloud.sap/docs/) is a framework of languages, libraries, and tools for building enterprise-grade services and applications.
It guides developers through proven best practices and accelerates the development process.

[SAP Cloud SDK](https://sap.github.io/cloud-sdk/docs/overview/overview-cloud-sdk) is a set of libraries and tools for developers to build cloud-native applications on the SAP Business Technology Platform (SAP BTP).
CAP internally uses the SAP Cloud SDK for service consumption.

Over the course of this workshop, you will create an application that consumes both an OpenAPI and an OData service.

For the OData part, you will learn how to fetch data from SAP SuccessFactors [Goal Plan API](https://api.sap.com/api/PerformanceandGoalsPMGM/overview) by using CAP's [Remote Services](https://cap.cloud.sap/docs/java/remote-services#configuring-remote-services).
You will also add functionality to create goals and sub-goals and delete them by interacting with the SuccessFactors service via the application.

For the OpenAPI part, you will learn how to use the SAP Cloud SDK to conveniently interact with the service in a type-safe manner.

You could then follow similar steps to connect to any other SAP application that exposes an API and easily build extensions for them in the cloud.

## Requirements

The requirements to follow the exercises in this repository are:

- Java 17
- Maven 3.9
- Node 18+
- NPM 9+
- cdsdk 7.0+
- cf cli 7.7+

## Exercises

// TODO: update once structure is final

- [Getting Started](exercises/ex0/)
  - [Prerequisites](exercises/ex0#prerequisites)
  - [Understanding the Use Case](exercises/ex0#understanding-the-use-case)
- [Exercise 1 - Understand the Existing Project Setup](exercises/ex1/)
  - [Exercise 1.1 - Understanding the Project Structure](exercises/ex1#11-understanding-the-project-structure)
  - [Exercise 1.2 - Understanding Service Definitions](exercises/ex1#12-understanding-service-definitions)
  - [Exercise 1.3 - CDS Maven Plugin](exercises/ex1#13-cds-maven-plugin)
  - [Exercise 1.4 - Understanding EventHandlers](exercises/ex1#14-understanding-eventhandlers)
  - [Exercise 1.5 - Run your application locally](exercises/ex1#15-run-your-application-locally)
- [Exercise 2 - Use SAP Cloud SDK to make your Application Resilient](exercises/ex2/)
  - [Exercise 2.1 - Add the Required Dependencies to Your Project](exercises/ex2#21---add-the-required-dependencies-to-your-project)
  - [Exercise 2.2 - Use the Resilience API](exercises/ex2#22---use-the-resilience-api)
  - [Exercise 2.3 - Locally Test the Resilience Patterns](exercises/ex2#23---locally-test-the-resilience-patterns)
  - [Exercise 2.4 - Use the Retry Pattern](exercises/ex2#24---use-the-retry-pattern)
  - [Exercise 2.5 - Use the Rate-Limiter Pattern](exercises/ex2#25---use-the-rate-limiter-pattern)
- [Exercise 3 - Caching with the SAP Cloud SDK](exercises/ex3/)
  - [Exercise 3.1 - Add the Required Dependencies to Your Project](exercises/ex3#31---add-the-required-dependencies-to-your-project)
  - [Exercise 3.2 - Create a Cache Configuration](exercises/ex3#32---create-a-cache-configuration)
  - [Exercise 3.3 - Apply the Cache Configuration](exercises/ex3#33---apply-the-cache-configuration)
  - [Exercise 3.4 (Optional) - Access the Created Cache](exercises/ex3#34---access-the-created-cache)
- [Exercise 4 - (Optional) Asynchronous Operations](exercises/ex4/)
  - [Exercise 4.1 - Making the SuccessFactors Update Asynchronous](exercises/ex4#41-making-the-successfactors-update-asynchronous)
  - [Exercise 4.2 - The Problem with this Approach](exercises/ex4#42-the-problem-with-this-approach)
  - [Exercise 4.3 - How to Fix the Problem](exercises/ex4#43-how-to-fix-the-problem)
- [Exercise 5 (Optional) - Deploying the application to SAP Business Technology Platform](exercises/ex5/)
  - [Exercise 5.1 - Creating a Destination for SuccessFactors API Endpoint and the Synthetic OpenAPI Service](exercises/ex5#51-creating-a-destination-for-successfactors-api-endpoint-and-the-synthetic-openapi-service)
  - [Exercise 5.2 - Creating a Destination Service Instance](exercises/ex5#52-creating-a-destination-service-instance)
  - [Exercise 5.3 Adjusting the Deployment Descriptor - manifest.yml](exercises/ex5#53-adjusting-the-deployment-descriptor---manifestyml)
  - [Exercise 5.4 Deploy the Application and Test](exercises/ex5#54-deploy-the-application-and-test)
- [Share your feedback](https://github.com/SAP-samples/teched2023-IN260/issues/new/choose)

## Useful Links
- [SAP Customer Influence](https://influence.sap.com/sap/ino/#/campaign/1175) for SAP S/4HANA Cloud APIs.

## Contributing
Please read the [CONTRIBUTING.md](./CONTRIBUTING.md) to understand the contribution guidelines.

## Code of Conduct
Please read the [SAP Open Source Code of Conduct](https://github.com/SAP-samples/.github/blob/main/CODE_OF_CONDUCT.md).

## How to obtain Support

Support for the content in this repository is available during the actual time of the online session for which this content has been designed. Otherwise, you may request support via the [Issues](../../issues) tab.

## License
Copyright (c) 2023 SAP SE or an SAP affiliate company. All rights reserved. This project is licensed under the Apache Software License, version 2.0 except as noted otherwise in the [LICENSE](LICENSES/Apache-2.0.txt) file.
