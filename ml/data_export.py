"""
真实数据导出脚本 - 数据积累后替代模拟器使用

从 recommendation_log 关联 post、content_emotion_tag 导出特征，
输出 Parquet 格式供 train.py 使用。

使用时机：recommendation_log 中点击数 >= 500 时切换到此脚本。
"""

import os
import datetime
import pandas as pd
from sqlalchemy import create_engine
from dotenv import load_dotenv

load_dotenv()

DB_URL = (
    f"mysql+pymysql://"
    f"{os.getenv('DB_USER', 'emotionhub')}:"
    f"{os.getenv('DB_PASSWORD', '123456')}@"
    f"{os.getenv('DB_HOST', 'localhost')}:"
    f"{os.getenv('DB_PORT', '3307')}/"
    f"{os.getenv('DB_NAME', 'emotion_hub')}?charset=utf8mb4"
)

OUTPUT_PATH = "data/training_data.parquet"

QUERY = """
SELECT
    rl.id              AS log_id,
    rl.user_id,
    rl.post_id,
    rl.strategy,
    rl.emotion_state,
    rl.score           AS cf_base_score,
    rl.position        AS position_original,
    rl.impressed_at,
    rl.clicked,
    rl.user_avg_score,
    rl.user_volatility,
    rl.trend_type,
    rl.author_influence,

    p.emotion_score    AS post_emotion_score,
    p.like_count,
    p.comment_count,
    p.view_count,
    p.created_at       AS post_created_at,

    COALESCE(cet.primary_tag, 'NEUTRAL_CALM')      AS primary_tag,
    COALESCE(cet.controversy_score, 0.0)            AS controversy_score

FROM recommendation_log rl
JOIN post p ON rl.post_id = p.id
LEFT JOIN content_emotion_tag cet ON rl.post_id = cet.post_id
WHERE rl.impressed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
  AND p.status = 'PUBLISHED'
"""


def main():
    engine = create_engine(DB_URL)
    print("Exporting training data...")
    df = pd.read_sql(QUERY, engine)

    if df.empty:
        print("No data found.")
        return

    # 计算时效特征
    df["recency_hours"] = (
        datetime.datetime.now() - pd.to_datetime(df["post_created_at"])
    ).dt.total_seconds() / 3600
    df["recency_hours"] = df["recency_hours"].clip(lower=0)

    os.makedirs("data", exist_ok=True)
    df.to_parquet(OUTPUT_PATH, index=False)

    total = len(df)
    clicks = df["clicked"].sum()
    print(f"Exported {total} rows, {clicks} clicks (CTR={clicks/total:.3f})")
    print(f"Saved to {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
