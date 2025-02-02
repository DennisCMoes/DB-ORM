package org.zenith.app.controllers;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.zenith.app.services.TodoService;
import org.zenith.models.TodoItem;

import java.sql.SQLException;

public class HomeController extends BaseController {
    private TodoService todoService;

    @FXML
    public Text remainingTasksLbl;

    @FXML
    public ListView<TodoItem> todoListView;

    @FXML
    public Button addTodoBtn;

    @FXML
    public Button deleteTodoBtn;

    @FXML
    public void initialize() {
        this.todoService = TodoService.getInstance();
        this.todoService.loadTodos();

        remainingTasksLbl.textProperty().bind(
                Bindings.format("Remaining Tasks: %d",
                        TodoService.getInstance().todosSizeProperty())
        );

        todoListView.setItems(todoService.getTodos());

        deleteTodoBtn.disableProperty().bind(
                Bindings.isEmpty(todoListView.getSelectionModel().getSelectedItems())
        );

        todoListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TodoItem selectedTodo = todoListView.getSelectionModel().getSelectedItem();
                if (selectedTodo != null) {
                    System.out.println("PRESSED");
                    super.openNewWindow("detail-view.fxml", selectedTodo.title, controller -> {
                        if (controller instanceof DetailController detailController) {
                            detailController.setTodoItem(selectedTodo);
                        }
                    });
                }
            }
        });

        // Wait until the scene is set up
//        Platform.runLater(() -> {
//            if (todoListView.getScene() != null) {
//                todoListView.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleClickOutside);
//            }
//        });
    }

    private void handleClickOutside(MouseEvent event) {
        Node source = event.getTarget() instanceof Node ? (Node) event.getTarget() : null;

        if (source != null) {
            Point2D clickLocation = source.localToScene(source.getLayoutBounds().getMinX(), source.getLayoutBounds().getMinY());

            if (!todoListView.contains(clickLocation)) {
                todoListView.getSelectionModel().clearSelection();
            }
        }
    }

    @FXML
    public void onAddButtonClick() {
        super.openNewWindow("detail-view.fxml", "New todo", controller -> {
            if (controller instanceof DetailController detailController) {
                detailController.setTodoItem(null);
            }
        });
    }

    @FXML
    public void onDeleteButtonClick() {
        try {
            TodoItem selectedTodo = todoListView.getSelectionModel().getSelectedItem();

            if (selectedTodo != null) {
                boolean confirmDeletion = super.showConfirmationDialog(
                        Alert.AlertType.CONFIRMATION,
                        "Delete TODO",
                        "Are you sure you want to delete this todo?",
                        "Delete",
                        "Cancel");

                if (confirmDeletion) {
                    if (todoService.deleteTodo(selectedTodo)) {
                        super.showAlert(Alert.AlertType.INFORMATION, "Successfully deleted the todo", "Successfully deleted the todo");
                    } else {
                        super.showAlert(Alert.AlertType.ERROR, "Error deleting todo", "Something went wrong during the deleting of the todo");
                    }
                }
            }
        } catch(SQLException | NoSuchFieldException | IllegalAccessException ex) {
            super.showAlert(Alert.AlertType.WARNING, "Something went wrong with deleting the todo", ex.getMessage());
        }
    }
}