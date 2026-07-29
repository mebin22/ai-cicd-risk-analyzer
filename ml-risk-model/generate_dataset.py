import csv
import random
from pathlib import Path

random.seed(42)

OUTPUT_FILE = Path("training_dataset.csv")
TOTAL_ROWS = 1000

low_templates = [
    "Build completed successfully and all tests passed",
    "Maven build successful with no compilation errors",
    "Docker image built successfully and pushed to registry",
    "Deployment completed successfully to Kubernetes",
    "All unit tests and integration tests passed",
    "Pipeline completed successfully with no errors",
    "Application deployed successfully and health check passed",
    "Code quality checks passed and coverage threshold achieved",
    "Security scan completed with no vulnerabilities",
    "All workflow stages completed successfully",
]

medium_templates = [
    "Flaky test detected but retry passed successfully",
    "Dependency version warning detected during build",
    "Deployment timeout occurred but retry succeeded",
    "Memory usage warning detected during compilation",
    "Code coverage below recommended threshold",
    "SonarQube reported code quality warnings",
    "Docker image size exceeded recommended limit",
    "Deprecated dependency detected in project",
    "Network timeout occurred while downloading dependencies",
    "Minor configuration warning detected during deployment",
]

high_templates = [
    "Unit test failed with assertion error and build stopped",
    "Docker build failed because base image was not found",
    "Kubernetes deployment failed with ImagePullBackOff",
    "Application deployment failed because connection was refused",
    "Maven build failed due to unresolved dependency",
    "Security scan found critical vulnerabilities",
    "Integration tests failed and pipeline was terminated",
    "Compilation failed because symbol could not be resolved",
    "Container crashed with OutOfMemoryError",
    "Deployment failed because service was unavailable",
]

services = [
    "UserService",
    "OrderService",
    "PaymentService",
    "InventoryService",
    "NotificationService",
    "GatewayService",
    "AuthenticationService",
]

tests = [
    "UserServiceTest",
    "OrderControllerTest",
    "PaymentIntegrationTest",
    "TaskServiceTest",
    "LoginServiceTest",
    "RepositoryTest",
]

environments = [
    "development",
    "testing",
    "staging",
    "production",
]

dependencies = [
    "Spring Boot",
    "PostgreSQL",
    "Jackson",
    "JUnit",
    "Mockito",
    "Docker",
    "Kubernetes client",
]

def add_variation(template: str) -> str:
    additions = [
        f" Service: {random.choice(services)}.",
        f" Environment: {random.choice(environments)}.",
        f" Test: {random.choice(tests)}.",
        f" Dependency: {random.choice(dependencies)}.",
        f" Workflow run number {random.randint(1000, 9999)}.",
        f" Job completed in {random.randint(10, 600)} seconds.",
        "",
    ]

    return template + random.choice(additions)

rows = []

class_counts = {
    "LOW": 334,
    "MEDIUM": 333,
    "HIGH": 333,
}

for risk, count in class_counts.items():
    if risk == "LOW":
        templates = low_templates
    elif risk == "MEDIUM":
        templates = medium_templates
    else:
        templates = high_templates

    for _ in range(count):
        template = random.choice(templates)
        logs = add_variation(template)

        rows.append({
            "logs": logs,
            "risk": risk,
        })

random.shuffle(rows)

with OUTPUT_FILE.open("w", newline="", encoding="utf-8") as file:
    writer = csv.DictWriter(file, fieldnames=["logs", "risk"])
    writer.writeheader()
    writer.writerows(rows)

print(f"Created {len(rows)} rows in {OUTPUT_FILE.resolve()}")