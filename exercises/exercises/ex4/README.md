# Exercise 4 - Consuming the SAP SuccessFactors Goal API using the CAP Remote Services Feature

In this exercise, we will look at how to use the [CAP Remote Services](https://cap.cloud.sap/docs/java/remote-services) feature to connect to the [SAP SuccessFactors Goal API](https://api.sap.com/api/PerformanceandGoalsPMGM/resource/Goal_Weight). 

In the previous exercise, we added functionality to allow a user to register for an event.
What is left now is to add the functionality to create a goal and related tasks in SuccessFactors. 

- [ ] 🔨 **To get a better understanding of the use case log into the demo SuccessFactors system at https://pmsalesdemo8.successfactors.com/ with the credentials provided to you.**
    - Use `SFEDU028064` as the company ID.
    - Use `sfadmin` as the username.
    - Once logged in click on the `My Goals` tile on the home screen.

Now let's enhance the `GoalServiceHandler` to implement the interactions with the SuccessFactors API.

## 4.1 Create a Remote Service Object

Similarly to the OpenAPI consumption discussed in exercise 3 we need to define a service object that we will use to run our OData queries.

- [ ] 🔨 **Add the following field to the `GoalServiceHandler` class:**

    ```java
    @Autowired
    @Qualifier(cds.gen.goal.Goal_.CDS_NAME)
    private CqnService goalService;
    ```

> **Tip:** Treat the package names with caution. Depending on how you name your CDS entities and services as well as how remote services are named there may be naming clashes. 
> 
> In our case the generated API class for the SuccessFactors service is similar to the generated entity class `cds.gen.goalservice.Goal_` of our `GoalServcie`. So the fully qualified class name is shown here.
> 
> If you like feel free to rename the `cds.gen.goalservice.Goal_` entity later as an optional exercise.

## 4.1 - Fetch all learning goals of a user in GoalServiceHandler

First, we'll write a query that fetches all goals of the user. We'll then refine the query to look for the learning goal specifically.

### 4.1.1 Fetch all goals

- [ ] 🔨Extend the `getLearningGoals()` method in `GoalServiceHandler` class as follows:
    ```java
    public List<Goal101> getLearningGoals() {
        var user = helper.getUser();

        var select = Select.from(GOAL101)
                           .where(g -> g.userId().eq(user));
  
        var goals = goalService.run(select).listOf(Goal101.class);

        log.info("Got the following goals from the server:");
        goals.forEach(g -> log.info("ID: {}, Title: {}", g.getId(), g.getName()));
        return goals;
    }
    ```

Take a moment to understand the query we are constructing and running.

We use the CQL Statement builder for [Select](https://cap.cloud.sap/docs/java/query-api#the-cql-statement-builders) queries.

- A select in CQL translates to a read (i.e. an HTTP GET request) in OData.
- The `from` clause determines the entity we read from
- The `where` clause is translated into a `$filter` OData expression
- The `goalService` represents the remote service we run the OData request against
- The `run` command performs the OData request
- Finally, we obtain the parsed response data as `.listOf(Goal101.class)`

> **Tip:** We filter by the `userId` here because a user may have access to goals from other users in addition to their own goals. For example if goals are set to be publicly visible or because a user may have a manager role. But for our use case we are only interested about the user's own goals.

Let's test the code to make sure it functions correctly:

- [ ] 🔨Run the application and head to http://localhost:8080/odata/v4/GoalService/Goal

> **Tip:** This uses endpoints we created for you in the `GoalServiceController` for ease of testing.

You should see an empty list as we haven't created a goal for the user just yet.

- [ ] 🔨Temporarily change the `DEMO_ID` in the `Helper` class to `ID00` and run the application again.

//TODO update this

### 4.1.2 Filter for Learning Goals

Now that we have a working query let's refine it to only fetch learning goals.

- [ ] 🔨 **Add a filter condition such that only goals in the `Learning and Growth` category which aren't completed yet and have a name containing `Learn something at TechEd 2023` are returned.**

<details><summary>Click here to view the solution.</summary>

```java
var select = Select.from(GOAL101)
                   .where(g -> g.userId().eq(user)
                           .and(g.category().eq("Learning and Growth"))
                           .and(g.name().contains("Learn something at TechEd 2023"))
                           .and(g.status().ne("Completed")));
```

</details>

> **Tip:** You can find more information on how to build queries in the [CDS documentation](https://cap.cloud.sap/docs/java/query-api#where-clause).

While this should narrow the results down, there is still one category of goals we want to exclude from our search: Archived goals. 
However, we can't filter for those on the goal objects directly, but instead have to look at one of the navigation properties.
In order to do that we have to expand the navigation property first.
Also, we'll need the `task` navigation property for later, so let's expand that as well.

- [ ] 🔨 **Adjust the select statement to also expand the `permissionNav` and `task` navigation properties.**

<details><summary>Click here to view the solution.</summary>

```java
var select = Select.from(GOAL101)
                   .columns(g -> g._all(),
                            g -> g.tasks().expand(),
                            g -> g.permissionNav().expand()) 
                   .where(g -> g.userId().eq(user)
                           .and(g.category().eq("Learning and Growth"))
                           .and(g.name().contains("Learn something at TechEd 2023"))
                           .and(g.status().ne("Completed")));
```

</details>

> **Tip:** Make sure that while expanding the two navigation properties all other fields of the Goal are still being returned by the server.

Now we can update our code to only return visible goals.

- [ ] 🔨 **Extend the method with the following code:** 
  
   ```java
   var goals = goalService.run(select).listOf(Goal101.class);
   
   var visibleGoals = goals
           .stream()
           .filter(g -> g.getPermissionNav().getView())
           .toList();
   
   log.info("Got the following goals from the server:");
   visibleGoals.forEach(g -> log.info("ID: {}, Title: {}", g.getId(), g.getName()));
   
   return visibleGoals;
   ```

> **Tip:** While we could also include a further `where` statement to filter archived goals out on the server side, the SuccessFactors system we are using does not permit filtering on this property. So we have to filter on the client side instead.


## 4.2 Create a Learning Goal

The first time you try to fetch the goals, you would get an empty list. This is because we haven't created a goal for the user yet. Let's add the functionality to create a goal for the user.

Before we continue though we'll add some configuration to our `Helper` class.

- [ ] 🔨 **Change the value for `DEMO_ID` at the top of the `Helper` class.**

   ```java
   public static final String DEMO_ID = "ID"+"<add your desk number here>";
   ```

This is necessary because all participants will use the same credentials for the SuccessFactors system and we want to make sure that the goals created by each participant are easily distinguishable.

With this preparation out of the way, let's create a goal for the user.

- [ ] 🔨 **Implement the `draftGoal(String user)` method to create a new `Goal101` object with the following properties:**
  - `name`: `ID<your id>: Learn something at TechEd 2023`
  - `metric`: `Attend sessions at TechEd 2023`
  - `weight`: `0.0`
  - `start`: Simply use the current date here
  - `due`: Use the current date plus two weeks
  - `flag`: `0`
  - `type`: `user`
  - `userId`: Use the `user` parameter here
  - `state`: `On Track`
- [ ] 🔨 **Implement the `Goal101 createGoal(String user)` method to create and execute an `Insert` query.**

<details><summary>Click here to view the solution.</summary>

```java
private static Goal101 draftGoal(String user)
{
    var goal = Goal101.create();

    goal.setName(DEMO_ID + ": Learn something at TechEd 2023");
    goal.setMetric("Attend sessions at TechEd 2023");
    goal.setCategory("Learning and Growth");
    goal.setType("user");
    goal.setFlag(0);
    goal.setWeight(0d);
    goal.setUserId(user);
    goal.setState("On Track");
    goal.setStart(LocalDate.now());
    goal.setDue(LocalDate.now().plusDays(14));
    return goal;
}

public Goal101 createGoal(String user)
{
    var draft = draftGoal(user);
    var query = Insert.into(GOAL101).entry(draft);

    var result = goalService.run(query).single(Goal101.class);

    log.info("Created the following Goal in SFSF: {}", result);
    return result;
}
```

The data object classes have a static `.create()` factory method that allows us to build new instances.

We use a CQL Statement builder [Insert](https://cap.cloud.sap/docs/java/query-api#single-insert) to build the insert query.
Again, using the `goalService` remote service object we run the query and parse the response into a `Goal101` object.

</details>

Now it's time to test our code.

- [ ] 🔨 **Test the code by signing up for a TechEd session in the frontend.**
  - Check the application logs to see if the goal was created successfully. 
  - Head to the [SuccessFactors](https://pmsalesdemo8.successfactors.com/) UI to see your created goal. 

// TODO rather test this via UI? Or not test at all yet? we don't want too many goals created in SFSF



## 4.3 - Create a Task

We want to add tasks to an already created goal when a user registers for session. Tasks are represented by the `Task101` entity in SuccessFactors (in the UI you may also see them labeled as "Sub-Goals"). 

Let's add the functionality to create a task for the user.

- [ ] 🔨 **Implement the `void createTask(goal, title)` method to create a task related to the given goal in the remote service.**
  - Use the `GoalTask_101` entity to create a new task
  - Make sure to use the `id` of the goal as `objId` property of the task
  - Chose a number for the `done` percentage property that makes sense to you
  
<details><summary>Click here to view the solution.</summary>
   
```java
public void createTask(Goal101 goal, String title )
{
    var description = "Attend the session '" + title + "' and share what you learned!";
    var task = GoalTask101.create();
    task.setObjId(goal.getId());
    task.setDescription(description);
    task.setDone(10d);

    var insert = Insert.into(GOAL_TASK101).entry(task);
    goalService.run(insert);
}
```

We use an insert statement on the `GoalTask101` entity to create a new task.
</details>

> **Tip:** OData also allows for "deep create" or "deep update" operations. In our case that would mean to create tasks directly into the `/Goal_101/tasks` endpoint when creating or updating the `Goal_101`entity. 
> However, the SuccessFactors API does not allow this for Goal tasks, as they have a dedicated API endpoint. 

- [ ] 🔨 **Test the code by signing up for a TechEd session in the frontend.**
  - Again, check the logs and the UI to see if the task was created successfully. 


## Summary

You've now successfully used CAP Remote services to consume SuccessFactors Goal service.

Below are a few further, optional exercises to learn more. Continue with those or head to the next step: [Exercise 5 - Deploying the Application to SAP Business Technology Platform](../ex5/README.md)

## 4.4 (Optional) Add a Custom HTTP Header to Requests

Sometimes one needs to send additional headers to a system.
The SAP Cloud SDK offers various ways to achieve this.
A relatively easy way is to include the headers in the destination configuration as [documented here](https://sap.github.io/cloud-sdk/docs/java/features/connectivity/destination-service#add-headers-via-destination-properties). 

In our case it would be helpful to disable gzip compression to make the response payload readable in the logs.

- [ ] 🔨 **Adjust the `destinations` environment variable to include the `accept-encoding: identity` header.**

<details><summary>Click here to view the solution.</summary>

```
{"name":"SFSF-BASIC-ADMIN", "url":"https://apisalesdemo8.successfactors.com/", "type": "HTTP", "user": USER, "password":password, "URL.headers.accept-encoding": "identity"}
```

</details>

## 4.5 (Optional) Understanding and Improving the `GoalServiceFilter`

In the `GoalServiceFilter` class we have implemented a filter that is applied to all requests to the `GoalService` endpoint.
The filter is responsible for filtering for the `DEMO_ID` so that each participant only sees their own created goals.

This is achieved by implementing an `EventHandler` for the **_remote service_**:

```java
@Before( entity = Goal101_.CDS_NAME)
public void beforeRemoteGoal(CdsReadEventContext ctx)
```

Effectively we intercept all read requests to the `Goal101` entity and manipulate the query.
Because we are not setting any result, the CAP framework will subsequently call the actual remote service with the modified query.

- [ ] 🔨 **Move the filtering for the userID out of the `GoalServiceHandler` and add it to this before handler instead.**
- [ ] 🔨 **Move the filtering for the view permission out of the `GoalServiceHandler` by creating an `@After` implementation in the `GoalServiceFilter` to achieve the same behaviour.**

//TODO add solution, test this

> **Tip:** Extracting filtering logic like this may also be helpful if you need to turn it on or off depending on other factors. For example, you could annotate this class to only be loaded for specific Spring profiles.
> 
> You can learn more about the different event handling phases in the [CAP documentation](https://cap.cloud.sap/docs/java/provisioning-api#phases).


