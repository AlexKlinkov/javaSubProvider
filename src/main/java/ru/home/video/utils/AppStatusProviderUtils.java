package ru.home.video.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class AppStatusProviderUtils {

    /**
     * The method checks correction of inputted data
     *
     * @param inputVideoFilePath  - a link to the initial video file
     * @param outputVideoFilePath - a link to the directory where this video file has to  be saved
     * @return true - the video file exists and directory for saving was pointed correctly, otherwise false
     */
    public static boolean validateInput(TextField inputVideoFilePath, TextField outputVideoFilePath) {
        // Check the video
        if (inputVideoFilePath.getText().trim().isEmpty()) {
            return false;
        }
        File videoFile = new File(inputVideoFilePath.getText());
        if (!videoFile.exists() || !videoFile.isFile()) {
            return false;
        }
        // Check output folder
        return !outputVideoFilePath.getText().trim().isEmpty();
    }

    /**
     * The method updates progress bar and label for user interface
     *
     * @param progress      - points the stage of execution the app (value from 0.0 till 1.0)
     * @param message       - describes the stage of execution the app
     * @param progressBar   - the visual element for reflection of execution the app (just line which is being color)
     * @param progressLabel - the visual element for message reflection close to the progressBar
     */
    public static void updateProgress(double progress, String message, ProgressBar progressBar, Label progressLabel) {
        javafx.application.Platform.runLater(() -> {
            progressBar.setProgress(progress);
            progressLabel.setText(message);
        });
    }

    /**
     * The method shows a message about failure
     *
     * @param message - the gist of error
     */
    public static void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * The method shows a message about success
     *
     * @param message - text about positive result of executing app
     */
    public static void showSuccess(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Successfully");
            alert.setHeaderText("Ready!");

            Label label = new Label(message); // creates a label with text wrapping
            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setMaxHeight(Double.MAX_VALUE);

            alert.getDialogPane().setContent(label);
            alert.setResizable(true);

            alert.showAndWait();
        });
    }
}
