package com.mabin.riskanalyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

        Object runId = runs.get(0).get("id");

        if (!(runId instanceof Number number)) {
            throw new IllegalStateException(
                    "The latest GitHub workflow run does not contain a valid ID."
            );
        }

        return number.longValue();
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
            return """
                    GitHub Actions job information was not returned.
                    Conclusion: failure
                    Failure cause: Unable to retrieve workflow job information.
                    """;
        }

        return response.toString();
    }

    public String buildWorkflowSummary() {
        return buildWorkflowSummary(getLatestRunId());
    }

    public String buildWorkflowSummary(Long runId) {

        Map<?, ?> workflowRun = getWorkflowRun(runId);
        Map<?, ?> jobsResponse = getWorkflowJobs(runId);

        if (jobsResponse == null || jobsResponse.get("jobs") == null) {
            return buildNoJobsSummary(
                    workflowRun,
                    runId,
                    "GitHub did not return job information for this workflow run."
            );
        }

        List<Map<String, Object>> jobs = extractJobs(jobsResponse);

        if (jobs.isEmpty()) {
            return buildNoJobsSummary(
                    workflowRun,
                    runId,
                    "No jobs were created for this workflow run."
            );
        }

        StringBuilder summary = new StringBuilder();

        appendWorkflowRunDetails(summary, workflowRun, runId);

        summary.append("Jobs created: ")
                .append(jobs.size())
                .append("\n");

        for (Map<String, Object> job : jobs) {

            summary.append("\n");

            summary.append("Job: ")
                    .append(valueOrUnknown(job.get("name")))
                    .append("\n");

            summary.append("Job status: ")
                    .append(valueOrUnknown(job.get("status")))
                    .append("\n");

            summary.append("Job conclusion: ")
                    .append(valueOrUnknown(job.get("conclusion")))
                    .append("\n");

            Object runnerName = job.get("runner_name");

            if (runnerName != null) {
                summary.append("Runner: ")
                        .append(runnerName)
                        .append("\n");
            }

            Object stepsObject = job.get("steps");

            if (stepsObject instanceof List<?> steps) {

                summary.append("Steps:\n");

                for (Object stepObject : steps) {

                    if (!(stepObject instanceof Map<?, ?> step)) {
                        continue;
                    }

                    summary.append("- ")
                            .append(valueOrUnknown(step.get("name")))
                            .append(": ")
                            .append(valueOrUnknown(step.get("conclusion")))
                            .append("\n");
                }
            }
        }

        return summary.toString();
    }

    private Map<?, ?> getWorkflowRun(Long runId) {

        String runUrl = String.format(
                "https://api.github.com/repos/%s/%s/actions/runs/%d",
                owner,
                repo,
                runId
        );

        return restClient.get()
                .uri(runUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(Map.class);
    }

    private Map<?, ?> getWorkflowJobs(Long runId) {

        String jobsUrl = buildJobsUrl(runId);

        return restClient.get()
                .uri(jobsUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractJobs(Map<?, ?> response) {

        Object jobsObject = response.get("jobs");

        if (!(jobsObject instanceof List<?> jobsList)) {
            return Collections.emptyList();
        }

        return (List<Map<String, Object>>) jobsList;
    }

    private String buildNoJobsSummary(
            Map<?, ?> workflowRun,
            Long runId,
            String reason
    ) {

        StringBuilder summary = new StringBuilder();

        appendWorkflowRunDetails(summary, workflowRun, runId);

        summary.append("Jobs created: 0\n");
        summary.append("Failure cause: ")
                .append(reason)
                .append("\n");

        String conclusion = workflowRun == null
                ? "unknown"
                : valueOrUnknown(workflowRun.get("conclusion"));

        if ("failure".equalsIgnoreCase(conclusion)) {
            summary.append(
                    "Risk indicator: The workflow failed before job execution or job information was unavailable.\n"
            );
            summary.append(
                    "Possible cause: Invalid workflow YAML, configuration error, permission issue, or failure before job initialization.\n"
            );
        } else if ("cancelled".equalsIgnoreCase(conclusion)) {
            summary.append(
                    "Risk indicator: The workflow was cancelled before jobs completed.\n"
            );
        } else if ("skipped".equalsIgnoreCase(conclusion)) {
            summary.append(
                    "Risk indicator: The workflow was skipped and did not create executable jobs.\n"
            );
        } else {
            summary.append(
                    "Risk indicator: Workflow job information is unavailable.\n"
            );
        }

        return summary.toString();
    }

    private void appendWorkflowRunDetails(
            StringBuilder summary,
            Map<?, ?> workflowRun,
            Long runId
    ) {

        summary.append("Workflow run ID: ")
                .append(runId)
                .append("\n");

        if (workflowRun == null) {
            summary.append("Workflow: Unknown\n");
            summary.append("Status: unknown\n");
            summary.append("Conclusion: unknown\n");
            return;
        }

        summary.append("Workflow: ")
                .append(valueOrUnknown(workflowRun.get("name")))
                .append("\n");

        summary.append("Event: ")
                .append(valueOrUnknown(workflowRun.get("event")))
                .append("\n");

        summary.append("Status: ")
                .append(valueOrUnknown(workflowRun.get("status")))
                .append("\n");

        summary.append("Conclusion: ")
                .append(valueOrUnknown(workflowRun.get("conclusion")))
                .append("\n");

        summary.append("Branch: ")
                .append(valueOrUnknown(workflowRun.get("head_branch")))
                .append("\n");
    }

    private String valueOrUnknown(Object value) {

        if (value == null) {
            return "unknown";
        }

        String text = String.valueOf(value);

        if (text.isBlank() || "null".equalsIgnoreCase(text)) {
            return "unknown";
        }

        return text;
    }

    private String buildJobsUrl(Long runId) {

        return String.format(
                "https://api.github.com/repos/%s/%s/actions/runs/%d/jobs",
                owner,
                repo,
                runId
        );
    }

    public Long getLatestJobId() {

        String runsUrl = String.format(
                "https://api.github.com/repos/%s/%s/actions/runs",
                owner,
                repo
        );

        Map<?, ?> runsResponse = restClient.get()
                .uri(runsUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(Map.class);

        if (runsResponse == null
                || runsResponse.get("workflow_runs") == null) {

            throw new IllegalStateException(
                    "No GitHub workflow runs were returned."
            );
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> runs =
                (List<Map<String, Object>>)
                        runsResponse.get("workflow_runs");

        for (Map<String, Object> run : runs) {

            Object runIdObject = run.get("id");

            if (!(runIdObject instanceof Number runIdNumber)) {
                continue;
            }

            Long runId = runIdNumber.longValue();

            Map<?, ?> jobsResponse = getWorkflowJobs(runId);

            if (jobsResponse == null
                    || jobsResponse.get("jobs") == null) {
                continue;
            }

            List<Map<String, Object>> jobs =
                    extractJobs(jobsResponse);

            if (jobs.isEmpty()) {
                continue;
            }

            Object jobIdObject = jobs.get(0).get("id");

            if (jobIdObject instanceof Number jobIdNumber) {
                return jobIdNumber.longValue();
            }
        }

        throw new IllegalStateException(
                "No recent GitHub workflow containing jobs was found."
        );
    }

    public String downloadLatestJobLog() {

        Long jobId = getLatestJobId();

        String url = String.format(
                "https://api.github.com/repos/%s/%s/actions/jobs/%d/logs",
                owner,
                repo,
                jobId
        );

        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + token
                    )
                    .header(
                            HttpHeaders.ACCEPT,
                            "application/vnd.github+json"
                    )
                    .header(
                            "X-GitHub-Api-Version",
                            "2022-11-28"
                    )
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "GitHub log download failed. HTTP status: "
                                + response.statusCode()
                );
            }

            String jobLog = response.body();

            if (jobLog == null || jobLog.isBlank()) {
                throw new IllegalStateException(
                        "GitHub returned an empty job log."
                );
            }

            System.out.println(
                    "Downloaded GitHub job log length: "
                            + jobLog.length()
            );

            return jobLog;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "GitHub job-log download was interrupted.",
                    exception
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to download the GitHub job log.",
                    exception
            );
        }
    }

}