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

        Map response = restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> runs =
                (List<Map<String, Object>>) response.get("workflow_runs");

        return ((Number) runs.get(0).get("id")).longValue();
    }

    public String getLatestJobLogs() {

        Long runId = getLatestRunId();

        String jobsUrl = String.format(
                "https://api.github.com/repos/%s/%s/actions/runs/%d/jobs",
                owner,
                repo,
                runId
        );

        Map response = restClient.get()
                .uri(jobsUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(Map.class);

        return response.toString();
    }

    public String buildWorkflowSummary() {

        Long runId = getLatestRunId();

        String jobsUrl = String.format(
                "https://api.github.com/repos/%s/%s/actions/runs/%d/jobs",
                owner,
                repo,
                runId
        );

        Map response = restClient.get()
                .uri(jobsUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> jobs =
                (List<Map<String, Object>>) response.get("jobs");

        StringBuilder summary = new StringBuilder();

        for (Map<String, Object> job : jobs) {

            summary.append("Workflow: ")
                    .append(job.get("workflow_name"))
                    .append("\n");

            summary.append("Status: ")
                    .append(job.get("status"))
                    .append("\n");

            summary.append("Conclusion: ")
                    .append(job.get("conclusion"))
                    .append("\n");

            List<Map<String, Object>> steps =
                    (List<Map<String, Object>>) job.get("steps");

            for (Map<String, Object> step : steps) {

                summary.append("Step ")
                        .append(step.get("name"))
                        .append(" ")
                        .append(step.get("conclusion"))
                        .append("\n");
            }
        }

        return summary.toString();
    }
}