import joblib

# Load trained model
model = joblib.load("risk_model.pkl")

# Test workflow log
test_log = """
Workflow: Java CI Build
Status: completed
Conclusion: success

Job: build
Job conclusion: success

Steps:
- Build Project: success
- Run Unit Tests: success
- Code coverage check: success
- Complete Build: success

Warnings:
- Code coverage below threshold
- Current coverage: 68%
- Required coverage: 80%
- Manual review required

Workflow result: completed successfully with non-critical warnings
Deployment recommendation: manual review required
"""

# Predict
prediction = model.predict([test_log])[0]
probabilities = model.predict_proba([test_log])[0]

print("Prediction:", prediction)
print("Classes:", model.classes_)
print("Probabilities:", probabilities)
print("Confidence:", round(float(max(probabilities)), 4))