package org.zenith.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import org.zenith.models.TodoItem;
import org.zenith.util.EntityManager;
import org.zenith.util.SQLGenerator;
import org.zenith.util.SQLiteDatabase;

import java.util.List;

public class HomeController extends BaseController {
    private EntityManager entityManager;
    private List<TodoItem> todos;

    @FXML
    public Text remainingTasksLbl;

    @FXML
    public ListView todoListView;

    @FXML
    public void initialize() {
        this.entityManager = new EntityManager();

        try {
            todos = entityManager.list(TodoItem.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        remainingTasksLbl.setText(String.format("Remaining Tasks: %d", todos.size()));

        ObservableList<TodoItem> observableTodos = FXCollections.observableList(todos);
        todoListView.setItems(observableTodos);

        todoListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TodoItem selectedTodo = (TodoItem) todoListView.getSelectionModel().getSelectedItem();
                if (selectedTodo != null) {
                    System.out.println("PRESSED");
                    super.openNewWindow("detail-view.fxml", selectedTodo.title, controller -> {
                        if (controller instanceof DetailController) {
                            ((DetailController) controller).setTodoItem(selectedTodo);
                        }
                    });
                }
            }
        });
    }
}