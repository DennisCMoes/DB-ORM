package org.zenith.app.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.zenith.models.TodoItem;

import java.time.LocalDate;
import java.time.ZoneId;

public class DetailController extends BaseController {
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
    public void initialize() {}

    @FXML
    public void onCloseButtonClick() {
        super.closeWindow();
    }

    public void setTodoItem(TodoItem todoItem) {
        this.todoItem = todoItem;
        System.out.println(todoItem);

        titleTextInput.setText(todoItem.title);
        descriptionTextInput.setText(todoItem.description);
        expiresAtDatepicker.setValue(todoItem.expiresAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        isCompletedCheckbox.setSelected(todoItem.isCompleted);
    }
}
