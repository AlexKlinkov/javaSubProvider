package ru.home.video.service;

import java.io.IOException;

public class AudioService {

    /**
     * The method extracts an audio file from the given video and put it next to the video
     *
     * @param ffmpegPath - a path to a free built app 'FFMPEG', which was installed locally and works with video
     *                   and audio files (<a href="https://www.gyan.dev/ffmpeg/builds/"></a>)
     * @param videoPath  - a path to the inputted video
     * @param audioPath  - a path with an audio file (by default: next to a base video) with the 'wav' extend
     */
    public static void extractAudioTrack(String ffmpegPath, String videoPath, String audioPath) {
        try {
            // it starts a command line of Windows OS, launches an external app 'FFMPEG'. Params:
            // '-i' - an input file, '-ac' - a number of audio channels, '1' - mono (single-channel sound)
            // '-ar' - a sample rate (Hz), '16000' - it's the standard for speech recognition,
            // 'y' - overwrite an output file (even it already exists)
            new ProcessBuilder(ffmpegPath, "-i", videoPath, "-ac", "1", "-ar", "16000", "-y", audioPath)
                    .inheritIO().start();
        } catch (IOException e) {
            System.out.println("An audio track wasn't extracted by video file, with the next path: " + videoPath);
        }
    }
}
