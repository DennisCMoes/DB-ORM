package org.zenith.app.services;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.zenith.models.TodoItem;
import org.zenith.util.EntityManager;

import java.sql.SQLException;
import java.util.List;

public class TodoService {
    private static TodoService instance;
    private ObservableList<TodoItem> todos;
    private EntityManager entityManager;

    private IntegerProperty todosSizeProperty;

    private TodoService() {
        this.entityManager = new EntityManager();
        this.todos = FXCollections.observableArrayList();

        this.todosSizeProperty = new SimpleIntegerProperty(todos.size());
        this.todos.addListener((ListChangeListener<? super TodoItem>) observable -> todosSizeProperty.set(todos.size()));
    }

    public static TodoService getInstance() {
        if (instance == null) {
            instance = new TodoService();
        }

        return instance;
    }

    public void loadTodos() {
        try {
            List<TodoItem> todoList = entityManager.list(TodoItem.class);
            this.todos.setAll(todoList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ObservableList<TodoItem> getTodos() {
        return todos;
    }

    public IntegerProperty todosSizeProperty() {
        return todosSizeProperty;
    }

    public void addTodo(TodoItem todoItem) throws SQLException, NoSuchFieldException, IllegalAccessException {
        todos.add(todoItem);
        entityManager.save(todoItem);
    }

    public void deleteTodo(TodoItem todoItem) throws SQLException, NoSuchFieldException, IllegalAccessException {
        todos.remove(todoItem);
        entityManager.delete(todoItem);
    }

    public void updateTodo(TodoItem todoItem) throws SQLException, NoSuchFieldException, IllegalAccessException {
         entityManager.update(todoItem);
    }
}
