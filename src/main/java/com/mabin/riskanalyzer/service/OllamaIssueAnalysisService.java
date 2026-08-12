package com.mabin.riskanalyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OllamaIssueAnalysisService {

    private final RestClient restClient;

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    public OllamaIssueAnalysisService() {
        this.restClient = RestClient.create();
    }

    public String generateRecommendation(
            String failureCause,
            String riskLevel
    ) {

        if (failureCause == null || failureCause.isBlank()) {
            return "Review the pipeline execution and identify the underlying issue before deployment.";
        }

        String lower = failureCause.toLowerCase();

        // HTTP 404
        if (lower.contains("404")
                || lower.contains("resource could not be found")
                || lower.contains("endpoint or resource could not be found")) {

            return "Verify that the requested API endpoint exists and that the URL and controller route mapping are correct. Correct any incorrect path or deployment configuration, then rerun the API test before deployment.";
        }

        // HTTP 500
        if (lower.contains("500")
                || lower.contains("internal server error")) {

            return "Inspect the backend application logs to identify the exception causing the HTTP 500 response, correct the failing application logic, and rerun the test before deployment.";
        }

        // HTTP 401
        if (lower.contains("401")
                || lower.contains("unauthorized")) {

            return "Verify the authentication credentials, tokens and authorization headers, then rerun the pipeline after correcting the authentication configuration.";
        }

        // HTTP 403
        if (lower.contains("403")
                || lower.contains("forbidden")) {

            return "Check the configured user roles, permissions and access-control rules, correct the authorization settings, and rerun the pipeline.";
        }

        // Otherwise use Ollama
        String systemPrompt = """
            You are a senior DevOps engineer.

            Generate one practical recommendation for resolving
            the supplied CI/CD pipeline failure.

            Rules:
            1. Use only the supplied failure cause.
            2. Do not invent additional errors.
            3. Do not change test expectations simply to make tests pass.
            4. Do not recommend verbose logging unless the failure cause
               explicitly indicates insufficient diagnostic information.
            5. Provide specific actionable remediation steps.
            6. Keep the recommendation concise.
            7. Return only plain text.
            """;

        String userPrompt = """
            Machine-learning risk level: %s

            Detected failure cause:
            %s

            Generate one specific recommendation explaining
            how to resolve this issue before deployment.
            """.formatted(
                riskLevel,
                failureCause
        );

        Map<String, Object> options = Map.of(
                "temperature", 0
        );

        Map<String, Object> requestBody =
                new LinkedHashMap<>();

        requestBody.put("model", ollamaModel);
        requestBody.put("system", systemPrompt);
        requestBody.put("prompt", userPrompt);
        requestBody.put("stream", false);
        requestBody.put("options", options);

        Map<?, ?> response = restClient.post()
                .uri(ollamaUrl + "/api/generate")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null
                || response.get("response") == null) {

            throw new IllegalStateException(
                    "Ollama returned an empty recommendation."
            );
        }

        String recommendation =
                String.valueOf(response.get("response"))
                        .trim();

        if (recommendation.isBlank()) {
            throw new IllegalStateException(
                    "Ollama returned a blank recommendation."
            );
        }

        return recommendation;
    }
}