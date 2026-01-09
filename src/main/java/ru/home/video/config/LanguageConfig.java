package ru.home.video.config;

import java.util.Arrays;
import java.util.List;

public class LanguageConfig {
    // List of available language pairs
    public static final List<String> AVAILABLE_LANGUAGE_PAIRS = Arrays.asList(
            "Russian → Russian",
            "Russian → English",
            "English → English",
            "English → Russian"
    );

    // Language by default
    public static final String DEFAULT_LANGUAGE_PAIR = "English → English";
}
