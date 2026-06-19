package com.mabin.riskanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabin.riskanalyzer.dto.RiskResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RiskResponseDTO analyzeLogsWithAI(String logs) {

        try {
            String prompt = """
                Analyze the following CI/CD pipeline logs.

                Return ONLY valid JSON.
                Do not include markdown.
                Do not include explanation outside JSON.

                JSON format:
                {
                  "riskScore": 0,
                  "riskLevel": "LOW",
                  "failureCause": "Short failure cause",
                  "recommendation": "Actionable recommendation",
                  "deploymentDecision": "PROCEED"
                }

                Rules:
                - riskScore must be between 0 and 100
                - riskLevel must be LOW, MEDIUM, or HIGH
                - deploymentDecision must be PROCEED, REVIEW, or STOP

                CI/CD Logs:
                """ + logs;

            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(content));

            String rawResponse = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(rawResponse);

            String aiText = root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            aiText = aiText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            return objectMapper.readValue(aiText, RiskResponseDTO.class);

        } catch (Exception e) {
            RiskResponseDTO fallback = new RiskResponseDTO();
            fallback.setRiskScore(50);
            fallback.setRiskLevel("MEDIUM");
            fallback.setFailureCause("Gemini AI service error: " + e.getMessage());
            fallback.setRecommendation("Check Gemini API key, API URL, model name, and backend logs");
            fallback.setDeploymentDecision("REVIEW");
            return fallback;
        }
    }
}