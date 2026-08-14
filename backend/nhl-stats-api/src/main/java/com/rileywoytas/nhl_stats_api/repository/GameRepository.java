package com.rileywoytas.nhl_stats_api.repository;

import com.rileywoytas.nhl_stats_api.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    Optional<Game> findByNhlId(Long nhlId);

    @Query("SELECT g FROM Game g WHERE g.nhlId IN :nhlIds")
    List<Game> findByNhlIdIn(@Param("nhlIds") Set<Long> nhlIds);

    List<Game> findAllBySeason(String season);

    @Query("SELECT DISTINCT g.season FROM Game g ORDER BY g.season DESC")
    List<String> findDistinctSeasons();

    @Query("SELECT g.nhlId FROM Game g WHERE g.season = :season")
    List<Long> getAllNhlIdsBySeason(@Param("season") String season);

    @Query("SELECT g.nhlId FROM Game g WHERE g.season = :season AND g.gameState <> 'FUT'")
    List<Long> getNonFutureNhlIdsBySeason(@Param("season") String season);

    // Games in the season that still have at least one PlayerGameStats row
    // missing per-game scoring detail (power_play_goals is the sentinel —
    // applyGameScoringDetails always sets it for every player in a game
    // together, so a null there means the whole game hasn't been processed
    // yet). Used to make importGameScoringDetails resumable: re-running it
    // only touches what's left, instead of redoing the whole season.
    @Query(value = """
            SELECT DISTINCT g.nhl_id
            FROM games g
            JOIN player_game_stats pgs ON pgs.game_id = g.nhl_id
            WHERE g.season = :season AND g.game_state <> 'FUT' AND pgs.power_play_goals IS NULL
            """, nativeQuery = true)
    List<Long> getNhlIdsMissingScoringDetailsBySeason(@Param("season") String season);
}