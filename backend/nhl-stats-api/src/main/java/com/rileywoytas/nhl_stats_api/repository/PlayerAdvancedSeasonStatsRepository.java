package com.rileywoytas.nhl_stats_api.repository;

import com.rileywoytas.nhl_stats_api.entity.GameType;
import com.rileywoytas.nhl_stats_api.entity.PlayerAdvancedSeasonStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerAdvancedSeasonStatsRepository extends JpaRepository<PlayerAdvancedSeasonStats, UUID> {
    Optional<PlayerAdvancedSeasonStats> findByPlayerIdAndSeasonAndGameType(Integer playerId, String season, GameType gameType);

    List<PlayerAdvancedSeasonStats> findBySeasonAndGameType(String season, GameType gameType);
}
