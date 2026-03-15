CREATE TABLE user_influence_score (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      user_id VARCHAR(50) NOT NULL,
                                      influence_score DOUBLE DEFAULT 0,
                                      post_influence DOUBLE DEFAULT 0,
                                      comment_influence DOUBLE DEFAULT 0,
                                      positive_impact DOUBLE DEFAULT 0,
                                      negative_impact DOUBLE DEFAULT 0,
                                      controversial_score DOUBLE DEFAULT 0,
                                      update_time DATETIME DEFAULT NOW(),
                                      UNIQUE (user_id)
);