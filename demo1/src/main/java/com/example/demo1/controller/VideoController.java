package com.example.demo1.controller;

import com.example.demo1.service.SummarizationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api")
public class VideoController {

    @PostMapping("/upload")
    public String uploadVideo(@RequestParam("video") MultipartFile file) {

        try {

            String uploadDir = "C:/videos/";
            String videoPath = uploadDir + file.getOriginalFilename();

            File dest = new File(videoPath);
            dest.getParentFile().mkdirs();

            file.transferTo(dest);

            // audio path
            String audioPath = uploadDir + "audio.mp3";

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "C:\\Users\\HP\\Downloads\\ffmpeg-8.0.1-essentials_build\\ffmpeg-8.0.1-essentials_build\\bin\\ffmpeg.exe",
                    "-i", videoPath,
                    "-vn",
                    "-acodec", "mp3",
                    audioPath
            );

            processBuilder.start();

            return "Video uploaded and audio extracted";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error uploading video";
        }
    }
    @Autowired
    private SummarizationService summarizationService;

    @PostMapping("/summarize")
    public String summarize(@RequestBody String text) {
        return summarizationService.summarizeText(text);
    }
}