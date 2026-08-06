package com.mabin.riskanalyzer.service;

import org.springframework.stereotype.Service;

@Service
public class IssueDetectionService {

    public String detect(String log) {

        String lower = log.toLowerCase();

        // HTTP 500 test failure
        if (lower.contains("expected http status 200")
                && lower.contains("received 500")) {

            return "UserServiceTest failed because the endpoint returned HTTP 500 instead of the expected HTTP 200.";
        }

        // Assertion error
        if (lower.contains("assertionerror")) {

            return "A unit test assertion failed.";
        }

        // Maven build failure
        if (lower.contains("build failure")) {

            return "Maven build failed.";
        }

        // Compilation error
        if (lower.contains("compilation failure")
                || lower.contains("compilation error")) {

            return "Compilation failed.";
        }

        // Docker
        if (lower.contains("docker build failed")) {

            return "Docker image build failed.";
        }

        // Kubernetes
        if (lower.contains("kubernetes deployment failed")) {

            return "Kubernetes deployment failed.";
        }

        // Dependency
        if (lower.contains("could not resolve dependencies")) {

            return "Dependency resolution failed.";
        }

        // Connection refused
        if (lower.contains("connection refused")) {

            return "Application could not connect to a required service.";
        }

        // Timeout
        if (lower.contains("timeout")) {

            return "Operation timed out.";
        }

        // Out of memory
        if (lower.contains("outofmemoryerror")) {

            return "Application ran out of memory.";
        }

        // Exit code
        if (lower.contains("exit code 1")) {

            return "Workflow failed with exit code 1.";
        }

        return "No specific failure was detected from the workflow log.";
    }
}