package com.example.summarizerbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class HuggingFaceService {

    private static final Logger log =
            LoggerFactory.getLogger(HuggingFaceService.class);

    private final String API_URL =
            "https://router.huggingface.co/hf-inference/models/facebook/bart-large-cnn";

    private final String TOKEN = "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";

    public String summarize(String text) {
        try {
            log.info(">>> summarize() called");
            log.info(">>> Input text: {}", text);
            log.info(">>> Calling URL: {}", API_URL);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String escaped = text.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");

            String payload = "{\"inputs\":\"" + escaped + "\"}";

            log.info(">>> Payload: {}", payload);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            log.info(">>> Response code: {}", responseCode);

            InputStream stream = responseCode >= 400
                    ? conn.getErrorStream()
                    : conn.getInputStream();

            StringBuilder response = new StringBuilder();

            try (BufferedReader br =
                         new BufferedReader(new InputStreamReader(stream))) {

                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            log.info(">>> Response body: {}", response);

            // Parse JSON response from Hugging Face
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.toString());

            if (jsonNode.isArray() && jsonNode.size() > 0) {
                return jsonNode.get(0)
                        .get("summary_text")
                        .asText();
            }

            return "No summary generated";

        } catch (Exception e) {
            log.error(">>> Exception: {}", e.getMessage(), e);
            return "ERROR: " + e.getMessage();
        }
    }}