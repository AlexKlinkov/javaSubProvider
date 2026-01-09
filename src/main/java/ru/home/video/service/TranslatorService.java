package ru.home.video.service;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import ru.home.video.config.JavaSubProviderAppConfig;
import ru.home.video.model.enums.LanguageType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class TranslatorService {

    /**
     * The method provides access to translator service (the translator service is launching locally)
     *
     * @param text            - the text which has to be translated
     * @param initialLanguage - an initial language which has to be translated by LibreTranslate service
     * @param targetLanguage  - a language of translation
     * @return a translated text or null
     */
    public static String translateWithLibre(String text, LanguageType initialLanguage, String targetLanguage) {
        // 1. get a query for the translation service
        var request = getReadyRequestForTranslateService(text, initialLanguage, targetLanguage);
        try {
            // 2. executes the query on translating
            var response = EntityUtils.toString(HttpClients.createDefault().execute(request).getEntity());
            // 3. returns the translated text
            return new JSONObject(response).getString("translatedText");
        } catch (IOException e) {
            System.out.println("Method translateWithLibre was failure.\n Text: " + text + " \n error: " + e.getMessage());
            return text;
        }
    }

    // returns a ready request towards translate service (deployed locally by the docker desktop app)
    private static HttpPost getReadyRequestForTranslateService(String text,
                                                               LanguageType initialLanguage, String targetLanguage) {
        var body = new JSONObject();
        body.put("q", text); // a text for translating
        body.put("source", initialLanguage);
        body.put("target", targetLanguage);
        body.put("format", "text");

        var request = new HttpPost(JavaSubProviderAppConfig.LIBRE_URL);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));
        return request;
    }

    /**
     * The method launches the translation service with a minimized window using Windows terminal (cmd.exe) and \
     * a process name "LibreTranslate" !
     */
    public static void launchTranslatorService() {
        var languageCodes = Arrays.stream(LanguageType.values())
                .map(LanguageType::name)
                .collect(Collectors.joining(","));
        try {
            var command = String.format(
                    "@echo off\r\n" +
                            "cd /d \"%s\"\r\n" +  // changes the directory to Python scripts folder
                            "set ARGOS_TRANSLATE_PACKAGES_DIR=%s\r\n" +
                            "start \"LibreTranslate\" /MIN " + // launches process with name "LibreTranslate" and minimized window !
                            "\"%s\" -c \"from libretranslate import main; main()\" --host " +
                            JavaSubProviderAppConfig.HOST + " --port " + JavaSubProviderAppConfig.PORT +
                            " --load-only %s --update-models\r\n",
                    JavaSubProviderAppConfig.PYTHON_SCRIPTS_PATH,
                    JavaSubProviderAppConfig.LANGUAGE_MODELS_PATH,
                    JavaSubProviderAppConfig.PYTHON_SCRIPTS_PATH.replace("\\Scripts", "") + "\\python.exe",
                    languageCodes
            );

            var batFile = createLibreTranslateTempBatFileByStringCommand(command);
            if (batFile != null) {
                new ProcessBuilder("cmd.exe", "/c", batFile.getAbsolutePath()).start();
            }
        } catch (IOException e) {
            System.out.println("Method launchTranslatorService was failure.\n Error: " + e.getMessage());
        }
    }

    /**
     * The method provides a file (.bat) with a launching command for locally the translation service
     *
     * @param command - string presentation of launching command for the translation service using Windows terminal
     * @return prepared a bat file for launching the translation service
     */
    private static File createLibreTranslateTempBatFileByStringCommand(String command) {
        try {
            var batFile = File.createTempFile("libretranslate_", ".bat");
            Files.write(batFile.toPath(), command.getBytes());
            batFile.deleteOnExit();
            return batFile;
        } catch (IOException e) {
            System.out.println("The method: createLibreTranslateTempBatFileByStringCommand wasn't able to " +
                    "create the temp bat file: " + e.getMessage());
            return null;
        }
    }

    /**
     * The method stops the translator service using Windows terminal (cmd.exe) and a process name "LibreTranslate" !
     */
    public static void stopTranslatorService() {
        try {
            new ProcessBuilder("cmd.exe", "/c", "taskkill /F /FI \"WINDOWTITLE eq LibreTranslate\"").start();
        } catch (IOException e) {
            System.out.println("Error stopping the translation service: " + e.getMessage());
        }
    }
}

