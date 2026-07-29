from pathlib import Path
import json

import joblib
from flask import Flask, jsonify, request

app = Flask(__name__)

BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "risk_model.pkl"
METRICS_PATH = BASE_DIR / "metrics.json"

model = joblib.load(MODEL_PATH)


@app.route("/predict-risk", methods=["POST"])
def predict_risk():
    data = request.get_json(silent=True)

    if not data:
        return jsonify({
            "error": "Request body must contain valid JSON."
        }), 400

    logs = str(data.get("logs", "")).strip()

    if not logs:
        return jsonify({
            "error": "The 'logs' field is required and cannot be empty."
        }), 400

    prediction = str(model.predict([logs])[0])

    probabilities = model.predict_proba([logs])[0]
    confidence = float(max(probabilities))

    if prediction == "LOW":
        score = 20
        decision = "PROCEED"
        failure_cause = "No significant pipeline failure detected"
        recommendation = "Deployment can proceed."

    elif prediction == "MEDIUM":
        score = 55
        decision = "REVIEW"
        failure_cause = "Potential pipeline warning or recoverable issue"
        recommendation = "Review pipeline logs before deployment."

    elif prediction == "HIGH":
        score = 85
        decision = "STOP"
        failure_cause = "Critical pipeline failure detected"
        recommendation = "Fix detected pipeline issues before deployment."

    else:
        return jsonify({
            "error": f"Unexpected model prediction: {prediction}"
        }), 500

    return jsonify({
        "riskScore": score,
        "riskLevel": prediction,
        "failureCause": failure_cause,
        "confidence": round(confidence, 4),
        "deploymentDecision": decision,
        "recommendation": recommendation
    })


@app.route("/metrics", methods=["GET"])
def get_metrics():
    if not METRICS_PATH.exists():
        return jsonify({
            "error": "metrics.json was not found."
        }), 404

    with METRICS_PATH.open("r", encoding="utf-8") as file:
        metrics = json.load(file)

    return jsonify(metrics)


@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "UP",
        "modelLoaded": True
    })


if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=5001,
        debug=True
    )