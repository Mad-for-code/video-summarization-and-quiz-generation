package com.example.demo1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.ByteArrayOutputStream;

@Service
public class VideoService {

    private final String API_KEY ="f9f70acccbmshc29c412896592dbp187692jsn3256b5d7d1c3";
    private final String API_HOST = "deep-translate1.p.rapidapi.com";

    public String readFileContent(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR_READING_FILE";
        }
    }


    public byte[] generatePdf(String content) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph(content));

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public String normalizeLang(String lang) {

        switch (lang.toLowerCase()) {
            case "fr":
                return "fr"; // French
            case "de":
                return "de"; // German
            case "es":
                return "es"; // Spanish
            case "hi":
                return "hi"; // Hindi
            case "en":
                return "en"; // English
            default:
                return "en"; // fallback
        }
    }



    public String translateLargeText(String text, String lang) {

        int chunkSize = 300; // safe limit
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i += chunkSize) {

            String chunk = text.substring(i, Math.min(i + chunkSize, text.length()));

            String translatedChunk = translateText(chunk, lang);

            result.append(translatedChunk).append(" ");

            try {
                Thread.sleep(800); // avoid rate limit
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }


    public String translateText(String text, String targetLang) {

        try {
            RestTemplate restTemplate = new RestTemplate();

            String url = "https://deep-translate1.p.rapidapi.com/language/translate/v2";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", "f9f70acccbmshc29c412896592dbp187692jsn3256b5d7d1c3");
            headers.set("X-RapidAPI-Host", "deep-translate1.p.rapidapi.com");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // ✅ NORMALIZE HERE
            targetLang = normalizeLang(targetLang);

            Map<String, Object> body = new HashMap<>();
            body.put("q", text);
            body.put("source", "en");
            body.put("target", targetLang);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            if (response.getBody() == null) return text;

            Map data = (Map) response.getBody().get("data");
            if (data == null) return text;

            Map translations = (Map) data.get("translations");
            if (translations == null) return text;

            Object translated = translations.get("translatedText");

            return translated != null ? translated.toString() : text;

        } catch (Exception e) {
            return text;
        }
    }
}
