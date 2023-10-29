package com.sap.cloud.sdk.demo.in260.remote;

import cds.gen.goal.Goal_;
import cds.gen.goal.Goal101;
import cds.gen.goal.GoalTask101;
import com.sap.cds.Result;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.cqn.CqnDelete;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cloud.sdk.demo.in260.utility.Helper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static cds.gen.goal.Goal_.GOAL101;
import static cds.gen.goal.Goal_.GOAL_TASK101;
import static com.sap.cloud.sdk.demo.in260.utility.Helper.DEMO_ID;

@Slf4j
@Component
public class GoalServiceHandler implements EventHandler
{
    @Autowired
    @Qualifier(Goal_.CDS_NAME)
    private CqnService goalService;

    @Autowired
    private Helper helper;

    public List<Goal101> getLearningGoals()
    {
        var user = helper.getUser();

        var select = Select.from(GOAL101)
                .columns(
                        g -> g._all(),
                        g -> g.tasks().expand(),
                        g -> g.permissionNav().expand())
                .where(
                        g -> g.category().eq("Learning and Growth")
                        .and(g.name().contains("Learn something at TechEd 2023"))
                        .and(g.state().ne("Completed"))
                        .and(g.userId().eq(user)));

        var goals = goalService.run(select).listOf(Goal101.class);

        var visibleGoals = goals
                .stream()
                .filter(g -> g.getPermissionNav().getView())
                .toList();

        log.info("Got the following goals from the server:");
        visibleGoals.forEach(g -> log.info("ID: {}, Title: {}", g.getId(), g.getName()));

        return visibleGoals;
    }

    public Goal101 getLearningGoal() {
        return getLearningGoals().stream().findFirst().orElse(null);
    }

    public Goal101 createGoal( ) {
        return createGoal(helper.getUser());
    }

    public Goal101 createGoal(String user)
    {
        var draft = draftGoal(user);
        var query = Insert.into(GOAL101).entry(draft);

        var result = goalService.run(query).single(Goal101.class);

        log.info("Created the following Goal in SFSF: {}", result);
        return result;
    }

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

    public Result deleteGoal(CqnDelete delete){
        return goalService.run(delete);
    }

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
}
