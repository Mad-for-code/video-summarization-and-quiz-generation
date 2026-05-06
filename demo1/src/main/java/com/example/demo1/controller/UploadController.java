package com.example.demo1.controller;

import com.example.demo1.dto.ResultDTO;
import com.example.demo1.entity.User;
import com.example.demo1.entity.Video;
import com.example.demo1.repository.UserRepository;
import com.example.demo1.repository.VideoRepository;
import com.example.demo1.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UploadController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoService videoService;

    private final String uploadDir = "C:/Users/HP/Downloads/demo1/demo1/uploads/";

    // =========================
    // UPLOAD
    // =========================
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId
    ) {
        try {

            File folder = new File(uploadDir);
            if (!folder.exists()) folder.mkdirs();

            String originalName = file.getOriginalFilename();

            // check if already uploaded before
            List<Video> existingVideos =
                    videoRepository.findAllByOriginalFilename(originalName);

            for (Video existingVideo : existingVideos) {
                if (existingVideo.getTranscriptText() != null) {
                    return ResponseEntity.ok(existingVideo.getId());
                }
            }

            // unique filename
            String fileName = System.currentTimeMillis() + "_" + originalName;

            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);

            ProcessBuilder pb = new ProcessBuilder(
                    "C:/Users/HP/AppData/Local/Programs/Python/Python312/python.exe",
                    "C:/Users/HP/Downloads/demo1/demo1/transcribe.py",
                    fileName
            );

            pb.directory(new File("C:/Users/HP/Downloads/demo1/demo1"));
            pb.redirectErrorStream(true);

            Process process = pb.start();

            new Thread(() -> {
                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(process.getInputStream()))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("PYTHON: " + line);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            System.out.println("Python started for file: " + fileName);

            User user = userRepository.findById(userId).orElseThrow();

            Video video = new Video();
            video.setFilename(fileName);
            video.setOriginalFilename(originalName);

            // IMPORTANT: exactly same as python output
            String baseName = fileName.substring(0, fileName.lastIndexOf("."));
            video.setTranscript(baseName + "_output.txt");
            video.setSummary(baseName + "_summary.txt");
            video.setQuiz(baseName + "_quiz.txt");

            video.setUser(user);

            videoRepository.save(video);

            return ResponseEntity.ok(video.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed");
        }
    }

    // =========================
    // TRANSCRIPT
    // =========================
    @GetMapping("/transcript/{id}")
    public String getTranscript(
            @PathVariable Long id,
            @RequestParam(defaultValue = "en") String lang
    ) {

        System.out.println("Selected language: " + lang);

        Video video = videoRepository.findById(id).orElse(null);

        if (video == null) {
            System.out.println("Video not found for id: " + id);
            return "NOT_READY";
        }

        System.out.println("DB transcript name: " + video.getTranscript());

        String fullPath = uploadDir + video.getTranscript();
        System.out.println("Looking for: " + fullPath);

        File file = new File(fullPath);
        System.out.println("Exists: " + file.exists());

        if (!file.exists()) return "NOT_READY";

        String text;

        // Load from DB or file
        if (video.getTranscriptText() != null) {
            text = video.getTranscriptText();
        } else {
            text = videoService.readFileContent(fullPath);

            video.setTranscriptText(text);
            videoRepository.save(video);
        }

        // Translate if needed
        if (!lang.equals("en")) {
            text = videoService.translateLargeText(text, lang);
        }

        return text;
    }
    // =========================
    // SUMMARY
    // =========================
    @GetMapping("/summary/{id}")
    public String getSummary(@PathVariable Long id,
                             @RequestParam(defaultValue = "en") String lang) {

        Video video = videoRepository.findById(id).orElse(null);
        if (video == null) return "NOT_READY";

        String text;

        if (video.getSummaryText() != null) {
            text = video.getSummaryText();
        } else {
            String fullPath = uploadDir + video.getSummary();
            File file = new File(fullPath);

            if (!file.exists()) return "NOT_READY";

            text = videoService.readFileContent(fullPath);

            video.setSummaryText(text);
            videoRepository.save(video);
        }

        if (!lang.equals("en")) {
            text = videoService.translateLargeText(text, lang);
        }

        return text;
    }

    // =========================
    // QUIZ
    // =========================
    @GetMapping("/quiz/{id}")
    public String getQuiz(@PathVariable Long id,
                          @RequestParam(defaultValue = "en") String lang) {

        Video video = videoRepository.findById(id).orElse(null);
        if (video == null) return "NOT_READY";

        String text;

        if (video.getQuizText() != null) {
            text = video.getQuizText();
        } else {
            String fullPath = uploadDir + video.getQuiz();
            File file = new File(fullPath);

            if (!file.exists()) return "NOT_READY";

            text = videoService.readFileContent(fullPath);

            video.setQuizText(text);
            videoRepository.save(video);
        }

        if (!lang.equals("en")) {
            text = videoService.translateLargeText(text, lang);
        }

        return text;
    }

    // ----------//
    ///--------//
    // pdf /////

    @GetMapping("/download-pdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "en") String lang
    ) {

        Video video = videoRepository.findById(id).orElse(null);

        if (video == null || video.getTranscript() == null) {
            return ResponseEntity.notFound().build();
        }

        String fullPath = uploadDir + video.getTranscript();
        File file = new File(fullPath);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Read transcript
        String text = videoService.readFileContent(fullPath);

        // Translate only once

        if (!lang.equals("en")) {
            text = videoService.translateLargeText(text, lang);

        }

        byte[] pdfBytes = videoService.generatePdf(text);

        // Generate PDF

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=transcript.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdfBytes);
    }
    //----------------//
    //youtube video---//
    //----------------//

    @PostMapping("/upload-youtube")
    public ResponseEntity<?> uploadYoutube(@RequestBody Map<String, String> body) {

        try {

            String url = body.get("url");
            Long userId = Long.parseLong(body.get("userId"));

            String fileName = System.currentTimeMillis() + "_youtube.mp3";

            // 🔥 CALL PYTHON TO DOWNLOAD + PROCESS
            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    "C:/Users/HP/Downloads/demo1/demo1/youtube_download.py",
                    url,
                    fileName
            );

            pb.inheritIO(); // ✅ shows Python logs in IntelliJ

            Process process = pb.start();

            // run transcription after youtube download
            ProcessBuilder transcribePb = new ProcessBuilder(
                    "python",
                    "C:/Users/HP/Downloads/demo1/demo1/transcribe.py",
                    fileName
            );

            transcribePb.inheritIO();
            Process transcribeProcess = transcribePb.start();
            transcribeProcess.waitFor();

            process.waitFor(); // 🔥 VERY IMPORTANT

            // SAVE DB
            User user = userRepository.findById(userId).orElseThrow();

            Video video = new Video();
            video.setFilename(fileName);


            String baseName = fileName.substring(0, fileName.lastIndexOf("."));

            video.setTranscript(baseName + "_output.txt");
            video.setSummary(baseName + "_summary.txt");
            video.setQuiz(baseName + "_quiz.txt");
            video.setUser(user);

            videoRepository.save(video);

            return ResponseEntity.ok(video.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error");
        }
    }

    // =========================
    // CREATE USER
    // =========================
    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

}