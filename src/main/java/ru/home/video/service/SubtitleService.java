package ru.home.video.service;

import ru.home.video.config.SubtitleConfig;
import ru.home.video.model.enums.LanguageType;
import ru.home.video.model.subtitles.SubtitleItem;
import ru.home.video.model.subtitles.SubtitleStyle;

import java.util.*;
import java.util.stream.Collectors;

import static ru.home.video.config.SubtitleConfig.MAX_COUNT_OF_WORDS_FOR_ONE_PORTION_SUBTITLES;
import static ru.home.video.config.SubtitleConfig.QUANTITY_OF_LINES_WITH_SUBTITLES;

public class SubtitleService {

    /**
     * The method creates styled subtitles in ASS format with proper Unicode support
     *
     * @param subtitles - ready subtitles, but without applied style (base entities with text and timestamp)
     * @param style     - style of given subtitles
     * @return - subtitles as a string
     */
    public static String createStyledSubtitles(List<SubtitleItem> subtitles, SubtitleStyle style) {
        if (subtitles != null && !subtitles.isEmpty()) {
            var assBuilder = getStringBuilderWithHeaders(style);
            // Adds Dialogue
            for (SubtitleItem item : subtitles) {
                if (item.text() != null && !item.text().trim().isEmpty()) {
                    assBuilder.append(String.format("Dialogue: 0,%s,%s,Default,,0,0,0,,%s\n",
                            formatAssTime(item.start()),
                            formatAssTime(item.end()),
                            item.text()
                    ));
                }
            }
            return assBuilder.toString();
        }
        return "";
    }

    /**
     * The method collects all subtitles base info into subtitle list
     *
     * @param textExtractedFromAudioTrack - an audio track converted into text
     * @param originalVideoLanguage       - an initial video voice acting
     * @param subtitlesLanguage           - a language of subtitles
     * @param subtitleStyle               - a style of subtitle text
     * @return a list with subtitles (base information: starting of text, ending of text, text itself, text style)
     */
    public static List<SubtitleItem> getReadySubtitles(List<SubtitleItem> textExtractedFromAudioTrack,
                                                       LanguageType originalVideoLanguage, String subtitlesLanguage,
                                                       SubtitleStyle subtitleStyle) {

        return Optional.ofNullable(textExtractedFromAudioTrack)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .filter(text -> text.text() != null)
                .filter(text -> !text.text().trim().isBlank())
                .filter(text ->
                        text.end() - text.start() >= SubtitleConfig.MIN_DURATION_OF_SECONDS_FOR_ONE_SUBTITLE_ON_THE_SCREEN)
                .map(text -> {
                    var translatedText = originalVideoLanguage.name().equalsIgnoreCase(subtitlesLanguage) ? text.text() :
                            TranslatorService.translateWithLibre(text.text(), originalVideoLanguage, subtitlesLanguage);
                    return new SubtitleItem(
                            text.start(),
                            text.end(),
                            getDividedStringSubtitleByHalf(translatedText), // split subtitle text in several rows
                            subtitleStyle
                    );
                })
                .map(SubtitleService::simpleTrimForLongTimeSubtitlesOnTheScreen)
                .collect(Collectors.toList());
    }

    private static StringBuilder getStringBuilderWithHeaders(SubtitleStyle style) {
        var assBuilder = new StringBuilder();

        // Script Info
        assBuilder.append("[Script Info]\n")
                .append("ScriptType: v4.00+\n") // Required header for ASS format (compatibility with version 4.00 and higher)
                .append("PlayResX: 384\n") // Video screen width for subtitles (standard default value)
                .append("PlayResY: 288\n") // Video screen height for subtitles (standard default value)
                .append("ScaledBorderAndShadow: no\n\n"); // Borders and shadows doesn't scale with the video
        // Styles with support all UI params
        assBuilder.append("[V4+ Styles]\n")
                .append("Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, ")
                .append("BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, ")
                .append("BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n");

        // - Name: name of subtitles style
        // - Fontname: "Arial" by default
        // - Fontsize: style.getFontSize()
        // - PrimaryColour: style.getTextColor() with transparency
        // - SecondaryColour: &H000000FF (fixed totally transparent blue color, which usually used for karaoke effect)
        // - OutlineColour: &H00000000 (fixed black, but OutlineColour=0, so this means, it doesn't reflect)
        // - BackColour: style.getBackgroundColor() (background color of the rectangle for subtitles, transparency is 0%)
        // - Bold: -1 - font: not bold (0=no, 1=yes, -1=default)
        // - Italic: 0 - font is not italic
        // - Underline: 0 - font without underlining
        // - StrikeOut: 0 - font without strikethrough
        // - ScaleX: 100 - horizontal scale (100% = normal)
        // - ScaleY: 100 - vertical scale (100% = normal)
        // - Spacing: 0 - additional space between characters (pixels)
        // - Angle: 0 - text rotation angle (degrees)
        // - BorderStyle: 4 - border style (Stroke + Shadow + Background)
        // - Outline: 0 (fixed, no outline) (pixels)
        // - Shadow: 0 (fixed, without shadow) (pixels)
        // - Alignment: style.getPosition() (text alignment)
        // - MarginL, MarginR, MarginV: 15,15,20 (fixed indents) (L-left, R-right, V-vertical indents)
        // - Encoding: (1=ANSI, 0=Unicode with BOM)

        assBuilder.append(String.format(
                "Style: Default,Arial,%d,%s,&H000000FF,&H80000000,%s,-1,0,0,0,100,100,0,0,4,0,0,%d,15,15,20,1\n\n",
                style.getFontSize(),
                style.getTextColor(),
                style.getBackgroundColor(),
                style.getPosition()
        ));

        // Events
        assBuilder.append("[Events]\n");
        assBuilder.append("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n");

        return assBuilder;
    }

    /**
     * Time formatting for ASS
     */
    private static String formatAssTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        int milliseconds = (int) ((seconds - Math.floor(seconds)) * 100);

        return String.format("%d:%02d:%02d.%02d", hours, minutes, secs, milliseconds);
    }

    /**
     * The method puts away long time being subtitles on the screen
     *
     * @param item - prepared ready subtitles
     * @return a subtitle with a shortened timestamp
     */
    private static SubtitleItem simpleTrimForLongTimeSubtitlesOnTheScreen(SubtitleItem item) {

        double duration = item.end() - item.start();
        if (duration <= SubtitleConfig.MAX_DURATION_OF_SECONDS_FOR_ONE_SUBTITLE_ON_THE_SCREEN) {
            return item;
        }

        // long subtitles remain at the end of a time for the allotted time
        // to avoid subtitles freezing on the screen without speech
        return new SubtitleItem(
                Math.max(item.start(), item.end() - SubtitleConfig.MAX_DURATION_OF_SECONDS_FOR_ONE_SUBTITLE_ON_THE_SCREEN),
                item.end(),
                item.text(),
                item.style()
        );
    }

    /**
     * The method gives splitting initial subtitle text on 2 rows
     *
     * @param text -an initial subtitle text
     * @return a formated subtitle text
     */
    private static String getDividedStringSubtitleByHalf(String text) {

        String[] words = text.split(" ");
        if (words.length <= (MAX_COUNT_OF_WORDS_FOR_ONE_PORTION_SUBTITLES / QUANTITY_OF_LINES_WITH_SUBTITLES)) {
            return text;
        }

        int mid = words.length / QUANTITY_OF_LINES_WITH_SUBTITLES; // just split it in half
        return String.join(" ", Arrays.copyOfRange(words, 0, mid)) + "\\N" +
                String.join(" ", Arrays.copyOfRange(words, mid, words.length));
    }
}