package com.mabin.riskanalyzer.service;

import org.springframework.stereotype.Service;

@Service
public class IssueDetectionService {

    public String detect(String log) {

        if (log == null || log.isBlank()) {
            return "No workflow log was available for analysis.";
        }

        String lower = log.toLowerCase();

        // 1. HTTP 500 returned instead of HTTP 200
        if (lower.contains("expected http status 200")
                && lower.contains("received 500")) {

            return "UserServiceTest failed because the endpoint returned HTTP 500 instead of the expected HTTP 200.";
        }

        // 2. Generic assertion failure
        if (lower.contains("assertionerror")) {
            return "A unit test assertion failed because the actual result did not match the expected result.";
        }

        // 3. JUnit test failure
        if (lower.contains("tests run:")
                && lower.contains("failures:")
                && !lower.contains("failures: 0")) {

            return "One or more JUnit tests failed during the pipeline.";
        }

        // 4. Test timeout
        if (lower.contains("test timed out")
                || lower.contains("test timeout")) {

            return "A unit test exceeded the allowed execution time.";
        }

        // 5. Mockito verification failure
        if (lower.contains("wanted but not invoked")
                || lower.contains("too few actual invocations")
                || lower.contains("verificationwantedbutnotinvoked")) {

            return "A Mockito verification failed because the expected method interaction did not occur.";
        }

        // 6. Maven build failure
        if (lower.contains("build failure")
                || lower.contains("[info] build failure")) {

            return "The Maven build failed.";
        }

        // 7. Compilation failure
        if (lower.contains("compilation failure")
                || lower.contains("compilation error")
                || lower.contains("cannot find symbol")) {

            return "The project failed to compile because of a source-code or dependency error.";
        }

        // 8. Maven dependency resolution failure
        if (lower.contains("could not resolve dependencies")
                || lower.contains("failed to collect dependencies")
                || lower.contains("dependency resolution exception")) {

            return "Maven could not resolve one or more project dependencies.";
        }

        // 9. Gradle build failure
        if (lower.contains("gradle build failed")
                || lower.contains("execution failed for task")) {

            return "The Gradle build failed while executing a build task.";
        }

        // 10. Spring Bean creation failure
        if (lower.contains("beancreationexception")) {
            return "Spring Boot failed to create a required application bean.";
        }

        // 11. Missing Spring bean
        if (lower.contains("nosuchbeandefinitionexception")
                || lower.contains("no qualifying bean")) {

            return "Spring Boot could not find a required bean definition.";
        }

        // 12. Spring application context failure
        if (lower.contains("failed to load applicationcontext")
                || lower.contains("applicationcontext exception")) {

            return "The Spring Boot application context failed to start.";
        }

        // 13. Port already in use
        if (lower.contains("port already in use")
                || lower.contains("address already in use")) {

            return "The application could not start because the configured port is already in use.";
        }

        // 14. Spring Boot startup failure
        if (lower.contains("application run failed")
                || lower.contains("failed to start application")) {

            return "The Spring Boot application failed during startup.";
        }

        // 15. Docker build failure
        if (lower.contains("docker build failed")
                || lower.contains("failed to solve")
                || lower.contains("dockerfile:")
                && lower.contains("exit code")) {

            return "The Docker image build failed.";
        }

        // 16. Docker image not found
        if (lower.contains("manifest unknown")
                || lower.contains("pull access denied")
                || lower.contains("image not found")) {

            return "Docker could not find or access the requested image.";
        }

        // 17. Docker login failure
        if (lower.contains("docker login failed")
                || lower.contains("unauthorized: authentication required")) {

            return "Docker registry authentication failed.";
        }

        // 18. Docker push failure
        if (lower.contains("docker push failed")
                || lower.contains("denied: requested access to the resource is denied")) {

            return "The Docker image could not be pushed to the registry.";
        }

        // 19. Kubernetes ImagePullBackOff
        if (lower.contains("imagepullbackoff")) {
            return "Kubernetes could not pull the required container image.";
        }

        // 20. Kubernetes ErrImagePull
        if (lower.contains("errimagepull")) {
            return "Kubernetes failed while pulling the container image.";
        }

        // 21. Kubernetes CrashLoopBackOff
        if (lower.contains("crashloopbackoff")) {
            return "The Kubernetes container repeatedly crashed after startup.";
        }

        // 22. Kubernetes deployment failure
        if (lower.contains("deployment failed")
                || lower.contains("rollout status")
                && lower.contains("failed")) {

            return "The Kubernetes deployment failed to complete successfully.";
        }

        // 23. Pod failure
        if (lower.contains("pod failed")
                || lower.contains("pod status: failed")) {

            return "A Kubernetes pod entered a failed state.";
        }

        // 24. Database connection refused
        if (lower.contains("connection refused")
                && (lower.contains("postgres")
                || lower.contains("mysql")
                || lower.contains("database")
                || lower.contains("jdbc"))) {

            return "The application could not connect to the database because the connection was refused.";
        }

        // 25. SQL exception
        if (lower.contains("sqlexception")
                || lower.contains("sqlstate")) {

            return "A database operation failed with an SQL exception.";
        }

        // 26. Database authentication failure
        if (lower.contains("password authentication failed")
                || lower.contains("access denied for user")) {

            return "Database authentication failed because the configured credentials were rejected.";
        }

        // 27. Database timeout
        if (lower.contains("connection timed out")
                || lower.contains("database timeout")) {

            return "The database connection timed out.";
        }

        // 28. HTTP 401
        if (lower.contains("401 unauthorized")
                || lower.contains("http status 401")) {

            return "The pipeline request failed because authentication was not accepted.";
        }

        // 29. HTTP 403
        if (lower.contains("403 forbidden")
                || lower.contains("http status 403")) {

            return "The pipeline request was denied because the caller did not have sufficient permission.";
        }

        // 30. HTTP 404
        if (lower.contains("404 not found")
                || lower.contains("http status 404")) {

            return "The requested API endpoint or resource could not be found.";
        }

        // 31. NullPointerException
        if (lower.contains("nullpointerexception")) {
            return "The application failed because code attempted to use a null object reference.";
        }

        // 32. OutOfMemoryError
        if (lower.contains("outofmemoryerror")) {
            return "The application ran out of JVM memory during pipeline execution.";
        }

        // 33. StackOverflowError
        if (lower.contains("stackoverflowerror")) {
            return "The application exceeded the JVM call-stack limit.";
        }

        // 34. FileNotFoundException
        if (lower.contains("filenotfoundexception")
                || lower.contains("no such file or directory")) {

            return "A required file or directory could not be found.";
        }

        // 35. SSL certificate error
        if (lower.contains("sslhandshakeexception")
                || lower.contains("certificate verify failed")
                || lower.contains("unable to find valid certification path")) {

            return "The pipeline failed because SSL certificate validation was unsuccessful.";
        }

        // 36. Permission denied
        if (lower.contains("permission denied")) {
            return "The pipeline failed because the process did not have permission to access a required resource.";
        }

        // 37. Generic timeout
        if (lower.contains("timed out")
                || lower.contains("timeout exception")) {

            return "A pipeline operation exceeded its allowed execution time.";
        }

        // 38. Generic non-zero exit
        if (lower.contains("process completed with exit code 1")
                || lower.contains("exit code 1")) {

            return "The workflow failed because a pipeline command exited with code 1.";
        }

        // Database connection refused
        if (lower.contains("connection")
                && lower.contains("refused")
                && (lower.contains("postgres")
                || lower.contains("postgresql")
                || lower.contains("mysql")
                || lower.contains("jdbc")
                || lower.contains("database"))) {

            return "The application could not connect to the database because the database connection was refused.";
        }

        return "No specific failure was detected from the workflow log.";
    }
}