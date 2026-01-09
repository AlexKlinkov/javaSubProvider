package ru.home.video.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import ru.home.video.model.subtitles.SubtitleItem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ru.home.video.config.SubtitleConfig.*;

public class SpeechRecognitionService {

    /**
     * The method prepares subtitles for a video
     *
     * @param audioPath - a path with an audio file (by default: next to a base video) with the 'wav' extend
     * @param modelPath - a path to a free language model 'VOSK' for recognizing a speech,
     *                  installed locally by: <a href="https://alphacephei.com/vosk/models"></a>
     * @return the list with requested subtitles by user
     */
    public static List<SubtitleItem> recognizeSpeech(String audioPath, String modelPath) {
        try {
            // 1. initializes 'VOSK' model
            var model = new Model(modelPath);
            // 2. initializes recognizer ('16000.0f' - it's the standard for speech recognition)
            var recognizer = new Recognizer(model, 16000.0f);
            // 3. enables word-level timestamps (extremely important!)
            recognizer.setWords(true);
            // 4. creates a list with future subtitles
            List<SubtitleItem> items = new ArrayList<>();
            // 5. creates a buffer, if there will be a really large audio file (avoiding out of memory!)
            byte[] buffer = new byte[16384]; // 16 KB (standard), where to a voice data is read
            // 6. creates a new input stream with an audio file for handling it like a byte stream (for reading)
            try (InputStream audioStream = Files.newInputStream(Paths.get(audioPath))) {
                int bytesRead;
                while ((bytesRead = audioStream.read(buffer)) != -1) {
                    // 6.1 returns 'true' if there is enough collected data for an intermediate result
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        // 6.2 extracts a recognized speech with timestamps for translating and creating subtitles
                        fillSubtitlesByTextWithTimestamps(recognizer.getResult(), items); // intermediate results
                    }
                }
                // 7. returns rest of data (the last phrase, which wasn't treated in the above cycle)
                fillSubtitlesByTextWithTimestamps(recognizer.getFinalResult(), items); // the final result
                // 8. returns a list with the ready subtitles (not translated yet, but combined into whole phrases)
                return mergeToPhrases(items);
            }
        } catch (IOException e) {
            System.err.println("Method recognizeSpeech was failure.\nError: " + e.getMessage());
            return null;
        }
    }

    // === Auxiliary methods ===

    // Method fills up a list of subtitles (a text isn't translated yet)
    private static void fillSubtitlesByTextWithTimestamps(String jsonStr, List<SubtitleItem> items) {
        try {
            var result = new JSONObject(jsonStr);
            if (result.has("result")) {
                var words = result.getJSONArray("result");
                for (int i = 0; i < words.length(); i++) {
                    var word = words.getJSONObject(i);
                    items.add(new SubtitleItem(
                            word.getDouble("start"),
                            word.getDouble("end"),
                            word.getString("word"),
                            null
                    ));
                }
            }
        } catch (JSONException e) {
            System.err.println("Method fillSubtitlesByTextWithTimestamps was failure.\nError: " + e.getMessage());
        }
    }

    // combines separated words into phrases for translating and then creating subtitles
    private static List<SubtitleItem> mergeToPhrases(List<SubtitleItem> words) {
        if (!words.isEmpty()) {

            List<SubtitleItem> phrases = new ArrayList<>();
            var currentPhrase = new StringBuilder();
            int phraseWordCount = 0;
            SubtitleItem previousWord = null;
            SubtitleItem phraseStartWord = words.getFirst();
            SubtitleItem phraseEndWord = null;

            for (SubtitleItem word : words) {
                // Checking whether a new sentence needs to be started
                if (isNecessaryToStartNewPhraseByParams(currentPhrase, previousWord, word, phraseWordCount)) {
                    // Add the current phrase if it's not empty
                    if (!currentPhrase.isEmpty() && phraseEndWord != null) {
                        // Format and add the current phrase
                        phrases.add(new SubtitleItem(
                                phraseStartWord.start(),
                                phraseEndWord.end(),
                                formatPhraseText(
                                        getBeginningSentenceWithCapitalLetter(
                                                currentPhrase.toString().trim())
                                ),
                                null
                        ));
                    }
                    // Start a new phrase
                    currentPhrase.setLength(0);
                    phraseStartWord = word;
                    phraseWordCount = 0;
                }
                // Add a word to the current phrase
                if (!currentPhrase.isEmpty()) currentPhrase.append(" ");
                currentPhrase.append(word.text());
                phraseEndWord = word;
                phraseWordCount++;
                previousWord = word;
            }
            // Add the last phrase
            if (!currentPhrase.isEmpty() && phraseEndWord != null) {
                phrases.add(new SubtitleItem(phraseStartWord.start(), phraseEndWord.end(),
                        formatPhraseText(currentPhrase.toString().trim()), words.getFirst().style())
                );
            }

            return phrases;
        }
        return Collections.emptyList();
    }

    // The method checks it has to be started a new phrase, return true/false
    private static boolean isNecessaryToStartNewPhraseByParams(StringBuilder currentPhrase, SubtitleItem previousWord,
                                                               SubtitleItem currentWord, int phraseWordCount) {

        var previousText = (previousWord != null) ? previousWord.text() : "";
        return  // checking the length of a phrase (characters)
                currentPhrase.length() + currentWord.text().length() > QUANTITY_OF_CHARS_FOR_ONE_PORTION_SUBTITLES
                        // checking the pause between words
                        || (previousWord != null &&
                        currentWord.start() - previousWord.end() > TYPICAL_SECONDS_BETWEEN_WORDS_AT_ONE_PHRASE)
                        // checking the number of words in a phrase
                        || phraseWordCount >= MAX_COUNT_OF_WORDS_FOR_ONE_PORTION_SUBTITLES
                        // Checking for a sentence end mark in the previous word
                        || (previousWord != null &&
                        getPunctuationMarks().contains(previousText.charAt(previousText.length() - 1)));
    }

    // Method gives list with punctuation marks
    private static List<Character> getPunctuationMarks() {
        return Arrays.asList('.', '!', '?', ',', ':', ';');
    }

    // The method formats the text of a phrase, it adds a capital letter and return refreshed phrase text
    private static String getBeginningSentenceWithCapitalLetter(String phraseText) {
        var firstChar = phraseText.charAt(0);
        if (Character.isLetter(firstChar) && Character.isLowerCase(firstChar)) {
            phraseText = Character.toUpperCase(firstChar) + phraseText.substring(1);
        }
        return phraseText;
    }

    // The method formats the text of a phrase: it adds a dot if necessary.
    private static String formatPhraseText(String phraseText) {
        return !getPunctuationMarks().contains(phraseText.charAt(phraseText.length() - 1)) ? phraseText + "." : phraseText;
    }
}
