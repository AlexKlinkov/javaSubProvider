package ru.home.video.config;

import lombok.Getter;
import ru.home.video.model.enums.LanguageType;

import java.io.File;

public class JavaSubProviderAppConfig {

    // === PATH VARIABLES ===

    @Getter
    private static String VOSK_MODEL_PATH;

    public static final String APP_COMPONENTS_PATH = System.getProperty("user.dir") + File.separator +
            "src" + File.separator + "main" + File.separator + "resources" + File.separator + "app_components";

    // a path to 'FFMPEG' app, which was installed locally and works with video and audio files
    public static final String FFMPEG_PATH = APP_COMPONENTS_PATH + File.separator + "ffmpeg" +
            File.separator + "bin" + File.separator + "ffmpeg.exe";

    public static final String PYTHON_SCRIPTS_PATH = APP_COMPONENTS_PATH + File.separator + "Python" +
            File.separator + "Python312" + File.separator + "Scripts"; // a path to python scripts folder

    // a path where language models are stored for using it by translation service
    public static final String LANGUAGE_MODELS_PATH = APP_COMPONENTS_PATH + File.separator + "lang_models";

    public static final String HOST = "127.0.0.1"; // a host where will be launched the translation service
    public static final String PORT = "5000"; // the exact address where the translation service will be listening requests
    // url of the translator service (127.0.0.1 - localhost)
    public static final String LIBRE_URL = "http://" + HOST + ":" + PORT + "/translate";

    /**
     * The method sets language models for extracting audio track from video (.mp4 -> .wav) and convert an audio into text
     *
     * @param languageType - the language of a provided audio
     */
    public static void setAppComponentsByLanguageType(LanguageType languageType) {
        // a path to language model, which extracts phrases from an audio track and converts it into text (string)
        VOSK_MODEL_PATH = APP_COMPONENTS_PATH + File.separator +
                "vosk" + File.separator + languageType + File.separator + "vosk-model";
    }
}
