package com.sap.cloud.sdk.demo.recap23.remote.utility;

import cloudsdk.gen.namespaces.todoentryv2.TodoEntryV2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("odata/v2/")
public class MockTodoService {
    private static final int PRESET_STATUS = 1;
    private static final String PRESET_CATEGORY_LABEL = "Preset Task";
    private static final String PRESET_CATEGORY_ID = "42";
    private static final String[] PRESET_TODOS = {
            "Finish Your Profile",
            "Read All Messages",
            "Respond Open Questions",
            "Take A Break",
            "Complete Learning Exercise",
            "Read Course Requirements"
    };
    private static final int MAX_NUM_START = PRESET_TODOS.length;
    private static final int MIN_NUM_START = 2;

    private final Map<String, List<TodoEntryV2>> store = new ConcurrentHashMap<>();
    private final Random rnd = new Random();

    @GetMapping("TodoEntryV2")
    protected ResponseEntity<?> getTodos(
            @RequestParam(value = "$top", defaultValue = "100") Integer top,
            @RequestParam(value = "$filter", defaultValue = "") String filter
    ) {
        filter = filter.replaceFirst("^userId eq \\W(.*)\\W$", "$1");
        var entries = store.computeIfAbsent(filter, this::createItems);
        return ResponseEntity.ok(
            Collections.singletonMap("d",
                Collections.singletonMap("results",
                    entries.stream().limit(top).toList())));
    }

    @PostMapping("TodoEntryV2")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    protected void postTodo( @RequestBody TodoEntryV2 todo ) {
        var userNav = todo.getCustomFields().remove("userNav");
        if( !(userNav instanceof Map)) {
            throw new IllegalArgumentException("User navigation property must be present.");
        }
        var uri = ((Map<?, ?>) ((Map<?, ?>) userNav).get("__metadata")).get("uri").toString();
        uri = uri.replaceFirst("^.*\\(\\W(.*)\\W\\)$", "$1");
        store.computeIfAbsent(uri, this::createItems).add(todo);
    }

    private List<TodoEntryV2> createItems(String filter) {
        var baseId = Math.abs(filter.hashCode()%1000000);
        var baseDate = ZonedDateTime.now().minusDays(1);
        return IntStream.rangeClosed(MIN_NUM_START, rnd.nextInt(MIN_NUM_START, MAX_NUM_START+1))
            .mapToObj(i -> TodoEntryV2.builder()
                .todoEntryId(new BigDecimal(baseId+i))
                .todoEntryName(PRESET_TODOS[(baseId+i)%PRESET_TODOS.length])
                .lastModifiedDateTime(baseDate.plusHours(i))
                .createdDate(baseDate)
                .categoryId(PRESET_CATEGORY_ID)
                .categoryLabel(PRESET_CATEGORY_LABEL)
                .status(PRESET_STATUS)
                .build())
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
