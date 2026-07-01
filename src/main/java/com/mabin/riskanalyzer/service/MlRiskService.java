package com.mabin.riskanalyzer.service;

import com.mabin.riskanalyzer.dto.MlRiskResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class MlRiskService {

    private final RestClient restClient = RestClient.create();

    public MlRiskResponseDTO predictRisk(String logs) {
        Map<String, String> requestBody = Map.of("logs", logs);

        MlRiskResponseDTO response = restClient.post()
                .uri("http://ml-service:5001/predict-risk")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(MlRiskResponseDTO.class);

        if (response != null) {
            response.setRecommendation(generateRecommendation(logs));
        }

        return response;
    }

    private String generateRecommendation(String logs) {
        String log = logs.toLowerCase();

        if (log.contains("docker")) {
            return "Check Dockerfile configuration and dependency installation steps.";
        }

        if (log.contains("unit test") || log.contains("test failed")) {
            return "Review failing test cases and fix code defects before deployment.";
        }

        if (log.contains("dependency")) {
            return "Resolve dependency version conflicts and rebuild the project.";
        }

        if (log.contains("deployment failed")) {
            return "Check deployment configuration and service availability.";
        }

        if (log.contains("connection refused")) {
            return "Verify network connectivity and target service status.";
        }

        if (log.contains("success") || log.contains("passed")) {
            return "Deployment can proceed.";
        }

        return "Review CI/CD logs and resolve identified issues before deployment.";
    }
}