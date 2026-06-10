package com.example.summarizerbackend.controller;

import com.example.summarizerbackend.service.HuggingFaceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class SummaryController {

    private final HuggingFaceService service;

    public SummaryController(
            HuggingFaceService service) {

        this.service = service;
    }

    @PostMapping("/summarize")
    public String summarize(
            @RequestBody String text) {

        return service.summarize(text);
    }
}
