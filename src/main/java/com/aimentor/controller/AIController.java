package com.aimentor.controller;

import com.aimentor.dto.CodeRequest;
import com.aimentor.dto.CodeReviewResponse;
import com.aimentor.dto.HistoryResponse;
import com.aimentor.service.AIService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    // ================= ASK =================
    @GetMapping("/ask")
    public String ask(@RequestParam String prompt) {

        if (prompt == null || prompt.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Prompt cannot be empty"
            );
        }

        return aiService.askAI(prompt);
    }

    // ================= REVIEW =================
    @PostMapping("/review")
    public CodeReviewResponse review(
            @RequestBody CodeRequest request,
            @RequestParam(required = false) String mode
    ) {

        // 🔐 VALIDATION
        if (request == null || request.getCode() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Code cannot be null"
            );
        }

        if (request.getCode().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Code cannot be empty"
            );
        }

        if (request.getCode().length() > 5000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Code too large (max 5000 chars)"
            );
        }

        // default mode
        if (mode == null || mode.isBlank()) {
            mode = "review";
        }

        return aiService.reviewCode(request.getCode(), mode);
    }

    // ================= HISTORY =================
    @GetMapping("/history")
    public List<HistoryResponse> getHistory() {
        return aiService.getHistory();
    }
}