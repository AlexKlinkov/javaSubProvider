package ru.home.video.model.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum LanguageType {

    // You can add here your language type for downloading and using in the app context, by analogy as below, just like:
    // "en("English"),", but before the LAST LINE WITH ';' !
    en("English"),
    // add here your language type if it's necessary. The full list of supported languages you can see here:

    ru("Russian");

    @Getter
    private final String languageName;
    private static final Map<String, LanguageType> BY_LANGUAGE_NAME = new HashMap<>();

    static { // Initialize map when loading the class
        for (LanguageType lang : values()) {
            BY_LANGUAGE_NAME.put(lang.getLanguageName(), lang);
        }
    }

    LanguageType(String languageName) {
        this.languageName = languageName;
    }

    /**
     * The method gives language type by its name
     *
     * @param languageName - full name of the language
     * @return - defined language type
     */
    public static LanguageType getTypeByLanguageName(String languageName) {
        return BY_LANGUAGE_NAME.getOrDefault(languageName, null);
    }
}
