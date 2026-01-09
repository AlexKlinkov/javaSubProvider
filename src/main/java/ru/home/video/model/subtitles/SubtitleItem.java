package ru.home.video.model.subtitles;

/**
 * The class embodies structure of subtitles with timestamps
 * @param start - beginning time of phrase
 * @param end - ending time of phrase
 * @param text - subtitles itself
 * @param style - style of subtitles reflection on a video
 */
public record SubtitleItem(double start, double end, String text, SubtitleStyle style) {

}
