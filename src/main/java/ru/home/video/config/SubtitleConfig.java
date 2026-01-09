package ru.home.video.config;

public class SubtitleConfig {
    // === FONT SIZE ===
    public static final int SMALL_SIZE_OF_FONT = 18;
    public static final int MEDIUM_SIZE_OF_FONT = 24;
    public static final int LARGE_SIZE_OF_FONT = 32;

    // === SUBTITLES POSITION ON THE SCREEN ===
    public static final int BOTTOM_POSITION = 2;
    public static final int CENTER_POSITION = 5;
    public static final int TOP_POSITION = 8;

    // === SUBTITLES PARAMETERS ===

    // ~ 35 chars for one line of subtitle
    public static final int QUANTITY_OF_CHARS_FOR_ONE_PORTION_SUBTITLES = 55; // doesn't include punctuation, space marks
    public static final int QUANTITY_OF_LINES_WITH_SUBTITLES = 2;
    public static final double TYPICAL_SECONDS_BETWEEN_WORDS_AT_ONE_PHRASE = 0.5;
    public static final int MAX_COUNT_OF_WORDS_FOR_ONE_PORTION_SUBTITLES = 16;
    public static final double MAX_DURATION_OF_SECONDS_FOR_ONE_SUBTITLE_ON_THE_SCREEN = 7.2;
    public static final double MIN_DURATION_OF_SECONDS_FOR_ONE_SUBTITLE_ON_THE_SCREEN = 0.2;
}
