package com.example.demo1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UploadController {

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {

            String uploadDir = "C:/Users/HP/Downloads/demo1/demo1/uploads/";
            File folder = new File(uploadDir);

            // delete old AI outputs
            new File(uploadDir + "output.txt").delete();
            new File(uploadDir + "summary.txt").delete();
            new File(uploadDir + "quiz.txt").delete();

            if (!folder.exists()) {
                folder.mkdirs();
            }

            String filePath = uploadDir + file.getOriginalFilename();
            file.transferTo(new File(filePath));

            // run python but DO NOT wait
            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    "C:/Users/HP/Downloads/demo1/demo1/transcribe.py"
            );

            pb.start();

            return ResponseEntity.ok("Upload successful");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed");
        }
    }

    @GetMapping("/transcript")
    public String getTranscript() throws IOException {

        File file = new File("C:/Users/HP/Downloads/demo1/demo1/uploads/output.txt");

        if (!file.exists()) {
            return "Transcript not ready yet";
        }

        return new String(java.nio.file.Files.readAllBytes(file.toPath()));
    }

    @GetMapping("/summary")
    public String getSummary() throws IOException {

        File file = new File("C:/Users/HP/Downloads/demo1/demo1/uploads/summary.txt");

        if (!file.exists()) {
            return "Summary not ready yet";
        }

        return new String(java.nio.file.Files.readAllBytes(file.toPath()));
    }

    @GetMapping("/quiz")
    public String getQuiz() throws Exception {

        File file = new File("C:/Users/HP/Downloads/demo1/demo1/uploads/quiz.txt");

        if (!file.exists()) {
            return "Quiz not ready yet";
        }

        return new String(java.nio.file.Files.readAllBytes(file.toPath()));
    }

}