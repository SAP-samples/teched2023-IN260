# Getting Started - Preparation

Before getting started with the exercise, let's check if you have all the prerequisites in place and understand the use case we intend to build an application for.

## Prerequisites

- Please open an IDE of your choice and clone [this repository](https://github.com/SAP-samples/teched2023-AD266) to your local machine and checkout to `initial-state` branch. We recommend using IntelliJ IDEA.
   <details> 
   <summary>The necessary software for following the exercises is already installed in your systems.</summary>
   If you want you could confirm the installations by running the following commands in your terminal:
   
   - Java 17
      ```shell
         java -version
      ```
   - Maven 3.9+ 

     ```shell 
        mvn -version
     ```
   - Node 18+

     ```shell
        node --version
     ```
   - Npm 9+

     ```shell
        npm --version
     ```
   - cdsdk 7.0+

     ```shell
        cds --version
     ```
   - cf cli 7.7+

     ```shell
        cf --version
     ```
   </details>


- Make sure you have a SAP BTP trial account. If you don't have one, you can create one [here](https://developers.sap.com/tutorials/hcp-create-trial-account.html).

## Understanding the use case

We want to build an application that helps users to sign up for an event and sessions of the event.

When the user signs up for an event the following things should happen:
- The user should get registered for the event. 
- A learning goal should be automatically created for them in SuccessFactors. 
- Any subsequent sessions that a user signs up for should also be registered and added as sub-goals to the created goal.

Based on the use case we need components to handle signing up, registration, and goal creation.
As goal creation involves consuming a SuccessFactors service, let's first get started with setting up things required to consume the SuccessFactors Goal Plan service.

> **Note:** We are using a pre-existing project for this tutorial which is a Spring Boot CAP application.

Continue to - [Exercise 1 - Get and Import SuccessFactors Goal Plan Service](../ex1/README.md)
