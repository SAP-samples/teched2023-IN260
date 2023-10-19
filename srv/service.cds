using { managed } from '@sap/cds/common';

@path: 'TodoGeneratorService'
service TodoGeneratorService {
    type GeneratedTodo {
            title : String;
            description : String;
        }
    function getTodoSuggestion() returns GeneratedTodo;
    action addTodo(todo: GeneratedTodo);
    action quit() returns String;
}

