package org.zenith.app.controllers;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import org.zenith.app.services.TodoService;
import org.zenith.models.TodoItem;

public class HomeController extends BaseController {
    private TodoService todoService;

    @FXML
    public Text remainingTasksLbl;

    @FXML
    public ListView todoListView;

    @FXML
    public void initialize() {
        this.todoService = TodoService.getInstance();
        this.todoService.loadTodos();

        remainingTasksLbl.textProperty().bind(
                Bindings.format("Remaining Tasks: %d",
                        TodoService.getInstance().todosSizeProperty())
        );

        todoListView.setItems(todoService.getTodos());

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