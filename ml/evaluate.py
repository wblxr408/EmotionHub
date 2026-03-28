"""
离线评估脚本 - 计算 NDCG@5 和 NDCG@10

使用方式：
  python evaluate.py --source real     # 评估真实数据
  python evaluate.py --source simulator
"""

import os
import argparse
import numpy as np
import pandas as pd
import lightgbm as lgb
from sklearn.metrics import ndcg_score

from feature_builder import build_features, FEATURE_COLS

MODEL_PATH = "model/ranker.txt"


def ndcg_at_k(df: pd.DataFrame, model: lgb.Booster, k: int) -> float:
    scores = []
    for session_id, group in df.groupby("session_id"):
        if group["clicked"].sum() == 0:
            continue  # 全未点击的组跳过
        X = build_features(group)
        preds = model.predict(X)
        true_labels = group["clicked"].values.reshape(1, -1)
        pred_scores = preds.reshape(1, -1)
        scores.append(ndcg_score(true_labels, pred_scores, k=k))
    return float(np.mean(scores)) if scores else 0.0


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", choices=["simulator", "real"], default="real")
    args = parser.parse_args()

    if not os.path.exists(MODEL_PATH):
        print(f"Model not found at {MODEL_PATH}. Run train.py first.")
        return

    model = lgb.Booster(model_file=MODEL_PATH)

    if args.source == "real":
        path = "data/training_data.parquet"
        if not os.path.exists(path):
            print("No real data found. Run data_export.py first.")
            return
        df = pd.read_parquet(path)
        df["session_id"] = (
            df["user_id"].astype(str)
            + "_"
            + pd.to_datetime(df["impressed_at"]).dt.strftime("%Y%m%d%H")
        )
    else:
        from train import load_from_simulator
        df = load_from_simulator()

    # 取最后 20% 的 session 作为测试集
    unique_sessions = df["session_id"].unique()
    test_sessions = set(unique_sessions[int(len(unique_sessions) * 0.8):])
    test_df = df[df["session_id"].isin(test_sessions)]

    ndcg5 = ndcg_at_k(test_df, model, k=5)
    ndcg10 = ndcg_at_k(test_df, model, k=10)

    print(f"Evaluation on {len(test_sessions)} sessions ({len(test_df)} samples):")
    print(f"  NDCG@5  = {ndcg5:.4f}")
    print(f"  NDCG@10 = {ndcg10:.4f}")


if __name__ == "__main__":
    main()
