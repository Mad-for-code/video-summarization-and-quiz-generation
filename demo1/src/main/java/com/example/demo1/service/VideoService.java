package com.example.demo1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.ByteArrayOutputStream;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.core.io.ClassPathResource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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

        String[] sentences = text.split("(?<=\\.)"); // split by full stop
        StringBuilder result = new StringBuilder();

        for (String sentence : sentences) {

            String translated = translateText(sentence, lang);
            result.append(translated).append(" ");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }

    public String translateText(String text, String targetLang) {

        try {
            String url = "https://translate.googleapis.com/translate_a/single"
                    + "?client=gtx"
                    + "&sl=en"
                    + "&tl=" + targetLang
                    + "&dt=t"
                    + "&q=" + URLEncoder.encode(text, "UTF-8");

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            if (response == null) return text;

            // ✅ Extract FULL translation (not just first word)
            StringBuilder translated = new StringBuilder();

            String[] parts = response.split("\\[\\[\\[|\\]\\]\\]");
            if (parts.length > 1) {
                String[] segments = parts[1].split("\\],\\[");
                for (String seg : segments) {
                    String[] words = seg.split(",");
                    String word = words[0].replace("\"", "");

                    try {
                        word = URLDecoder.decode(word, "UTF-8");
                    } catch (Exception e) {
                        // ignore if not decodable
                    }

                    translated.append(word).append(" ");
                }
            }

            String finalText = translated.toString().trim();
            return translated.toString().trim();

        } catch (Exception e) {
            System.out.println("⚠️ Translation failed: " + e.getMessage());
            return text;
        }
    }





    public byte[] generatePdf(String text) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // ✅ LOAD FONT FROM RESOURCES
            ClassPathResource resource = new ClassPathResource("fonts/NotoSans-Regular.ttf");

            if (text.matches(".*[\\u0A00-\\u0A7F].*")) { // Punjabi unicode range
               resource = new ClassPathResource("src/main/resources/fonts/NotoSansGurmukhi-Regular.ttf");
            }

            InputStream is = resource.getInputStream();



            PdfFont font = PdfFontFactory.createFont(
                    is.readAllBytes(),
                    PdfEncodings.IDENTITY_H
            );

            // ✅ APPLY FONT
            Paragraph para = new Paragraph(text).setFont(font).setFontSize(12);

            document.add(para);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
