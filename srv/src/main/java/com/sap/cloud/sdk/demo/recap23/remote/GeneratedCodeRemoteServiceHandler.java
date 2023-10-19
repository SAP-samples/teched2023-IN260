package com.sap.cloud.sdk.demo.recap23.remote;


import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataResponseException;
import com.sap.cloud.sdk.datamodel.odata.helper.batch.BatchResponse;
import com.sap.cloud.sdk.demo.recap23.remote.utility.Helper;

import cds.gen.todoentryv2.TodoEntryV2;
import cloudsdk.gen.namespaces.todoentryv2.batch.TodoEntryV2ServiceBatch;
import cloudsdk.gen.namespaces.todoentryv2.batch.TodoEntryV2ServiceBatchChangeSet;
import cloudsdk.gen.services.DefaultTodoEntryV2Service;
import cloudsdk.gen.services.TodoEntryV2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
@Component("generated")
@Slf4j
public class GeneratedCodeRemoteServiceHandler implements ToDoRemoteServiceHandler {
    private static final TodoEntryV2Service service = new DefaultTodoEntryV2Service();

    private HttpDestination getDestination() {
        return DestinationAccessor.getDestination("TODO_SERVICE").asHttp();
    }

    @Override
    public List<TodoEntryV2> getCurrentToDos(String userName) {
        var destination = getDestination();

        return service.getAllTodoEntryV2().executeRequest(destination)
                .stream()
                .map(this::convertToCdsTodoObject)
                .toList();
    }


    @Override
    public TodoEntryV2 addToDo(TodoEntryV2 todo, String userName) {

        final cloudsdk.gen.namespaces.todoentryv2.TodoEntryV2 vdmTodo = cloudsdk.gen.namespaces.todoentryv2.TodoEntryV2.builder()
                .todoEntryName(todo.getTodoEntryName())
                .status(todo.getStatus())
                .build();

        vdmTodo.setCustomField("userNav", Helper.getUserNav(userName));

        var destination = getDestination();

        var result = service.createTodoEntryV2(vdmTodo).executeRequest(destination).getModifiedEntity();
        return convertToCdsTodoObject(result);
    }

    @Override
    public String quit(String userName){
        var destination = getDestination();

        var todos = service.getAllTodoEntryV2().executeRequest(destination);

        final TodoEntryV2ServiceBatchChangeSet TodoEntryV2ServiceBatchChangeSet = service.batch().beginChangeSet();
        todos.forEach(TodoEntryV2ServiceBatchChangeSet::deleteTodoEntryV2);
        final TodoEntryV2ServiceBatch TodoEntryV2ServiceBatch = TodoEntryV2ServiceBatchChangeSet.endChangeSet();

        // execute & check the results
        int failedDeletes = 0;
        try (BatchResponse batchResponse = TodoEntryV2ServiceBatch.executeRequest(destination)) {
            for( int i = 0; i< todos.size(); i++ ) {
                try{
                    batchResponse.get(i);
                } catch (ODataResponseException e) {
                    log.warn("Failed to delete ToDo from the server: {}", e.getMessage());
                    failedDeletes++;
                }
            }
        }
        String resultMessage = String.format("Deleted %s ToDos from the server.", todos.size() - failedDeletes);
        log.info(resultMessage);
        if( failedDeletes > 0 ) {
            resultMessage += String.format(" Failed to delete %s ToDos from the server.", failedDeletes);
        }
        return resultMessage;
    }

    private TodoEntryV2 convertToCdsTodoObject (cloudsdk.gen.namespaces.todoentryv2.TodoEntryV2 vdmTodoEntryV2) {
        var todo = TodoEntryV2.create();
        todo.setTodoEntryName(vdmTodoEntryV2.getTodoEntryName());
        todo.setStatus(vdmTodoEntryV2.getStatus());
        return todo;
    }
}
