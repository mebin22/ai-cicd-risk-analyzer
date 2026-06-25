from flask import Flask, request, jsonify
import joblib

app = Flask(__name__)

model = joblib.load("risk_model.pkl")

@app.route("/predict-risk", methods=["POST"])
def predict_risk():
    data = request.get_json()
    logs = data.get("logs", "")

    prediction = model.predict([logs])[0]
    confidence = max(model.predict_proba([logs])[0])

    if prediction == "LOW":
        score = 20
        decision = "PROCEED"
        recommendation = "Deployment can proceed."
    elif prediction == "MEDIUM":
        score = 55
        decision = "REVIEW"
        recommendation = "Review pipeline logs before deployment."
    else:
        score = 85
        decision = "STOP"
        recommendation = "Fix detected pipeline issues before deployment."

    return jsonify({
        "riskScore": score,
        "riskLevel": prediction,
        "confidence": round(float(confidence), 2),
        "deploymentDecision": decision,
        "recommendation": recommendation
    })

if __name__ == "__main__":
    app.run(port=5001, debug=True)