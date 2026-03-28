"""
冷启动数据模拟器

从数据库读取现有 post + content_emotion_tag + user 数据，
模拟用户浏览行为，生成合成 recommendation_log 用于初始模型训练。

点击概率模型：
  p(click) = sigmoid(
      1.5 × emotion_match
    + 0.8 × normalized_hot_score
    + 0.5 × recency_score
    - 0.3 × position_penalty
    + noise
  )
"""

import os
import math
import random
import datetime
import numpy as np
import pandas as pd
from sqlalchemy import create_engine, text
from dotenv import load_dotenv
from feature_builder import compute_emotion_match

load_dotenv()

DB_URL = (
    f"mysql+pymysql://"
    f"{os.getenv('DB_USER', 'emotionhub')}:"
    f"{os.getenv('DB_PASSWORD', '123456')}@"
    f"{os.getenv('DB_HOST', 'localhost')}:"
    f"{os.getenv('DB_PORT', '3307')}/"
    f"{os.getenv('DB_NAME', 'emotion_hub')}?charset=utf8mb4"
)

# 每个用户模拟的 Feed 请求次数
SESSIONS_PER_USER = 10
# 每次 Feed 曝光帖子数
FEED_SIZE = 20
# 随机种子保证可复现
RANDOM_SEED = 42

EMOTION_STATES = ["HAPPY", "CALM", "LOW", "ANXIOUS", "FLUCTUANT"]


def sigmoid(x: float) -> float:
    return 1.0 / (1.0 + math.exp(-x))


def simulate_click(
    emotion_score: float,
    emotion_state: str,
    like_count: int,
    comment_count: int,
    view_count: int,
    created_at: datetime.datetime,
    position: int,
) -> bool:
    """基于启发式规则模拟点击行为"""
    em = compute_emotion_match(emotion_score, emotion_state)
    hot = math.log1p(like_count * 3 + comment_count * 5 + view_count)
    hot_norm = min(hot / 10.0, 1.0)
    hours = max(1, (datetime.datetime.now() - created_at).total_seconds() / 3600)
    recency = math.exp(-hours / 72.0)
    pos_penalty = math.log1p(position) * 0.15
    noise = random.gauss(0, 0.3)

    logit = 1.5 * em + 0.8 * hot_norm + 0.5 * recency - pos_penalty + noise
    return random.random() < sigmoid(logit)


def main():
    random.seed(RANDOM_SEED)
    np.random.seed(RANDOM_SEED)

    engine = create_engine(DB_URL)
    with engine.connect() as conn:
        posts = pd.read_sql(
            "SELECT id, user_id, emotion_score, emotion_label, "
            "like_count, comment_count, view_count, created_at "
            "FROM post WHERE status = 'PUBLISHED'",
            conn,
        )
        tags = pd.read_sql(
            "SELECT post_id, primary_tag, sentiment_score, controversy_score "
            "FROM content_emotion_tag",
            conn,
        )
        users = pd.read_sql("SELECT id FROM user WHERE status = 'ACTIVE'", conn)
        # 作者影响力（可能为空，用默认值兜底）
        influence_raw = pd.read_sql(
            "SELECT user_id, influence_score FROM user_influence_score "
            "WHERE calculation_date = (SELECT MAX(calculation_date) FROM user_influence_score)",
            conn,
        )

    if posts.empty or users.empty:
        print("No data found. Please insert some posts and users first.")
        return

    posts = posts.merge(tags, left_on="id", right_on="post_id", how="left")
    posts["primary_tag"] = posts["primary_tag"].fillna("NEUTRAL_CALM")
    posts["controversy_score"] = posts["controversy_score"].fillna(0.0).astype(float)
    posts["emotion_score"] = posts["emotion_score"].fillna(0.0).astype(float)

    # 归一化作者影响力到 0~1
    influence_map: dict = {}
    if not influence_raw.empty:
        max_score = float(influence_raw["influence_score"].max()) or 1.0
        for _, row in influence_raw.iterrows():
            influence_map[int(row["user_id"])] = float(row["influence_score"]) / max_score

    user_ids = users["id"].tolist()
    post_rows = posts.to_dict("records")

    records = []
    now = datetime.datetime.now()

    print(f"Simulating {len(user_ids)} users × {SESSIONS_PER_USER} sessions...")

    for uid in user_ids:
        # 模拟该用户的情感历史统计（2.1 产物）
        user_avg_score = random.uniform(-0.5, 0.5)
        user_volatility = random.uniform(0.05, 0.45)
        trend_type = random.choice(["RISING", "FALLING", "STABLE"])

        for session_idx in range(SESSIONS_PER_USER):
            emotion_state = random.choice(EMOTION_STATES)
            strategy = random.choice(["emotional_adaptive", "traditional"])
            # 随机抽取 FEED_SIZE 个帖子（排除自己）
            candidates = [p for p in post_rows if p["user_id"] != uid]
            if len(candidates) < FEED_SIZE:
                sample = candidates
            else:
                sample = random.sample(candidates, FEED_SIZE)

            impressed_at = now - datetime.timedelta(
                hours=random.randint(0, 24 * 7)
            )

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
                records.append(
                    {
                        "user_id": uid,
                        "post_id": post["id"],
                        "strategy": strategy,
                        "emotion_state": emotion_state,
                        "score": 0.5,
                        "position": pos,
                        "impressed_at": impressed_at,
                        "clicked": 1 if clicked else 0,
                        "clicked_at": impressed_at + datetime.timedelta(seconds=random.randint(5, 60))
                        if clicked
                        else None,
                        # 新增：2.1 用户情感深度特征
                        "user_avg_score": user_avg_score,
                        "user_volatility": user_volatility,
                        "trend_type": trend_type,
                        # 新增：2.3 作者影响力特征
                        "author_influence": influence_map.get(int(post["user_id"]), 0.5),
                    }
                )

    df = pd.DataFrame(records)
    print(f"Generated {len(df)} impressions, {df['clicked'].sum()} clicks "
          f"(CTR={df['clicked'].mean():.3f})")

    # 写入数据库
    with engine.connect() as conn:
        conn.execute(text("DELETE FROM recommendation_log WHERE score = 0.5"))
        conn.commit()

    df.to_sql(
        "recommendation_log",
        engine,
        if_exists="append",
        index=False,
        chunksize=500,
    )
    print(f"Inserted {len(df)} rows into recommendation_log.")


if __name__ == "__main__":
    main()
