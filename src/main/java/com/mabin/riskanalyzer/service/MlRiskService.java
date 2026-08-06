package com.mabin.riskanalyzer.service;

import com.mabin.riskanalyzer.dto.MlRiskResponseDTO;
import com.mabin.riskanalyzer.dto.ModelMetricsDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Locale;
import java.util.Map;

@Service
public class MlRiskService {

    private static final double MINIMUM_CONFIDENCE = 0.50;

    private final RestClient restClient = RestClient.create();

    public MlRiskResponseDTO predictRisk(String logs) {

        String safeLogs = logs == null ? "" : logs.trim();

        Map<String, String> requestBody = Map.of(
                "logs", safeLogs
        );

        MlRiskResponseDTO response = restClient.post()
                .uri("http://ml-service:5001/predict-risk")
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(MlRiskResponseDTO.class);

        if (response == null) {
            throw new IllegalStateException(
                    "ML service returned an empty response."
            );
        }

        applyWorkflowValidation(safeLogs, response);

        return response;
    }

    private void applyWorkflowValidation(
            String logs,
            MlRiskResponseDTO response) {

        String normalizedLogs = logs.toLowerCase(Locale.ROOT);

        boolean containsFailure =
                normalizedLogs.contains("failed")
                        || normalizedLogs.contains("failure")
                        || normalizedLogs.contains("error")
                        || normalizedLogs.contains("exception")
                        || normalizedLogs.contains("cancelled")
                        || normalizedLogs.contains("timed out")
                        || normalizedLogs.contains("timeout")
                        || normalizedLogs.contains("connection refused");

        boolean containsSuccess =
                normalizedLogs.contains("conclusion: success")
                        || normalizedLogs.contains("conclusion=success")
                        || normalizedLogs.contains("\"conclusion\":\"success\"")
                        || normalizedLogs.contains("workflow completed successfully")
                        || normalizedLogs.contains("all jobs passed")
                        || normalizedLogs.contains("build succeeded");

        double confidence = response.getConfidence();

        /*
         * A successful workflow with no failure indicators should not be
         * classified as HIGH because of a low-confidence ML prediction.
         */
        if (containsSuccess && !containsFailure) {

            response.setRiskLevel("LOW");
            response.setRiskScore(20);
            response.setDeploymentDecision("PROCEED");
            response.setFailureCause(
                    "GitHub Actions workflow completed successfully"
            );
            response.setRecommendation(
                    "Deployment can proceed."
            );

            return;
        }

        /*
         * When the model is uncertain, require manual review instead of
         * returning HIGH or LOW with weak confidence.
         */
        if (confidence < MINIMUM_CONFIDENCE) {

            response.setRiskLevel("MEDIUM");
            response.setRiskScore(55);
            response.setDeploymentDecision("REVIEW");
            response.setFailureCause(
                    "ML prediction confidence is below the required threshold"
            );
            response.setRecommendation(
                    "Review the GitHub Actions logs manually before deployment."
            );
        }
    }

    public ModelMetricsDTO getMetrics() {

        return restClient.get()
                .uri("http://ml-service:5001/metrics")
                .retrieve()
                .body(ModelMetricsDTO.class);
    }
}