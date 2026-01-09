package ru.home.video.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import ru.home.video.config.LanguageConfig;
import ru.home.video.model.enums.LanguageType;
import ru.home.video.model.subtitles.SubtitleStyle;
import ru.home.video.service.TranslatorService;
import ru.home.video.service.VideoProcessService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.home.video.model.subtitles.SubtitleStyle.colorToAss;
import static ru.home.video.model.subtitles.SubtitleStyle.getPercentOfOpacityForBackGroundColor;
import static ru.home.video.utils.AppStatusProviderUtils.*;

/**
 * The class responds for business logic
 */
public class MainController {

    // === Connection with FXML elems ===
    @FXML
    private TextField inputVideoFilePath;
    @FXML
    private TextField outputVideoFilePath;
    @FXML
    private ComboBox<String> languageCombo;
    @FXML
    private ComboBox<String> fontSizeCombo;
    @FXML
    private ComboBox<String> positionCombo;
    @FXML
    private ColorPicker textColorPicker;
    @FXML
    private ColorPicker backgroundColorPicker;
    @FXML
    private Label opacityValueLabel;
    @FXML
    private Slider opacitySlider;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;

    @Setter
    private Stage primaryStage;
    private String selectedLanguage;
    private String targetLanguage;
    private SubtitleStyle subtitleStyle;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Initialization of controller automatically after FXML file was loaded
     */
    @FXML
    private void initialize() {
        init();
    }

    /**
     * Updates the subtitle style from the form values
     */
    private void updateSubtitleStyle() {
        subtitleStyle = new SubtitleStyle();
        subtitleStyle.setFontSize(subtitleStyle.getFontSize(fontSizeCombo.getValue())); // sets up chosen subtitles size
        subtitleStyle.setPosition(
                subtitleStyle.getDigitalViewOfPosition(positionCombo.getValue())); // sets up chosen subtitles position
        subtitleStyle.setTextColor(
                colorToAss(textColorPicker.getValue(), (int) opacitySlider.getValue())); // sets up chosen subtitles color
        subtitleStyle.setBackgroundColor(
                colorToAss(
                        backgroundColorPicker.getValue(),
                        getPercentOfOpacityForBackGroundColor((int) opacitySlider.getValue()
                        )) // sets up chosen subtitles background color
        );
    }

    // === Event handler ===
    @FXML
    private void handleBrowseInput() { // chooses inputting video
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Select a video file");

        // Filter for videos
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Video files", "*.mp4", "*.avi", "*.mov", "*.mkv", "*.wmv");
        fileChooser.getExtensionFilters().add(extFilter);

        inputVideoFilePath.setText(fileChooser.showOpenDialog(primaryStage).getAbsolutePath());
    }

    @FXML
    private void handleBrowseOutput() { // chooses a folder for saving
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a folder for saving");

        outputVideoFilePath.setText(directoryChooser.showDialog(primaryStage).getAbsolutePath());
    }

    @FXML
    private void handleStartProcessing() { // launch of video handling
        if (!validateInput(inputVideoFilePath, outputVideoFilePath)) { // checks the input data
            showError("Please fill in all fields correctly!");
            return;
        }

        updateLanguage(); // updates the language before processing
        updateSubtitleStyle(); // updates the style before processing
        createMainFlow();  // the flow will be finished together with app
        launchMainFlow(); // launches video handling in a separate thread
    }

    @FXML
    private void handleClearForm() { // clearing of fxml form
        executorService.shutdownNow(); // finish the previous flow
        init();
    }

    @FXML
    private void handleExit() { // way out from app
        TranslatorService.stopTranslatorService(); // closes translate service, when a user doesn't use the app
        executorService.shutdownNow(); // finish the previous flow
        primaryStage.close();
    }

    // === Base logic of a video treatment ===
    private void processVideo() {
        VideoProcessService.processVideo(inputVideoFilePath, outputVideoFilePath, selectedLanguage, targetLanguage,
                subtitleStyle, progressBar, progressLabel);
    }

    private void createMainFlow() {
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread task = new Thread(r, "VideoProcessingThread");
            task.setDaemon(true); // the flow will be finished together with app
            return task;
        });
    }

    private void launchMainFlow() {
        executorService.submit(() -> {
            try {
                processVideo();
            } catch (Exception e) {
                TranslatorService.stopTranslatorService(); // closes translate service, when there is occurred error
                showError("Error processing: " + e.getMessage());
            }
        });
    }

    // method for the first initialize user interface and also for initialize it again after the user form was cleaned
    private void init() {
        TranslatorService.launchTranslatorService(); // launches the translator service
        inputVideoFilePath.clear();
        outputVideoFilePath.clear();
        languageCombo.getItems().setAll(LanguageConfig.AVAILABLE_LANGUAGE_PAIRS);
        languageCombo.setValue(LanguageConfig.DEFAULT_LANGUAGE_PAIR); // default value (combobox with languages)
        languageCombo.setOnAction(event -> updateLanguage());
        fontSizeCombo.setValue("Small"); // default value (font size combobox)
        positionCombo.setValue("Below"); // default value (combobox of subtitles position)
        opacityValueLabel.textProperty()
                .bind(Bindings.format("%.0f%%", opacitySlider.valueProperty())); // Setup opacity slider binding
        textColorPicker.setValue(Color.WHITE); // default value (subtitles color)
        backgroundColorPicker.setValue(Color.BLACK); // default value (background of subtitles color)
        languageCombo.setOnAction(e -> updateLanguage()); // a language handler
        progressBar.setProgress(0); // Initial state of progress
        progressLabel.setText("Ready to go"); // Initial state of progress
    }

    /**
     * Updates chosen language
     */
    private void updateLanguage() {
        var langArray = languageCombo.getValue().split(" → ");
        selectedLanguage = LanguageType.getTypeByLanguageName(langArray[0]).name();
        targetLanguage = LanguageType.getTypeByLanguageName(langArray[1]).name();
    }
}