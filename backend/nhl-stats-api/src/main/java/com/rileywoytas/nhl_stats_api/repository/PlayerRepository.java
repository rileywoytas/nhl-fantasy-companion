package com.rileywoytas.nhl_stats_api.repository;

import com.rileywoytas.nhl_stats_api.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {
    Optional<Player> findByNhlId(Integer nhlId);

    @Query("SELECT p FROM Player p WHERE p.nhlId IN :nhlIds")
    List<Player> findByNhlIdIn(@Param("nhlIds") Set<Integer> nhlIds);

    // Player IDs that appear in player_game_stats but have no matching row in
    // players — happens for anyone who's left the league since box scores
    // started being imported (retired, sent down, etc). Used to backfill.
    @Query(value = """
            SELECT DISTINCT pgs.player_id
            FROM player_game_stats pgs
            LEFT JOIN players p ON p.nhl_id = pgs.player_id
            WHERE p.id IS NULL
            """, nativeQuery = true)
    List<Integer> findPlayerIdsMissingFromPlayers();
}
