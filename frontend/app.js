/* global Vue axios */ //> from vue.html
const $ = (sel) => document.querySelector(sel);

const backend = {
  url: {
    dev: 'http://localhost:8080',
    prod: 'https://sap-cloud-sdk-js-demo-java-srv.cfapps.sap.hana.ondemand.com' //Todo: replace with your own application URL
  }
};
const todoApp = Vue.createApp({
  data() {
    return {
      todos: [],
      selectedUser: null,
      suggestion: { title: '', description: '' }
    };
  },

  methods: {

    checkIfUserIsSelected() {
      if (this.selectedUser == null) {
        alert("Please select a user");
        return false;
      }
      return true;
    },

    async getToDos() {
      console.log("Fetching Todos for: ", this.selectedUser);
      const { data } = await axios.get(
        `${todoService}/ToDo?$orderby=modifiedAt`
      );
      todoApp.todos = data.value;
    },

    async getToDoSuggestion() {
      console.log("Fetching Todo suggestion for: ", this.selectedUser);
      const { data } = await axios.get(
        `${this.getTodoGeneratorService()}/getTodoSuggestion()?user=${this.selectedUser}`
      );
      const { title, description } = data;
      todoApp.suggestion = { title, description };
    },

    getEnv() {
      return location.hostname === 'localhost' ||
        location.hostname === '127.0.0.1'
        ? 'dev'
        : 'prod';
    },

    async quit() {
      if (!this.checkIfUserIsSelected()) return;
      if (!confirm('Are you sure?')) return;
      const { data } = await axios.post(
        `${this.getTodoGeneratorService()}/quit?user=${this.selectedUser}`,
        {},
        { 'content-type': 'application/json' }
      );
      alert(data.value);
      todoApp.suggestion = { title: 'Weekend!', description: 'Properly end the week, party!' }
      await this.getToDos();
    },

    async addTodo() {
      console.log(this.selectedUser);
      if (!this.checkIfUserIsSelected()) return;
      await axios.post(
        `${this.getTodoGeneratorService()}/addTodo?user=${this.selectedUser}`,
        { todo: todoApp.suggestion },
        { 'content-type': 'application/json' }
      );
      await this.getToDos();
      await this.regenerateTodo();
    },

    async regenerateTodo() {
      if (!this.checkIfUserIsSelected()) return;
      await this.getToDoSuggestion();
    },

    async fetchTodosForUser() {
      if (!this.checkIfUserIsSelected()) return;
      await this.getToDoSuggestion();
    },

    getTodoGeneratorService() {
      return `${backend.url[this.getEnv()]
        }/odata/v4/TodoGeneratorService`;
    }

  }
}).mount('#app');

const todoService = todoApp.getEnv() === 'dev' ?
  'http://localhost:4004/odata/v4/ToDoService' : 'https://sap-cloud-sdk-js-demo-todo-server.cfapps.sap.hana.ondemand.com/odata/v4/ToDoService/';
