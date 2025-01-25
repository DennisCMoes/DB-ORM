package org.zenith.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public abstract class BaseController {
    protected Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    protected void openNewWindow(String fxmlFilename, String windowTitle, Consumer<BaseController> controllerInitializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/zenith/app/" + fxmlFilename));
            Parent root = loader.load();

            Stage newStage = new Stage();
            BaseController newController = loader.getController();
            newController.setStage(newStage);

            if (controllerInitializer != null) {
                controllerInitializer.accept(newController);
            }

            Scene scene = new Scene(root);

            newStage.setScene(scene);
            newStage.initModality(Modality.WINDOW_MODAL);
            newStage.initOwner(stage);
            newStage.setTitle(windowTitle);
            newStage.show();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void closeWindow() {
        if (stage != null) {
            stage.close();
        }
    }

    protected void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    protected boolean showConfirmationDialog(Alert.AlertType alertType, String title, String message) {
        return showConfirmationDialog(alertType, title, message, "Yes", "No");
    }

    protected boolean showConfirmationDialog(Alert.AlertType alertType, String title, String message, String confirmLabel, String cancelLabel) {
        Alert alert = new Alert(alertType);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Block input in the modal window
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(stage);

        ButtonType yesButton = new ButtonType(confirmLabel);
        ButtonType noButton = new ButtonType(cancelLabel);

        alert.getButtonTypes().setAll(yesButton, noButton);

        return alert.showAndWait().orElse(ButtonType.NO) == yesButton;
    }

    protected FXMLLoader getController(String fxmlFilename) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/zenith/app/" + fxmlFilename));
//            return fxmlLoader.load();
        return fxmlLoader;
    }
}
