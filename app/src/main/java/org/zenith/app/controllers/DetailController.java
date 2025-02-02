package org.zenith.app.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.zenith.app.services.TodoService;
import org.zenith.models.SubItem;
import org.zenith.models.TodoItem;
import org.zenith.util.EntityManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
    public ListView<SubItem> subItemsListView;

    @FXML
    public TextField subItemTextfield;

    @FXML
    public Button saveBtn;

    @FXML
    public Button createBtn;

    @FXML
    public Button deleteBtn;

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
                todoItem.title = titleTextInput.getText();
                todoItem.description = descriptionTextInput.getText();
                todoItem.isCompleted = isCompletedCheckbox.isSelected();
                todoItem.subItems = subItemsListView.getItems();

                if (expiresAtDatepicker.getValue() != null) {
                    todoItem.expiresAt = Date.from(expiresAtDatepicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                }

                // TODO: Add update subitems support
                todoService.updateTodo(todoItem);
                super.showAlert(Alert.AlertType.INFORMATION, "Updated", "Successfully updated the todo");
            } catch (Exception ex) {
                super.showAlert(Alert.AlertType.ERROR, "Something went wrong with saving the todo", ex.getMessage());
            }
        }
    }

    @FXML
    public void onSaveSubItemButtonClick() {
        SubItem subItem = new SubItem();
        subItem.title = subItemTextfield.getText();
        todoItem.subItems.add(subItem);

        subItemsListView.getItems().add(subItem);
        subItemTextfield.clear();
    }

    @FXML
    public void onCreateButtonClick() {
        TodoItem newTodo = new TodoItem();

        newTodo.title = titleTextInput.getText();
        newTodo.description = descriptionTextInput.getText();
        newTodo.isCompleted = isCompletedCheckbox.isSelected();
        newTodo.subItems = subItemsListView.getItems();

        if (expiresAtDatepicker.getValue() != null) {
            newTodo.expiresAt = Date.from(expiresAtDatepicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        try {
            todoService.addTodo(newTodo);

            super.closeWindow();
            super.showAlert(Alert.AlertType.INFORMATION, "Created a todo", "Successfully created a todo item");
        } catch (Exception ex) {
            ex.printStackTrace();
            super.showAlert(Alert.AlertType.ERROR, "Adding an todo", "Something went wrong trying to add a todo");
        }
    }

    public void setTodoItem(TodoItem todoItem) {
        this.todoItem = todoItem;
        boolean hasTodoItem = (todoItem != null);

        saveBtn.setVisible(hasTodoItem);
        saveBtn.setManaged(hasTodoItem);

        deleteBtn.setVisible(hasTodoItem);
        deleteBtn.setManaged(hasTodoItem);

        createBtn.setVisible(!hasTodoItem);
        createBtn.setManaged(!hasTodoItem);

        if (hasTodoItem) {
            titleTextInput.setText(todoItem.title);
            descriptionTextInput.setText(todoItem.description);
            isCompletedCheckbox.setSelected(todoItem.isCompleted);

            if (todoItem.expiresAt != null)
                expiresAtDatepicker.setValue(todoItem.expiresAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

            subItemsListView.setItems(FXCollections.observableArrayList(todoItem.subItems));
        } else {
            titleTextInput.clear();
            descriptionTextInput.clear();
            expiresAtDatepicker.setValue(null);
            isCompletedCheckbox.setSelected(false);
            subItemsListView.setItems(FXCollections.observableArrayList());
        }
    }
}
