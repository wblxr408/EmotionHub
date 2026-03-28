"""
特征工程模块 - 训练与预测共用，保证特征一致性

特征列表（22维）：
  --- 帖子质量 ---
  post_emotion_score   帖子情感分（-1~1）
  like_count_log       log(1 + likeCount)
  comment_count_log    log(1 + commentCount)
  view_count_log       log(1 + viewCount)
  recency_hours        距今小时数
  controversy_score    内容争议分（评论区情感标准差，来自 2.2）
  cf_base_score        协同过滤基础分（来自 2.3）
  position_original    原始曝光位置
  --- 情感匹配 ---
  emotion_match        规则情感匹配分（2.2 互补策略复现）
  state_happy          用户情感状态 one-hot（来自 2.1）
  state_calm
  state_low
  state_anxious
  tag_positive_energy  内容标签 one-hot（来自 2.2）
  tag_healing
  tag_neutral_calm
  tag_controversial
  --- 用户情感深度（来自 2.1 滑动窗口统计）---
  user_avg_score       用户 24h 平均情感分
  user_volatility      情感波动性（stddev / |avg|）
  trend_rising         情感趋势 one-hot（来自 2.1 judgeEmotionTrend）
  trend_falling
  --- 作者影响力（来自 2.3 PageRank 变体）---
  author_influence     作者影响力分（归一化到 0~1）
"""

import numpy as np
import pandas as pd

FEATURE_COLS = [
    # 帖子质量
    "post_emotion_score",
    "like_count_log",
    "comment_count_log",
    "view_count_log",
    "recency_hours",
    "controversy_score",
    "cf_base_score",
    "position_original",
    # 情感匹配
    "emotion_match",
    "state_happy",
    "state_calm",
    "state_low",
    "state_anxious",
    "tag_positive_energy",
    "tag_healing",
    "tag_neutral_calm",
    "tag_controversial",
    # 用户情感深度（2.1）
    "user_avg_score",
    "user_volatility",
    "trend_rising",
    "trend_falling",
    # 作者影响力（2.3）
    "author_influence",
]

EMOTION_STATE_MAP = {
    "HAPPY": "state_happy",
    "CALM": "state_calm",
    "LOW": "state_low",
    "ANXIOUS": "state_anxious",
    "FLUCTUANT": None,
}

PRIMARY_TAG_MAP = {
    "POSITIVE_ENERGY": "tag_positive_energy",
    "HEALING": "tag_healing",
    "NEUTRAL_CALM": "tag_neutral_calm",
    "CONTROVERSIAL": "tag_controversial",
    "WARM": "tag_positive_energy",
}

TREND_MAP = {
    "RISING": "trend_rising",
    "FALLING": "trend_falling",
    "STABLE": None,
}


def compute_emotion_match(emotion_score: float, emotion_state: str) -> float:
    """复用后端规则计算情感匹配分（保持与 Java 端一致）"""
    s = float(emotion_score) if emotion_score is not None else 0.0
    state = (emotion_state or "").upper()

    if state == "LOW":
        return float(np.clip((s + 1) / 2, 0, 1))
    elif state == "ANXIOUS":
        return float(np.clip(1 - abs(s), 0, 1))
    elif state == "HAPPY":
        return 0.5
    elif state == "CALM":
        return float(np.clip(1 - abs(s - 0.2), 0, 1))
    else:
        return float(np.clip(1 - abs(s), 0, 1))


def build_features(df: pd.DataFrame) -> pd.DataFrame:
    """
    输入 DataFrame 需包含列：
      post_emotion_score, like_count, comment_count, view_count,
      recency_hours, controversy_score, cf_base_score,
      position_original, emotion_state, primary_tag,
      user_avg_score, user_volatility, trend_type, author_influence
    输出：仅包含 FEATURE_COLS 的 DataFrame（22维）
    """
    out = pd.DataFrame(index=df.index)

    # 帖子质量
    out["post_emotion_score"] = df["post_emotion_score"].fillna(0.0).astype(float)
    out["like_count_log"] = np.log1p(df["like_count"].fillna(0).astype(float))
    out["comment_count_log"] = np.log1p(df["comment_count"].fillna(0).astype(float))
    out["view_count_log"] = np.log1p(df["view_count"].fillna(0).astype(float))
    out["recency_hours"] = df["recency_hours"].fillna(72.0).astype(float)
    out["controversy_score"] = df["controversy_score"].fillna(0.0).astype(float)
    out["cf_base_score"] = df["cf_base_score"].fillna(0.5).astype(float)
    out["position_original"] = df["position_original"].fillna(10).astype(float)

    # 情感匹配（规则分作为特征）
    out["emotion_match"] = df.apply(
        lambda r: compute_emotion_match(r["post_emotion_score"], r.get("emotion_state", "")),
        axis=1,
    )

    # 用户情感状态 one-hot（来自 2.1）
    for col in ["state_happy", "state_calm", "state_low", "state_anxious"]:
        out[col] = 0.0
    for state, col in EMOTION_STATE_MAP.items():
        if col:
            mask = df["emotion_state"].str.upper() == state
            out.loc[mask, col] = 1.0

    # 内容标签 one-hot（来自 2.2）
    for col in ["tag_positive_energy", "tag_healing", "tag_neutral_calm", "tag_controversial"]:
        out[col] = 0.0
    for tag, col in PRIMARY_TAG_MAP.items():
        mask = df["primary_tag"].str.upper() == tag
        out.loc[mask, col] = 1.0

    # 用户情感深度（来自 2.1 滑动窗口统计）
    out["user_avg_score"] = df.get("user_avg_score", pd.Series(0.0, index=df.index)).fillna(0.0).astype(float)
    out["user_volatility"] = df.get("user_volatility", pd.Series(0.2, index=df.index)).fillna(0.2).astype(float)

    # 情感趋势 one-hot（来自 2.1 judgeEmotionTrend）
    out["trend_rising"] = 0.0
    out["trend_falling"] = 0.0
    if "trend_type" in df.columns:
        for trend, col in TREND_MAP.items():
            if col:
                mask = df["trend_type"].str.upper() == trend
                out.loc[mask, col] = 1.0

    # 作者影响力（来自 2.3 PageRank 变体，已归一化）
    out["author_influence"] = df.get("author_influence", pd.Series(0.5, index=df.index)).fillna(0.5).astype(float)

    return out[FEATURE_COLS]
