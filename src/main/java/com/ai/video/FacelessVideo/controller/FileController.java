package com.ai.video.FacelessVideo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file) throws IOException {
        String destPath = "uploads/" + file.getOriginalFilename();
        file.transferTo(new File(destPath));
        return "File uploaded to: " + destPath;
    }
}
