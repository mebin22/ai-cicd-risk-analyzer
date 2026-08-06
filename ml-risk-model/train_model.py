import json
from pathlib import Path

import joblib
import pandas as pd

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score
)
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline


# File paths
BASE_DIR = Path(__file__).resolve().parent
DATASET_PATH = BASE_DIR / "training_dataset.csv"
MODEL_PATH = BASE_DIR / "risk_model.pkl"
METRICS_PATH = BASE_DIR / "metrics.json"


# Load dataset
data = pd.read_csv(DATASET_PATH)

# Check required columns
required_columns = {"logs", "risk"}

if not required_columns.issubset(data.columns):
    raise ValueError(
        "Dataset must contain the columns: logs and risk"
    )


# Clean dataset
data = data.dropna(subset=["logs", "risk"])

data["logs"] = (
    data["logs"]
    .astype(str)
    .str.strip()
)

data["risk"] = (
    data["risk"]
    .astype(str)
    .str.strip()
    .str.upper()
)

# Remove empty logs
data = data[data["logs"] != ""]

# Keep only valid labels
data = data[
    data["risk"].isin(["LOW", "MEDIUM", "HIGH"])
]

# Remove duplicate log entries
data = data.drop_duplicates(subset=["logs"])

print("Dataset size after cleaning:", len(data))
print("\nClass distribution:")
print(data["risk"].value_counts())


# Ensure all classes exist
class_counts = data["risk"].value_counts()

for risk_class in ["LOW", "MEDIUM", "HIGH"]:
    if class_counts.get(risk_class, 0) < 2:
        raise ValueError(
            f"Not enough samples for class {risk_class}. "
            "At least 2 samples are required."
        )


# Input logs and labels
X = data["logs"]
y = data["risk"]


# Split dataset
X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.25,
    random_state=42,
    stratify=y
)

print("\nTraining samples:", len(X_train))
print("Testing samples:", len(X_test))


# Build ML pipeline
model = Pipeline([
    (
        "tfidf",
        TfidfVectorizer(
            lowercase=True,
            ngram_range=(1, 2),
            min_df=1,
            max_df=0.95,
            sublinear_tf=True
        )
    ),
    (
        "classifier",
        LogisticRegression(
            max_iter=3000,
            class_weight="balanced",
            C=2.0,
            random_state=42
        )
    )
])


# Train model
model.fit(X_train, y_train)


# Make predictions
predictions = model.predict(X_test)


# Calculate evaluation metrics
accuracy = accuracy_score(
    y_test,
    predictions
)

precision = precision_score(
    y_test,
    predictions,
    average="weighted",
    zero_division=0
)

recall = recall_score(
    y_test,
    predictions,
    average="weighted",
    zero_division=0
)

f1 = f1_score(
    y_test,
    predictions,
    average="weighted",
    zero_division=0
)


# Print evaluation results
print("\nModel Evaluation")
print("Accuracy:", round(accuracy, 4))
print("Precision:", round(precision, 4))
print("Recall:", round(recall, 4))
print("F1 Score:", round(f1, 4))

print("\nClassification Report:")
print(
    classification_report(
        y_test,
        predictions,
        labels=["LOW", "MEDIUM", "HIGH"],
        zero_division=0
    )
)

print("\nConfusion Matrix:")
print(
    confusion_matrix(
        y_test,
        predictions,
        labels=["LOW", "MEDIUM", "HIGH"]
    )
)


# Save trained model
joblib.dump(model, MODEL_PATH)


# Save metrics for dashboard
metrics = {
    "accuracy": round(float(accuracy), 4),
    "precision": round(float(precision), 4),
    "recall": round(float(recall), 4),
    "f1Score": round(float(f1), 4)
}

with METRICS_PATH.open(
        "w",
        encoding="utf-8"
) as file:
    json.dump(
        metrics,
        file,
        indent=4
    )


print("\nRisk model trained successfully!")
print("Model saved to:", MODEL_PATH)
print("Metrics saved to:", METRICS_PATH)