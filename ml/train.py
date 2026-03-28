"""
LightGBM LambdaMART 训练脚本

支持两种数据来源：
  --source simulator  从数据库读取并通过模拟器生成（冷启动）
  --source real       从 data/training_data.parquet 读取真实导出数据（默认）

训练完成后将模型写入 model/ranker.txt。
"""

import os
import argparse
import datetime
import math
import random
import numpy as np
import pandas as pd
import lightgbm as lgb
from sqlalchemy import create_engine
from dotenv import load_dotenv
from sklearn.model_selection import GroupShuffleSplit

from feature_builder import build_features, FEATURE_COLS
from data_simulator import simulate_click, EMOTION_STATES, SESSIONS_PER_USER, FEED_SIZE

load_dotenv()

DB_URL = (
    f"mysql+pymysql://"
    f"{os.getenv('DB_USER', 'emotionhub')}:"
    f"{os.getenv('DB_PASSWORD', '123456')}@"
    f"{os.getenv('DB_HOST', 'localhost')}:"
    f"{os.getenv('DB_PORT', '3307')}/"
    f"{os.getenv('DB_NAME', 'emotion_hub')}?charset=utf8mb4"
)

MODEL_DIR = "model"
MODEL_PATH = os.path.join(MODEL_DIR, "ranker.txt")

LGBM_PARAMS = {
    "objective": "lambdarank",
    "metric": "ndcg",
    "ndcg_eval_at": [5, 10],
    "learning_rate": 0.05,
    "num_leaves": 31,
    "min_data_in_leaf": 10,
    "feature_fraction": 0.8,
    "bagging_fraction": 0.8,
    "bagging_freq": 5,
    "verbose": -1,
    "n_jobs": -1,
}
NUM_ROUNDS = 300
EARLY_STOPPING = 30


def load_from_simulator() -> pd.DataFrame:
    """冷启动：从数据库生成合成数据"""
    random.seed(42)
    np.random.seed(42)
    engine = create_engine(DB_URL)
    with engine.connect() as conn:
        posts = pd.read_sql(
            "SELECT id, user_id, emotion_score, like_count, comment_count, "
            "view_count, created_at FROM post WHERE status = 'PUBLISHED'",
            conn,
        )
        tags = pd.read_sql(
            "SELECT post_id, primary_tag, controversy_score FROM content_emotion_tags",
            conn,
        )
        users = pd.read_sql("SELECT id FROM user WHERE status = 'ACTIVE'", conn)

    posts = posts.merge(tags, left_on="id", right_on="post_id", how="left")
    posts["primary_tag"] = posts["primary_tag"].fillna("NEUTRAL_CALM")
    posts["controversy_score"] = posts["controversy_score"].fillna(0.0)
    posts["emotion_score"] = posts["emotion_score"].fillna(0.0)

    user_ids = users["id"].tolist()
    post_rows = posts.to_dict("records")
    now = datetime.datetime.now()
    records = []

    for uid in user_ids:
        for _ in range(SESSIONS_PER_USER):
            emotion_state = random.choice(EMOTION_STATES)
            candidates = [p for p in post_rows if p["user_id"] != uid]
            sample = random.sample(candidates, min(FEED_SIZE, len(candidates)))
            session_id = f"{uid}_{random.randint(0, 1_000_000)}"
            for pos, post in enumerate(sample, start=1):
                clicked = simulate_click(
                    emotion_score=float(post["emotion_score"]),
                    emotion_state=emotion_state,
                    like_count=int(post["like_count"]),
                    comment_count=int(post["comment_count"]),
                    view_count=int(post["view_count"]),
                    created_at=post["created_at"],
                    position=pos,
                )
                hours = max(
                    1,
                    (now - post["created_at"]).total_seconds() / 3600,
                )
                records.append(
                    {
                        "session_id": session_id,
                        "post_emotion_score": float(post["emotion_score"]),
                        "like_count": int(post["like_count"]),
                        "comment_count": int(post["comment_count"]),
                        "view_count": int(post["view_count"]),
                        "recency_hours": hours,
                        "controversy_score": float(post["controversy_score"]),
                        "cf_base_score": 0.5,
                        "position_original": pos,
                        "emotion_state": emotion_state,
                        "primary_tag": post["primary_tag"],
                        "clicked": 1 if clicked else 0,
                    }
                )

    return pd.DataFrame(records)


def load_from_parquet() -> pd.DataFrame:
    path = "data/training_data.parquet"
    if not os.path.exists(path):
        raise FileNotFoundError(f"{path} not found. Run data_export.py first.")
    df = pd.read_parquet(path)
    # 构建 session_id（按用户+天分组）
    df["session_id"] = (
        df["user_id"].astype(str)
        + "_"
        + pd.to_datetime(df["impressed_at"]).dt.strftime("%Y%m%d%H")
    )
    return df


def train(df: pd.DataFrame):
    print(f"Total samples: {len(df)}, CTR: {df['clicked'].mean():.3f}")

    X = build_features(df)
    y = df["clicked"].astype(int)
    groups = df["session_id"]

    # 按 session 分训练/验证集
    unique_groups = groups.unique()
    np.random.shuffle(unique_groups)
    split = int(len(unique_groups) * 0.8)
    train_groups = set(unique_groups[:split])
    val_groups = set(unique_groups[split:])

    train_mask = groups.isin(train_groups)
    val_mask = groups.isin(val_groups)

    X_train, y_train = X[train_mask], y[train_mask]
    X_val, y_val = X[val_mask], y[val_mask]

    # LightGBM 需要每个 query 的样本数（按 session 排序后统计）
    train_df = df[train_mask].copy()
    train_df["__X__"] = range(len(train_df))
    train_group_sizes = train_df.groupby("session_id", sort=False).size().tolist()

    val_df = df[val_mask].copy()
    val_group_sizes = val_df.groupby("session_id", sort=False).size().tolist()

    dtrain = lgb.Dataset(X_train, label=y_train, group=train_group_sizes,
                         feature_name=FEATURE_COLS)
    dval = lgb.Dataset(X_val, label=y_val, group=val_group_sizes,
                       feature_name=FEATURE_COLS, reference=dtrain)

    print("Training LightGBM LambdaMART...")
    model = lgb.train(
        LGBM_PARAMS,
        dtrain,
        num_boost_round=NUM_ROUNDS,
        valid_sets=[dval],
        callbacks=[
            lgb.early_stopping(EARLY_STOPPING, verbose=True),
            lgb.log_evaluation(50),
        ],
    )

    os.makedirs(MODEL_DIR, exist_ok=True)
    model.save_model(MODEL_PATH)
    print(f"\nModel saved to {MODEL_PATH}")
    print(f"Best iteration: {model.best_iteration}")

    # 特征重要性
    importance = pd.Series(
        model.feature_importance(importance_type="gain"),
        index=FEATURE_COLS,
    ).sort_values(ascending=False)
    print("\nFeature importance (gain):")
    print(importance.to_string())


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--source",
        choices=["simulator", "real"],
        default="simulator",
        help="Data source: 'simulator' for cold start, 'real' for production data",
    )
    args = parser.parse_args()

    if args.source == "simulator":
        print("Loading simulated data (cold start mode)...")
        df = load_from_simulator()
    else:
        print("Loading real exported data...")
        df = load_from_parquet()

    train(df)
