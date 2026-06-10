package com.example.summarizerbackend.controller;

import com.example.summarizerbackend.service.HuggingFaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class SummaryController {

    private final HuggingFaceService service;

    public SummaryController(HuggingFaceService service) {
        this.service = service;
    }

    @PostMapping("/summarize")
    public ResponseEntity<String> summarize(@RequestBody String text) {
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body("ERROR: Input text is empty.");
        }
        String result = service.summarize(text);
        if (result.startsWith("ERROR:")) {
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
