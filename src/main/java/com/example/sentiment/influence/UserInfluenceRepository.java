package com.example.sentiment.influence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserInfluenceRepository extends JpaRepository<UserInfluenceScore, Long> {
    Optional<UserInfluenceScore> findByUserId(String userId);
    List<UserInfluenceScore> findTop10ByOrderByPositiveImpactDesc();
    List<UserInfluenceScore> findTop10ByOrderByControversialScoreDesc();
}