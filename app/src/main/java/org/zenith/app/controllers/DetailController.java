package org.zenith.app.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.zenith.app.services.TodoService;
import org.zenith.models.TodoItem;
import org.zenith.util.EntityManager;

import java.time.LocalDate;
import java.time.ZoneId;

public class DetailController extends BaseController {
    private TodoService todoService;
    private TodoItem todoItem;

    @FXML
    public TextField titleTextInput;

    @FXML
    public TextArea descriptionTextInput;

    @FXML
    public DatePicker expiresAtDatepicker;

    @FXML
    public CheckBox isCompletedCheckbox;

    @FXML
    public ListView subItemsListView;

    @FXML
    public void initialize() {
        this.todoService = TodoService.getInstance();
    }

    @FXML
    public void onCloseButtonClick() {
        super.closeWindow();
    }

    @FXML
    public void onDeleteButtonClick() {
        boolean confirmedDelete = super.showConfirmationDialog(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this todo?",
                "There is no going back",
                "Delete",
                "Cancel");

        if (confirmedDelete) {
            super.closeWindow();

            try {
                todoService.deleteTodo(todoItem);
                super.showAlert(Alert.AlertType.INFORMATION, "Deleted", "Successfully deleted the todo");
            } catch (Exception ex) {
                super.showAlert(Alert.AlertType.ERROR, "Something went wrong with deleting the todo", ex.getMessage());
            }
        }
    }

    @FXML
    public void onSaveButtonClick() {
        boolean confirmSave = super.showConfirmationDialog(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to save this todo?",
                "You're data will be overwritten",
                "Save",
                "Cancel"
        );

        if (confirmSave) {
            try {
                todoService.updateTodo(todoItem);
                super.showAlert(Alert.AlertType.INFORMATION, "Updated", "Successfully updated the todo");
            } catch (Exception ex) {
                super.showAlert(Alert.AlertType.ERROR, "Something went wrong with saving the todo", ex.getMessage());
            }
        }
    }

    public void setTodoItem(TodoItem todoItem) {
        this.todoItem = todoItem;
        System.out.println(todoItem);

        titleTextInput.setText(todoItem.title);
        descriptionTextInput.setText(todoItem.description);
        expiresAtDatepicker.setValue(todoItem.expiresAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        isCompletedCheckbox.setSelected(todoItem.isCompleted);

        subItemsListView.setItems(FXCollections.observableArrayList(todoItem.subItems));
    }
}
