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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

            // ✅ UNIQUE NAME
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            File dest = new File(uploadDir + fileName);
            file.transferTo(dest);

            // ✅ RUN PYTHON WITH FILENAME
            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    "C:/Users/HP/Downloads/demo1/demo1/transcribe.py",
                    fileName
            );
            pb.start();

            // ✅ SAVE IN DB
            User user = userRepository.findById(userId).orElseThrow();

            Video video = new Video();
            video.setFilename(fileName);

            // 🔥 IMPORTANT: STORE FILE REFERENCES
            video.setTranscript(fileName + "_output.txt");
            video.setSummary(fileName + "_summary.txt");
            video.setQuiz(fileName + "_quiz.txt");

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
    @GetMapping(value = "/transcript/{id}", produces = "text/plain;charset=UTF-8")
    public String getTranscript(
            @PathVariable Long id,
            @RequestParam(defaultValue = "en") String lang
    ) {

        Video video = videoRepository.findById(id).orElse(null);

        if (video == null || video.getTranscript() == null) {
            return "NOT_READY";
        }

        String fullPath = uploadDir + video.getTranscript();

        File file = new File(fullPath);

        if (!file.exists()) {
            return "NOT_READY";
        }

        String text = videoService.readFileContent(fullPath);

        if (!lang.equals("en")) {
            text = videoService.translateLargeText(text, lang);
        }

        return text;
    }
    // =========================
    // SUMMARY
    // =========================
    @GetMapping(value = "/summary/{id}", produces = "text/plain;charset=UTF-8")
    public String getSummary(
            @PathVariable Long id,
            @RequestParam(defaultValue = "en") String lang
    ) {

        Video video = videoRepository.findById(id).orElse(null);

        if (video == null || video.getSummary() == null) {
            return "NOT_READY";
        }

        // ✅ FULL PATH
        String fullPath = uploadDir + video.getSummary();

        File file = new File(fullPath);

        if (!file.exists()) {
            return "NOT_READY";
        }

        // ✅ READ FILE CONTENT
        String text = videoService.readFileContent(fullPath);

        // ✅ TRANSLATION (SAFE)
        if (!lang.equals("en")) {
            text = videoService.translateLargeText(text, lang);
        }

        return text;
    }

    // =========================
    // QUIZ
    // =========================
    @GetMapping(value = "/quiz/{id}", produces = "text/plain;charset=UTF-8")
    public String getQuiz(@PathVariable Long id) {

        Video video = videoRepository.findById(id).orElse(null);

        if (video == null || video.getQuiz() == null) {
            return "NOT_READY";
        }

        String fullPath = uploadDir + video.getQuiz();

        File file = new File(fullPath);

        if (!file.exists()) {
            return "NOT_READY";
        }

        return videoService.readFileContent(fullPath);
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

        // ✅ Read transcript
        String text = videoService.readFileContent(fullPath);

        // ✅ Translate if needed
        if (!lang.equals("en")) {
            text = videoService.translateLargeText(text, lang);
        }

        // ✅ Generate PDF
        byte[] pdfBytes = videoService.generatePdf(text);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=transcript.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdfBytes);
    }

    // =========================
    // CREATE USER
    // =========================
    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

}