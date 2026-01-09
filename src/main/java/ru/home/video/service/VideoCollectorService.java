package ru.home.video.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * The class combines video with subtitles, responds for interaction with external app 'ffmpeg'.
 */
public class VideoCollectorService {

    /**
     * The method adds subtitles from ASS file (all styles already in the file)
     *
     * @param ffmpegPath          -
     * @param inputVideoFilePath  - a current video file, which has to be treatment
     * @param subtitlesPath       -
     * @param outputVideoFilePath - a directory (folder) where a new video file (with subtitles) has to be saved
     */
    public static void addSubtitlesToVideo(String ffmpegPath, String inputVideoFilePath,
                                           String subtitlesPath, String outputVideoFilePath) {
        try {
            List<String> command = Arrays.asList(
                    ffmpegPath,
                    "-i", inputVideoFilePath,
                    "-filter_complex", "ass=" + getCorrectedPathDirectedToFfmpegAppIndependentOfOS(subtitlesPath),
                    "-c:a", "copy",
                    "-y",
                    outputVideoFilePath
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (var in = process.getInputStream()) {
                in.transferTo(System.out); // reads the output data to avoid flow's blocking.
            }

            process.waitFor(); // the main flow await ending of the ffmpeg app finish his work

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error: " + e.getMessage(), e);
        }
    }

    /**
     * The method fix path
     *
     * @param path - an initial path
     * @return natural path for speaking with Ffmpeg app
     */
    private static String getCorrectedPathDirectedToFfmpegAppIndependentOfOS(String path) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return "'" + path.replace("\\", "/").replace(":", "\\:") + "'";
        } else { // Linux/Mac:
            return path.replace("'", "'\\''");  // escape single quotes
        }
    }
}