package ru.home.video.model.subtitles;

import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.home.video.config.SubtitleConfig;

/**
 * The Class for storing subtitle styles
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubtitleStyle {

    private String textColor;
    private int position;
    private int fontSize;
    private String backgroundColor;


    /**
     * Gets the font size based on the text description
     * @param subtitleSize text description of the size
     * @return the numeric value of the font size
     */
    public int getFontSize(String subtitleSize) {
        return switch (subtitleSize) {
            case "Small" -> fontSize = SubtitleConfig.SMALL_SIZE_OF_FONT;
            case "Medium" -> fontSize = SubtitleConfig.MEDIUM_SIZE_OF_FONT;
            default -> fontSize = SubtitleConfig.LARGE_SIZE_OF_FONT; // Крупный/Large
        };
    }

    /**
     * Gets the alignment code for the ASS format
     * @param subtitlePosition text description of the position
     * @return the alignment code for ASS
     */
    public int getDigitalViewOfPosition(String subtitlePosition) {
        return switch (subtitlePosition) {
            case "Top" -> position = SubtitleConfig.TOP_POSITION;
            case "Center"  -> position = SubtitleConfig.CENTER_POSITION;
            default -> position = SubtitleConfig.BOTTOM_POSITION;
        };
    }

    /**
     * The method converts JavaFX Color directly to ASS format (&H00BBGGRR)
     *
     * @param color                  - an initial color from a user form (interface)
     * @param transparencyPercentage - points how faded color of the subtitles has to be (0.0%=opaque, 100.0%=transparent)
     * @return - a presentation of color in correct string format for .ass subtitles
     */
    public static String colorToAss(Color color, int transparencyPercentage) {
        var transparency = transparencyPercentage == 0 ? 0.01 : transparencyPercentage / 100.0;
        return String.format("&H%02X%02X%02X%02X", (int) (color.getOpacity() * 255 * transparency),
                (int) (color.getBlue() * 255), (int) (color.getGreen() * 255), (int) (color.getRed() * 255));
    }

    /**
     * The method gives percentage for background color on based transparency of the main font
     * @param fontOpacity - % (0 - 100) of the main font transparency
     * @return % of transparency for background color
     */
    public static int getPercentOfOpacityForBackGroundColor(int fontOpacity) {
        if (fontOpacity <= 20) {
            return 60;
        } else if (fontOpacity <= 50) {
            return 30;
        } else { // fontOpacity <= 80
            return 15;
        }
    }

}