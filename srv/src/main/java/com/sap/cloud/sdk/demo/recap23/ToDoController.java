package com.sap.cloud.sdk.demo.recap23;

import cds.gen.todoentryv2.TodoEntryV2;
import cds.gen.todogeneratorservice.AddTodoContext;
import cds.gen.todogeneratorservice.GeneratedTodo;
import cds.gen.todogeneratorservice.GetTodoSuggestionContext;
import cds.gen.todogeneratorservice.QuitContext;
import cds.gen.todogeneratorservice.TodoGeneratorService_;

import com.sap.cds.services.EventContext;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cloud.sdk.demo.recap23.remote.ToDoRemoteServiceHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ServiceName(TodoGeneratorService_.CDS_NAME)
public class ToDoController implements EventHandler {

    @Autowired
    @Qualifier("cqn")
    private ToDoRemoteServiceHandler handler;

    @On( event = GetTodoSuggestionContext.CDS_NAME)
    public void getTodoSuggestion(final GetTodoSuggestionContext context)
    {
        // ToDo: actually get the list of ToDos from the remote service
        List<TodoEntryV2> toDos = handler.getCurrentToDos(extractUser(context));

        // Do some magic to combine existing todos into a suggestion for a new task
        GeneratedTodo suggestion = generateTodoSuggestion(toDos);

        context.setResult(suggestion);
    }

    private static GeneratedTodo generateTodoSuggestion(List<TodoEntryV2> input) {
        // TODO: Implement sophisticated algorithm to generate a ToDo suggestion based on the existing ToDos
        var result = GeneratedTodo.create();
        result.setTitle("Hello World!");
        if( input.isEmpty() ) {
            result.setDescription("Write a 'Hello World!' application with CAP Java.");
        } else {
            result.setDescription("Write a 'Hello World!' application with CAP Java, then finish the other " + input.size() + " ToDos ;)");
        }
        return result;
    }


    @On( event = AddTodoContext.CDS_NAME)
    public void addSuggestedToDo(final AddTodoContext context)
    {
        final TodoEntryV2 toDo = TodoEntryV2.create();


        toDo.setTodoEntryName(context.getTodo().getTitle());
        toDo.setStatus(3);

        handler.addToDo(toDo, extractUser(context));

        context.setCompleted();
    }



    @On(event = QuitContext.CDS_NAME)
    public void quit( QuitContext context ) {
        final String result = handler.quit(extractUser(context));
        context.setResult(result);
    }

    private String extractUser(final EventContext context) {
        String user = context.getParameterInfo().getQueryParameter("user");
        if ( user == null ) {
            user = "sfadmin";
        }
        return user;
    }
}
