FROM python:3.11-slim

WORKDIR /app

COPY . .

RUN pip install flask scikit-learn pandas joblib

EXPOSE 5001

CMD ["python", "app.py"]