package ru.home.video.service;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import ru.home.video.config.JavaSubProviderAppConfig;
import ru.home.video.model.enums.LanguageType;
import ru.home.video.model.subtitles.SubtitleItem;
import ru.home.video.model.subtitles.SubtitleStyle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static ru.home.video.service.SubtitleService.getReadySubtitles;
import static ru.home.video.utils.AppStatusProviderUtils.*;

/**
 * The class responds for adding subtitles in necessary format to a new video file.
 */
public class VideoProcessService {

    /**
     * The method handles input params as to save a new video file which was requested by user
     *
     * @param inputVideoFilePath    - a current video file, which has to be treatment
     * @param outputVideoFilePath   - a directory (folder) where a new video file (with subtitles) has to be saved
     * @param originalVideoLanguage - an original language of the initial video file
     * @param subtitlesLanguage     - language of subtitles, which has to be added to a new video file
     * @param subtitleStyle         - styling parameters for subtitles
     * @param progressBar           - a visual elem of user form, which says about status of a video treatment process
     * @param progressLabel         - a label for progress status
     */
    public static void processVideo(TextField inputVideoFilePath, TextField outputVideoFilePath,
                                    String originalVideoLanguage, String subtitlesLanguage,
                                    SubtitleStyle subtitleStyle, ProgressBar progressBar, Label progressLabel) {

        // 1. Generates paths for temp files
        var audioPath = outputVideoFilePath.getText() + File.separator + "temp_audio.wav";
        var subtitlesPath = outputVideoFilePath.getText() + File.separator + "subtitles.ass";
        var outputVideoPath = outputVideoFilePath.getText() + File.separator +
                getNewVideoFileNameWithLanguagePrefixAndExtension(inputVideoFilePath, subtitlesLanguage);
        try {
            // 2. Installs paths to tools depends on choose language
            updateProgress(0.1, "Start processing...", progressBar, progressLabel);
            var sourceLang = LanguageType.valueOf(originalVideoLanguage);
            JavaSubProviderAppConfig.setAppComponentsByLanguageType(sourceLang);

            // 3. Extracts audio from provided video
            updateProgress(0.2, "Extracting audio from the video...", progressBar, progressLabel);
            AudioService.extractAudioTrack(JavaSubProviderAppConfig.FFMPEG_PATH, inputVideoFilePath.getText(), audioPath);

            // 4. Recognizes speech from audio
            updateProgress(0.4, "Speech recognition...", progressBar, progressLabel);
            List<SubtitleItem> textExtractedFromAudioTrack =
                    SpeechRecognitionService.recognizeSpeech(audioPath, JavaSubProviderAppConfig.getVOSK_MODEL_PATH());

            // 5. Translates text and applies styling
            updateProgress(0.6, "Preparation, packaging of subtitles...", progressBar, progressLabel);
            if (textExtractedFromAudioTrack == null || textExtractedFromAudioTrack.isEmpty()) {
                updateProgress(0.0, "⚠ Speech recognition failed", progressBar, progressLabel);
                showError("Unable to recognize speech in the video");
            }

            // 6. Creates styled subtitles in ASS format
            updateProgress(0.8, "Creating stylized subtitles...", progressBar, progressLabel);
            var subtitles = SubtitleService.createStyledSubtitles(getReadySubtitles(textExtractedFromAudioTrack,
                    LanguageType.valueOf(originalVideoLanguage), subtitlesLanguage, subtitleStyle), subtitleStyle);

            // 7. Write subtitles into a temp file (extension is .ass)
            saveSubtitlesToASSFile(Paths.get(subtitlesPath), subtitles);

            // 8. Adds subtitles to the video
            updateProgress(0.9, "Adding subtitles to video...", progressBar, progressLabel);
            VideoCollectorService.addSubtitlesToVideo(
                    JavaSubProviderAppConfig.FFMPEG_PATH, inputVideoFilePath.getText(), subtitlesPath, outputVideoPath);
            updateProgress(1.0, "✅ Processing completed!", progressBar, progressLabel);
            showSuccess("The video has been processed successfully!\nSaved in: " + outputVideoPath);

        } catch (Exception e) {
            updateProgress(0.0, "Processing error", progressBar, progressLabel);
            showError("Error while processing video: " + e.getMessage());
        } finally {
            // 9. Delete temp files (an audio track and subtitles)
            deleteTempFile(audioPath);
            deleteTempFile(subtitlesPath);
        }
    }

    // === Auxiliary methods ===

    private static void saveSubtitlesToASSFile(Path subtitlesPath, String subtitles) {
        try (BufferedWriter writer = Files.newBufferedWriter(subtitlesPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {

            writer.write(subtitles);
            writer.flush();

        } catch (IOException e) {
            showError("Failed to create subtitles: empty content");
            throw new RuntimeException(e);
        }
    }

    private static String getNewVideoFileNameWithLanguagePrefixAndExtension(TextField inputVideoFilePath,
                                                                            String subtitlesLanguage) {

        var outputVideoPathArray = new File(inputVideoFilePath.getText()).getName().split("\\.");
        var languagePrefix = outputVideoPathArray[outputVideoPathArray.length - 2] + "_" + subtitlesLanguage;
        outputVideoPathArray[outputVideoPathArray.length - 2] = languagePrefix;
        return String.join(".", outputVideoPathArray);
    }

    /**
     * The method deletes temp files
     *
     * @param tempFile - a temp file with extension
     */
    private static void deleteTempFile(String tempFile) {
        try {
            Files.deleteIfExists(Paths.get(tempFile));
        } catch (IOException e) {
            System.out.println("There aren't temp file: - " + tempFile + " for deleting\n" + e.getMessage());
        }
    }
}