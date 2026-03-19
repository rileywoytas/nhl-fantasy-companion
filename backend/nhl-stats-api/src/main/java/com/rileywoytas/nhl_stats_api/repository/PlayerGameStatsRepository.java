package com.rileywoytas.nhl_stats_api.repository;

import com.rileywoytas.nhl_stats_api.entity.PlayerGameStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerGameStatsRepository extends JpaRepository<PlayerGameStats, UUID> {
    Optional<PlayerGameStats> findByPlayerIdAndGameId(Integer playerId, Long teamId);
}
