package com.sap.cloud.sdk.demo.in260.utility;

import cloudsdk.gen.namespaces.goal.GoalPermission_101;
import cloudsdk.gen.namespaces.goal.GoalTask_101;
import cloudsdk.gen.namespaces.goal.Goal_101;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationHeaderProvider;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationRequestContext;
import com.sap.cloud.sdk.cloudplatform.connectivity.Header;
import com.sap.cloud.sdk.cloudplatform.requestheader.RequestHeaderAccessor;

@RestController
@RequestMapping("odata/v2/")
public class MockGoalService {
    private final Map<String, List<Goal_101>> goals = new ConcurrentHashMap<>();
    private final Map<Long, List<GoalTask_101>> tasks = new ConcurrentHashMap<>();
    private final Random rnd = new Random();

    public static class FlakyHeaderProvider implements DestinationHeaderProvider {
        @Nonnull
        @Override
        public List<Header> getHeaders(@Nonnull DestinationRequestContext context ) {
            if ( !context.getDestination().get(DestinationProperty.NAME).contains("SFSF-BASIC-ADMIN")){
                return Collections.emptyList();
            }
            var result = new ArrayList<Header>();
            var headers = RequestHeaderAccessor.getHeaderContainer();
            headers.getHeaderValues("delay").forEach(s -> result.add(new Header("delay", s)));
            headers.getHeaderValues("fault").forEach(s -> result.add(new Header("fault", s)));
            return result;
        }
    }

    @GetMapping("Goal_101")
    protected ResponseEntity<?> getGoals(
            @RequestParam(value = "$top", defaultValue = "100") Integer top,
            @RequestParam(value = "$filter", defaultValue = "") String filter,
            @RequestHeader HttpHeaders headers
    ) {
        var userId = filter.replaceFirst(".*userId eq \\W(.*?)\\W.*", "$1");
        flaky(headers);
        var entries = goals.computeIfAbsent(userId, id -> new ArrayList<>());
        return ResponseEntity.ok(
                Collections.singletonMap("d",
                        Collections.singletonMap("results",
                                entries.stream().limit(top).peek(this::augmentTasks).toList())));
    }

    @GetMapping("Goal_101({id})")
    protected ResponseEntity<?> getGoal( @PathVariable String id,
                                         @RequestHeader HttpHeaders headers ) {
        var numMach = Pattern.compile("\\d+(\\.[fd]\\d+)?").matcher(id);
        if(!numMach.find()) {
            throw new IllegalArgumentException("Invalid id.");
        }
        // flaky(headers);
        var num = Long.valueOf(numMach.group());
        var goal = goals.values().stream().flatMap(List::stream).filter(g -> num.equals(g.getId())).findFirst();
        if(goal.isEmpty()) {
            throw new IllegalArgumentException("Goal not found.");
        }
        augmentTasks(goal.get());
        return ResponseEntity.ok(Collections.singletonMap("d", goal.get()));
    }

    @PostMapping("Goal_101")
    protected ResponseEntity<?> postGoal( @RequestBody Goal_101 goal,
                                          @RequestHeader HttpHeaders headers ) {
        var userId = goal.getUserId();
        if(userId==null) {
            throw new IllegalArgumentException("Missing userId.");
        }
        // flaky(headers);
        var goals = this.goals.computeIfAbsent(userId, id -> new ArrayList<>());
        var baseId = Math.abs(userId.hashCode()%1000000L);
        goal.setId(baseId+goals.size());
        goal.setPermissionNav(GoalPermission_101.builder().view(true).build());
        goals.add(goal);
        return ResponseEntity.ok(Collections.singletonMap("d", goal));
    }

    @DeleteMapping("Goal_101({id})")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    protected void deleteGoal( @PathVariable String id,
                               @RequestHeader HttpHeaders headers ) {
        var numMach = Pattern.compile("\\d+(\\.[fd]\\d+)?").matcher(id);
        if(!numMach.find()) {
            throw new IllegalArgumentException("Invalid id.");
        }
        // flaky(headers);
        var num = Long.valueOf(numMach.group());
        goals.values().forEach(list -> list.removeIf(goal -> num.equals(goal.getId())));
    }

    @PostMapping("GoalTask_101")
    protected ResponseEntity<?> postTask( @RequestBody GoalTask_101 task,
                                          @RequestHeader HttpHeaders headers ) {
        var goalId = task.getObjId();
        if(goalId==null) {
            throw new IllegalArgumentException("Missing objId.");
        }
        // flaky(headers);
        var tasks = this.tasks.computeIfAbsent(goalId, id -> new ArrayList<>());
        var baseId = Math.abs(goalId.hashCode()%1000000L);
        task.setId(baseId+tasks.size());
        tasks.add(task);
        return ResponseEntity.ok(Collections.singletonMap("d", task));
    }

    @DeleteMapping("GoalTask_101({id})")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    protected void deleteTask( @PathVariable String id,
                               @RequestHeader HttpHeaders headers ) {
        var numMach = Pattern.compile("\\d+(\\.[fd]\\d+)?").matcher(id);
        if(!numMach.find()) {
            throw new IllegalArgumentException("Invalid id.");
        }
        // flaky(headers);
        var num = Long.valueOf(numMach.group());
        tasks.values().forEach(list -> list.removeIf(t -> num.equals(t.getId())));
    }

    private void augmentTasks(Goal_101 g) {
        var tasks = this.tasks.getOrDefault(g.getId(), Collections.emptyList());
        g.setCustomField("tasks", Collections.singletonMap("results", tasks));
    }

    private void flaky(Map<String,List<String>> headers) {
        Optional.ofNullable(headers.get("delay")).ifPresent(v -> {
            try { Thread.sleep(Integer.parseInt(v.get(0))); } catch (InterruptedException e) { /* delay interrupted */ }
        });

        Optional.ofNullable(headers.get("fault")).ifPresent(v -> {
            if( rnd.nextInt(100)<Integer.parseInt(v.get(0))) { throw new RuntimeException("Flaky service."); }
        });
    }
}

