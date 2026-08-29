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

        // LOW-risk successful pipeline
        if ("LOW".equalsIgnoreCase(riskLevel)
                && failureCause != null
                && failureCause.toLowerCase()
                .contains("no specific failure")) {

            return "No corrective action is required. The pipeline completed without a specific failure and can proceed to deployment.";
        }

        // Missing failure information
        if (failureCause == null || failureCause.isBlank()) {

            if ("LOW".equalsIgnoreCase(riskLevel)) {
                return "No corrective action is required. The pipeline can proceed to deployment.";
            }

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

            return "Verify the authentication credentials, tokens and authorization headers, correct the authentication configuration, and rerun the pipeline.";
        }

        // HTTP 403
        if (lower.contains("403")
                || lower.contains("forbidden")) {

            return "Check the configured user roles, permissions and access-control rules, correct the authorization settings, and rerun the pipeline.";
        }

        // Compilation failure
        if (lower.contains("compilation")
                || lower.contains("cannot find symbol")) {

            return "Correct the compilation error by checking the referenced classes, methods, variables or dependencies, then rebuild the application before deployment.";
        }

        // Maven dependency failure
        if (lower.contains("dependency")
                && (lower.contains("resolve")
                || lower.contains("resolution"))) {

            return "Verify the Maven dependency coordinates, repository configuration and dependency versions, resolve the missing dependency, and rerun the build.";
        }

        // Docker failure
        if (lower.contains("docker")
                && (lower.contains("failed")
                || lower.contains("failure"))) {

            return "Inspect the Docker build error, verify the Dockerfile, base image and build context, correct the failing configuration, and rebuild the image before deployment.";
        }

        // Kubernetes ImagePullBackOff
        if (lower.contains("imagepullbackoff")
                || lower.contains("errimagepull")) {

            return "Verify that the container image exists, confirm the image name and tag, check registry credentials, and redeploy after Kubernetes can successfully pull the image.";
        }

        // Kubernetes CrashLoopBackOff
        if (lower.contains("crashloopbackoff")) {

            return "Inspect the container logs and application startup configuration, fix the error causing repeated container crashes, and redeploy the application.";
        }

        // Database connection failure
        if (lower.contains("connection refused")
                && (lower.contains("database")
                || lower.contains("postgres")
                || lower.contains("mysql")
                || lower.contains("jdbc"))) {

            return "Verify that the database service is running and reachable, check the host, port and connection configuration, and rerun the pipeline after connectivity is restored.";
        }

        // Database authentication failure
        if (lower.contains("password authentication failed")
                || lower.contains("access denied for user")) {

            return "Verify the database username and password, update the application database configuration, and rerun the pipeline after confirming successful authentication.";
        }

        // Unit / integration test failure
        if (lower.contains("test")
                && (lower.contains("failed")
                || lower.contains("assertion"))) {

            return "Investigate the failing test and the application behaviour that caused it, correct the underlying code or configuration issue, and rerun the test suite before deployment.";
        }

        // Security vulnerability
        if (lower.contains("critical vulnerabil")
                || lower.contains("high severity vulnerabil")) {

            return "Review the affected dependency or component, upgrade or replace the vulnerable version, rerun the security scan, and deploy only after the critical or high-severity vulnerability is resolved.";
        }

        // Generic HIGH risk fallback before Ollama
        if ("HIGH".equalsIgnoreCase(riskLevel)
                && lower.contains("exit code 1")) {

            return "Identify the command or pipeline step that exited with code 1, correct the underlying failure, and rerun the pipeline successfully before deployment.";
        }

        // Otherwise use Ollama
        String systemPrompt = """
                You are a senior DevOps engineer.

                Generate one practical recommendation for resolving
                the supplied CI/CD pipeline issue.

                Rules:
                1. Use only the supplied failure cause.
                2. Do not invent additional errors.
                3. Do not change test expectations simply to make tests pass.
                4. Do not recommend verbose logging unless the failure cause
                   explicitly indicates insufficient diagnostic information.
                5. Provide specific actionable remediation steps.
                6. Keep the recommendation concise.
                7. Return only plain text.
                8. Do not return Markdown, JSON, headings or bullet points.
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