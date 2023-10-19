package com.sap.cloud.sdk.demo.recap23.remote;

import cds.gen.todoentryv2.TodoEntryV2Model_;
import cds.gen.todoentryv2.TodoEntryV2_;
import com.google.gson.JsonObject;
import com.sap.cds.ql.Delete;
import com.sap.cds.ql.Insert;
import com.sap.cds.ql.Select;
import com.sap.cds.services.cds.CqnService;

import cds.gen.todoentryv2.TodoEntryV2;
import com.sap.cloud.sdk.demo.recap23.remote.utility.Helper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("cqn")
@Slf4j
public class CqnRemoteServiceHandler implements ToDoRemoteServiceHandler {

    @Autowired
    @Qualifier(TodoEntryV2Model_.CDS_NAME)
    private CqnService cqnToDoService;


    @Override
    public List<TodoEntryV2> getCurrentToDos(String userName) {
        var query = Select.from(TodoEntryV2_.class).limit(10);

        var toDos = cqnToDoService.run(query).listOf(TodoEntryV2.class);

        log.info("Got the following ToDos from the server: {}", toDos);
        return toDos;
    }


    @Override
    public TodoEntryV2 addToDo(TodoEntryV2 toDo,String userName) {
        final Object userNav = Helper.getUserNav(userName);
        toDo.put("userNav",userNav);
        var query = Insert.into(TodoEntryV2_.class).entry(toDo);

        return cqnToDoService.run(query).single(TodoEntryV2.class);
    }




    @Override
    public String quit(String userName) {
        var todos = getCurrentToDos(userName);

        // delete all current todos
        var deleteRequests = todos.stream()
                .map(TodoEntryV2::getCategoryId)
                .map(id -> Delete.from(TodoEntryV2_.class).byId(id))
                .toList();

        int failedDeletes = 0;
        for (Delete<TodoEntryV2_> deleteRequest : deleteRequests) {
            try {
                cqnToDoService.run(deleteRequest);
            } catch (Exception e) {
                log.warn("Failed to delete ToDo from the server: {}", e.getMessage());
                failedDeletes++;
            }
        }

        String resultMessage = String.format("Deleted %s ToDos from the server.", deleteRequests.size() - failedDeletes);
        log.info(resultMessage);
        if (failedDeletes > 0) {
            resultMessage += String.format(" Failed to delete %s ToDos from the server.", failedDeletes);
        }
        return resultMessage;
    }
}
