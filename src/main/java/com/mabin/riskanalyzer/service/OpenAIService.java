package com.mabin.riskanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mabin.riskanalyzer.dto.RiskResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

//@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RiskResponseDTO analyzeLogsWithAI(String logs) {

        String prompt = """
                Analyze the following CI/CD pipeline logs.

                You must return ONLY valid JSON.
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
                - Do not simply summarize logs
                - Identify likely failure cause
                - Provide actionable recommendation

                CI/CD Logs:
                """ + logs;

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4.1-mini",
                "input", prompt
        );

        String rawResponse = restClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(rawResponse);

            String aiText = root
                    .path("output")
                    .get(0)
                    .path("content")
                    .get(0)
                    .path("text")
                    .asText();

            return objectMapper.readValue(aiText, RiskResponseDTO.class);

        } catch (Exception e) {
            RiskResponseDTO fallback = new RiskResponseDTO();
            fallback.setRiskScore(50);
            fallback.setRiskLevel("MEDIUM");
            fallback.setFailureCause("Unable to parse AI response");
            fallback.setRecommendation("Review CI/CD logs manually");
            fallback.setDeploymentDecision("REVIEW");
            return fallback;
        }
    }
}