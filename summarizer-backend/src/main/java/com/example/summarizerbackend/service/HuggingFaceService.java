package com.example.summarizerbackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HuggingFaceService {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceService.class);

    private static final String API_URL =
            "https://router.huggingface.co/hf-inference/models/facebook/bart-large-cnn";

    @Value("${huggingface.token}")
    private String TOKEN;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 3000;

    // LRU cache — avoids re-calling HF for identical inputs (max 50 entries)
    private final Map<String, String> cache = new LinkedHashMap<>(50, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 50;
        }
    };

    private static final ObjectMapper mapper = new ObjectMapper();

    public String summarize(String text) {
        String cacheKey = text.trim();
        if (cache.containsKey(cacheKey)) {
            log.info(">>> Cache hit — returning cached summary");
            return cache.get(cacheKey);
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info(">>> summarize() attempt {}/{}", attempt, MAX_RETRIES);

                String payload = mapper.writeValueAsString(Map.of("inputs", text));

                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(120000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + TOKEN);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("x-wait-for-model", "true");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes());
                }

                int status = conn.getResponseCode();
                log.info(">>> Response code: {}", status);

                if (status == 503 || status == 504) {
                    log.warn(">>> Model not ready ({}), retrying in {}ms", status, RETRY_DELAY_MS);
                    conn.disconnect();
                    if (attempt < MAX_RETRIES) { Thread.sleep(RETRY_DELAY_MS); continue; }
                    return "Model is warming up — please retry in a few seconds.";
                }

                InputStream stream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
                StringBuilder body = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                    String line;
                    while ((line = br.readLine()) != null) body.append(line);
                }
                conn.disconnect();

                log.info(">>> Response body: {}", body.length() > 300 ? body.substring(0, 300) + "..." : body);

                if (status == 401) return "ERROR: Invalid token.";
                if (status == 403) return "ERROR: Token lacks Inference API permission.";
                if (status >= 400) return "ERROR: API error " + status;

                JsonNode root = mapper.readTree(body.toString());
                if (root.isArray() && root.size() > 0) {
                    String summary = root.get(0).get("summary_text").asText();
                    cache.put(cacheKey, summary);
                    log.info(">>> Summary generated and cached");
                    return summary;
                }

                return "No summary generated";

            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                log.error(">>> Attempt {} failed: {}", attempt, msg, e);
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    return "ERROR: " + msg;
                }
            }
        }
        return "ERROR: All retry attempts failed.";
    }
}
