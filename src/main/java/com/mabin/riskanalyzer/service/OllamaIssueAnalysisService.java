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

        String systemPrompt = """
                You are a senior DevOps engineer.

                Your task is to generate one practical recommendation
                for resolving a detected CI/CD pipeline issue.

                Rules:

                1. Use only the supplied failure cause.
                2. Do not invent additional errors.
                3. Do not change test expectations merely to make a test pass.
                4. When an application returns HTTP 500 instead of HTTP 200,
                   recommend investigating and fixing the backend error.
                5. Give clear, actionable steps.
                6. Keep the recommendation concise.
                7. Return only plain text.
                8. Do not return JSON, Markdown, headings or bullet points.
                """;

        String userPrompt = """
                Machine-learning risk level: %s

                Detected failure cause:
                %s

                Generate one specific DevOps recommendation that explains
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

        System.out.println(
                "===== OLLAMA RECOMMENDATION ====="
        );
        System.out.println(recommendation);
        System.out.println(
                "================================="
        );

        return recommendation;
    }
}