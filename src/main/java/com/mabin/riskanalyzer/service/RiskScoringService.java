package com.mabin.riskanalyzer.service;

import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {

    public int calculateRiskScore(String logs) {
        logs = logs.toLowerCase();

        int testRisk = 0;
        int buildRisk = 0;
        int dependencyRisk = 0;
        int dockerRisk = 0;
        int deploymentRisk = 0;
        int infrastructureRisk = 0;

        if (logs.contains("test failed") || logs.contains("unit test") || logs.contains("assertion failed")) {
            testRisk = 100;
        }

        if (logs.contains("build failed") || logs.contains("compilation error")) {
            buildRisk = 100;
        }

        if (logs.contains("dependency") || logs.contains("version conflict")) {
            dependencyRisk = 100;
        }

        if (logs.contains("docker") && (logs.contains("error") || logs.contains("failed"))) {
            dockerRisk = 100;
        }

        if (logs.contains("deployment failed") || logs.contains("connection refused")) {
            deploymentRisk = 100;
        }

        if (logs.contains("memory") || logs.contains("timeout") || logs.contains("service unavailable")) {
            infrastructureRisk = 100;
        }

        int score =
                (testRisk * 30 +
                        buildRisk * 20 +
                        dependencyRisk * 15 +
                        dockerRisk * 15 +
                        deploymentRisk * 10 +
                        infrastructureRisk * 10) / 100;

        return Math.min(score, 100);
    }

    public String getRiskLevel(int score) {
        if (score <= 30) {
            return "LOW";
        } else if (score <= 70) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }

    public String getDeploymentDecision(int score) {
        if (score <= 30) {
            return "PROCEED";
        } else if (score <= 70) {
            return "REVIEW";
        } else {
            return "STOP";
        }
    }
}