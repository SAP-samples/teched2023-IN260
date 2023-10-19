/*
package com.sap.cloud.sdk.demo.recap23.remote;

import cds.gen.TodoEntryV2.Todo;
import cds.gen.TodoEntryV2.TodoEntryV2;
import cds.gen.todoservice.ToDo;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.datamodel.odata.client.ODataProtocol;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataResponseException;
import com.sap.cloud.sdk.datamodel.odata.client.request.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("lowLevel")
@Slf4j
public class LowLevelRemoteServiceHandler implements ToDoRemoteServiceHandler {
    //Todo: fix the service path
    private static final String path = "/odata/v2/TodoEntryV2Service/";

    private HttpClient getHttpClient() {
        final HttpDestination destination = DestinationAccessor.getDestination("TODO_SERVICE").asHttp();
        return HttpClientAccessor.getHttpClient(destination);
    }


    @Override
    public List<TodoEntryV2> getCurrentToDos() {
        var httpClient = getHttpClient();

        var result = new ODataRequestRead(path, "ToDo", null, ODataProtocol.V4)
                .execute(httpClient);

        return result.asListOfMaps()
                .stream()
                .map(this::convertToCdsTodoObject)
                .toList();
    }

    private Todo convertToCdsTodoObject(Map<String, Object> map) {
        var todo = ToDo.create();
        todo.setId(String.valueOf(map.get(ToDo.ID)));
        todo.setTitle((String) map.get(ToDo.TITLE));
        todo.setDescription((String) map.get(ToDo.DESCRIPTION));
        todo.setStatus((String) map.get(ToDo.STATUS));
        return todo;
    }


    @Override
    public TodoEntryV2 addToDo(TodoEntryV2 todo) {
        var httpClient = getHttpClient();

        var result = new ODataRequestCreate(path, "ToDo", todo.toJson(), ODataProtocol.V4)
                .execute(httpClient);

        return result.as(TodoEntryV2.class);
    }

    @Override
    public String quit() {
        // Get current ToDos
        List<TodoEntryV2> currentToDos = getCurrentToDos();

        // prepare a batch request
        ODataRequestBatch batch = new ODataRequestBatch(path, ODataProtocol.V4);

        // prepare a delete request for each ToDo
        final List<ODataRequestDelete> deletes = currentToDos.stream()
                .map(ToDo::getId)
                .map(id -> new ODataEntityKey(ODataProtocol.V4).addKeyProperty(ToDo.ID, id))
                .map(key -> new ODataRequestDelete(path, "ToDo", key, null, ODataProtocol.V4))
                .peek(request -> batch.beginChangeset().addDelete(request).endChangeset())
                .toList();

        // execute the batch request
        HttpClient httpClient = getHttpClient();
        ODataRequestResultMultipartGeneric result = batch.execute(httpClient);

        // check the results
        int failedDeletes = 0;
        for (ODataRequestDelete delete : deletes) {
            try {
                result.getResult(delete);
            } catch (ODataResponseException e) {
                log.warn("Failed to delete ToDo under {} from the server.\n{}", delete.getRelativeUri(), e.getMessage());
                failedDeletes++;
            }
        }

        String resultMessage = String.format("Deleted %s ToDos from the server.", deletes.size() - failedDeletes);
        log.info(resultMessage);
        if (failedDeletes > 0) {
            resultMessage += String.format(" Failed to delete %s ToDos from the server.", failedDeletes);
        }
        return resultMessage;
    }
}
*/
