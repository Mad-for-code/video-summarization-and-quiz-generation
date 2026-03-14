package com.example.demo1.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class SpeechToTextService {

    public String convertAudioToText(String audioPath) {

        try {

            ProcessBuilder builder = new ProcessBuilder(
                    "python",
                    "transcribe.py",
                    audioPath
            );

            Process process = builder.start();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder text = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                text.append(line);
            }

            return text.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Error converting audio";
    }
}
