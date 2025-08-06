package com.ai.video.FacelessVideo.controller;

import com.ai.video.FacelessVideo.entity.VideoRecord;
import com.ai.video.FacelessVideo.repository.VideoRecordRepository;
import com.ai.video.FacelessVideo.service.GoogleTTSService;
import com.ai.video.FacelessVideo.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class ArticleController {

    @Autowired
    private GoogleTTSService ttsService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRecordRepository repository;

    @PostMapping("/generate")
    public String generateVideo(@RequestBody Map<String, String> body) {
        String text = body.get("text");

        String audioPath = ttsService.generateSpeech(text);
        String videoPath = videoService.generateLipSyncVideo(audioPath);

        VideoRecord record = new VideoRecord();
        record.setText(text);
        record.setAudioPath(audioPath);
        record.setVideoPath(videoPath);
        repository.save(record);

        return "Video created at: " + videoPath;
    }
}
