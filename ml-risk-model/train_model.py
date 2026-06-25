import pandas as pd
import joblib

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression

data = pd.read_csv("training_data.csv")

model = Pipeline([
    ("tfidf", TfidfVectorizer()),
    ("classifier", LogisticRegression(max_iter=1000))
])

model.fit(data["logs"], data["risk"])

joblib.dump(model, "risk_model.pkl")

print("Risk model trained successfully!")