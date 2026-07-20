package com.mabin.riskanalyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GitHubActionsService {

    @Value("${github.owner}")
    private String owner;

    @Value("${github.repo}")
    private String repo;

    @Value("${github.token}")
    private String token;

    private final RestClient restClient = RestClient.create();

    public String getLatestWorkflowRuns() {

        String url = String.format(
                "https://api.github.com/repos/%s/%s/actions/runs",
                owner,
                repo
        );

        return restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(String.class);
    }

    public Long getLatestRunId() {

        String url = String.format(
                "https://api.github.com/repos/%s/%s/actions/runs",
                owner,
                repo
        );

        Map<?, ?> response = restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("workflow_runs") == null) {
            throw new IllegalStateException(
                    "No GitHub Actions workflow runs were returned."
            );
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> runs =
                (List<Map<String, Object>>) response.get("workflow_runs");

        if (runs.isEmpty()) {
            throw new IllegalStateException(
                    "No GitHub Actions workflow runs were found."
            );
        }

        return ((Number) runs.get(0).get("id")).longValue();
    }

    public String getLatestJobLogs() {

        Long runId = getLatestRunId();

        String jobsUrl = buildJobsUrl(runId);

        Map<?, ?> response = restClient.get()
                .uri(jobsUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException(
                    "No GitHub Actions job information was returned."
            );
        }

        return response.toString();
    }

    public String buildWorkflowSummary() {
        return buildWorkflowSummary(getLatestRunId());
    }

    public String buildWorkflowSummary(Long runId) {

        String jobsUrl = buildJobsUrl(runId);

        Map<?, ?> response = restClient.get()
                .uri(jobsUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("jobs") == null) {
            throw new IllegalStateException(
                    "No GitHub Actions jobs were returned for run " + runId
            );
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> jobs =
                (List<Map<String, Object>>) response.get("jobs");

        if (jobs.isEmpty()) {
            throw new IllegalStateException(
                    "No jobs were found for GitHub workflow run " + runId
            );
        }

        StringBuilder summary = new StringBuilder();

        for (Map<String, Object> job : jobs) {

            summary.append("Workflow: ")
                    .append(job.get("workflow_name"))
                    .append("\n");

            summary.append("Job: ")
                    .append(job.get("name"))
                    .append("\n");

            summary.append("Status: ")
                    .append(job.get("status"))
                    .append("\n");

            summary.append("Conclusion: ")
                    .append(job.get("conclusion"))
                    .append("\n");

            Object stepsObject = job.get("steps");

            if (stepsObject instanceof List<?> steps) {
                for (Object stepObject : steps) {

                    if (!(stepObject instanceof Map<?, ?> step)) {
                        continue;
                    }

                    summary.append("Step ")
                            .append(step.get("name"))
                            .append(": ")
                            .append(step.get("conclusion"))
                            .append("\n");
                }
            }
        }

        return summary.toString();
    }

    private String buildJobsUrl(Long runId) {
        return String.format(
                "https://api.github.com/repos/%s/%s/actions/runs/%d/jobs",
                owner,
                repo,
                runId
        );
    }
}