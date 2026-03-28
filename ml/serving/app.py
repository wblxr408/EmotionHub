"""
Flask 预测服务

POST /predict
  Request:
    {
      "candidates": [
        {
          "post_id": 1,
          "post_emotion_score": 0.8,
          "like_count": 10,
          "comment_count": 3,
          "view_count": 120,
          "recency_hours": 5.0,
          "controversy_score": 0.1,
          "cf_base_score": 0.7,
          "position_original": 2,
          "emotion_state": "LOW",
          "primary_tag": "HEALING"
        },
        ...
      ]
    }

  Response:
    {
      "scores": [0.82, 0.65, ...]   // 与 candidates 顺序一致
    }

GET /health  → {"status": "ok", "model_loaded": true}
"""

import os
import sys
import logging

import numpy as np
import pandas as pd
import lightgbm as lgb
from flask import Flask, request, jsonify

# feature_builder.py 与 app.py 同目录（Docker 中均 COPY 到 /app）
sys.path.insert(0, os.path.dirname(__file__))
from feature_builder import build_features  # noqa: E402

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

MODEL_PATH = os.getenv("MODEL_PATH", "model/ranker.txt")
model: lgb.Booster | None = None


def load_model():
    global model
    if os.path.exists(MODEL_PATH):
        model = lgb.Booster(model_file=MODEL_PATH)
        logger.info(f"Model loaded from {MODEL_PATH}")
    else:
        logger.warning(f"Model file not found at {MODEL_PATH}. "
                       f"Run train.py first, then restart the service.")


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "model_loaded": model is not None})


@app.route("/predict", methods=["POST"])
def predict():
    if model is None:
        return jsonify({"error": "Model not loaded"}), 503

    body = request.get_json(silent=True)
    if not body or "candidates" not in body:
        return jsonify({"error": "Missing 'candidates' field"}), 400

    candidates = body["candidates"]
    if not candidates:
        return jsonify({"scores": []})

    try:
        df = pd.DataFrame(candidates)
        # 确保必要字段存在，缺失时填默认值
        defaults = {
            "post_emotion_score": 0.0,
            "like_count": 0,
            "comment_count": 0,
            "view_count": 0,
            "recency_hours": 72.0,
            "controversy_score": 0.0,
            "cf_base_score": 0.5,
            "position_original": 10,
            "emotion_state": "CALM",
            "primary_tag": "NEUTRAL_CALM",
        }
        for col, default in defaults.items():
            if col not in df.columns:
                df[col] = default

        X = build_features(df)
        scores = model.predict(X).tolist()
        return jsonify({"scores": scores})

    except Exception as e:
        logger.error(f"Prediction error: {e}", exc_info=True)
        return jsonify({"error": str(e)}), 500


@app.route("/reload", methods=["POST"])
def reload_model():
    """重新加载模型文件（重训练后调用）"""
    load_model()
    return jsonify({"status": "ok", "model_loaded": model is not None})


load_model()

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
