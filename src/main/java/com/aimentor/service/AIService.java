package com.aimentor.service;

import com.aimentor.dto.CodeReviewResponse;
import com.aimentor.dto.HistoryResponse;
import com.aimentor.entity.ReviewHistory;
import com.aimentor.repositories.ReviewRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    private final ReviewRepository reviewRepository;
    private final ChatClient chatClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public AIService(ChatClient.Builder builder, ReviewRepository reviewRepository) {
        this.chatClient = builder.build();
        this.reviewRepository = reviewRepository;
    }

    // ================= HISTORY =================
    public List<HistoryResponse> getHistory() {
        return reviewRepository.findAll()
                .stream()
                .map(h -> new HistoryResponse(
                        h.getId(),
                        h.getCode(),
                        h.getResult()
                ))
                .collect(Collectors.toList());
    }

    // ================= PROMPT BUILDER =================
    private String buildPrompt(String code, String mode) {

        if ("explain".equalsIgnoreCase(mode)) {
            return """
            You are an expert teacher.

            Explain this code clearly:
            - What it does
            - Step-by-step logic
            - Example

            code:
            %s
            """.formatted(code);
        }

        if ("optimize".equalsIgnoreCase(mode)) {
            return """
            You are a performance expert.

            Optimize this code:
            - Problems
            - Better version
            - Explanation

            code:
            %s
            """.formatted(code);
        }

        // default = review
        return """
        You are a Senior Software Engineer.

        STRICT RULES:
        - Return ONLY valid JSON
        - ALL values MUST be STRINGS
        - DO NOT return nested objects
        - Escape newlines using \\n
        - No markdown or extra text

        format:
        {
          "bugs": "",
          "improvements": "",
          "timeComplexity": "",
          "betterApproach": "",
          "improvedCode": ""
        }

        code:
        %s
        """.formatted(code);
    }

    // ================= ASK AI =================
    public String askAI(String userPrompt) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prompt cannot be empty");
        }

        return chatClient.prompt()
                .user(userPrompt.trim())
                .call()
                .content();
    }

    // ================= REVIEW =================
    public CodeReviewResponse reviewCode(String code, String mode) {

        // 🔐 VALIDATION
        if (code == null || code.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code cannot be empty");
        }

        if (code.length() > 5000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code too large");
        }

        code = code.trim();

        if (mode == null || mode.isBlank()) {
            mode = "review";
        }

        String prompt = buildPrompt(code, mode);

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 🔥 CLEAN RESPONSE (CRITICAL FIX)
        aiResponse = aiResponse
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .replace("\n", " ")
                .replace("\r", "")
                .replaceAll("\\s+", " ")
                .trim();

        // ================= NON-JSON MODES =================
        if (!"review".equalsIgnoreCase(mode)) {
            CodeReviewResponse response = new CodeReviewResponse();
            response.setBugs("-");
            response.setImprovements("-");
            response.setTimeComplexity("-");
            response.setBetterApproach(mode.toUpperCase() + " MODE OUTPUT");
            response.setImprovedCode(aiResponse);
            return response;
        }

        // ================= EXTRACT JSON =================
        int start = aiResponse.indexOf("{");
        int end = aiResponse.lastIndexOf("}");

        if (start != -1 && end != -1) {
            aiResponse = aiResponse.substring(start, end + 1);
        }

        try {
            JsonNode node = mapper.readTree(aiResponse);

            CodeReviewResponse response = new CodeReviewResponse();

            response.setBugs(node.path("bugs").asText("No major bugs found"));
            response.setImprovements(node.path("improvements").asText("Code can be improved"));
            response.setTimeComplexity(node.path("timeComplexity").asText("Not specified"));
            response.setBetterApproach(node.path("betterApproach").asText("Consider optimizing logic"));
            response.setImprovedCode(node.path("improvedCode").asText(code));

            // save
            ReviewHistory history = new ReviewHistory();
            history.setCode(code);
            history.setResult(aiResponse.length() > 10000
                    ? aiResponse.substring(0, 10000)
                    : aiResponse);

            reviewRepository.save(history);

            return response;

        } catch (Exception e) {

            // 🔥 FALLBACK (NO FAILURE EVER)
            CodeReviewResponse fallback = new CodeReviewResponse();

            fallback.setBugs("AI response format issue (handled safely)");
            fallback.setImprovements("Try simplifying or rewriting the code");
            fallback.setTimeComplexity("Unknown");
            fallback.setBetterApproach("Use standard optimization techniques");
            fallback.setImprovedCode(aiResponse); // still show raw output

            return fallback;
        }
    }
}