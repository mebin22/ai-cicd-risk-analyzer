import joblib

model = joblib.load("risk_model.pkl")

test_logs = [
    "Build completed and the application was deployed without errors",
    "JUnit test failed because expected status 200 but received 500",
    "Docker image build stopped due to an unavailable base image",
    "Dependency download timed out but succeeded after retry",
    "Kubernetes pod entered CrashLoopBackOff after deployment",
]

predictions = model.predict(test_logs)
probabilities = model.predict_proba(test_logs)

for log, prediction, probability in zip(
        test_logs,
        predictions,
        probabilities
):
    confidence = probability.max()

    print("\nLog:", log)
    print("Predicted risk:", prediction)
    print("Confidence:", round(float(confidence), 4))