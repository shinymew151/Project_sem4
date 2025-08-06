package com.ai.video.FacelessVideo.service;

import com.ai.video.FacelessVideo.util.ProcessRunner;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    public String generateLipSyncVideo(String audioPath) {
        String imagePath = "assets/avatar.jpg";
        String outputPath = "output/video_" + System.currentTimeMillis() + ".mp4";
        String command = "python scripts/lipsync.py --face " + imagePath +
                         " --audio " + audioPath +
                         " --outfile " + outputPath;
        ProcessRunner.run(command);
        return outputPath;
    }
}
