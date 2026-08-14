package com.rileywoytas.nhl_stats_api.repository;

import com.rileywoytas.nhl_stats_api.entity.PlayerGameStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerGameStatsRepository extends JpaRepository<PlayerGameStats, UUID> {
    Optional<PlayerGameStats> findByPlayerIdAndGameId(Integer playerId, Long teamId);

    List<PlayerGameStats> findByGameId(Long gameId);

    @Query(value = """
            SELECT
                pgs.player_id AS playerId,
                g.season AS season,
                COUNT(*) AS gamesPlayed,
                SUM(pgs.goals) AS goals,
                SUM(pgs.assists) AS assists,
                SUM(pgs.goals + pgs.assists) AS points,
                SUM(pgs.shots) AS shots,
                SUM(pgs.hits) AS hits,
                SUM(pgs.blocks) AS blocks,
                SUM(pgs.pim) AS pim,
                SUM(pgs.plus_minus) AS plusMinus,
                SUM(pgs.giveaways) AS giveaways,
                SUM(pgs.takeaways) AS takeaways,
                SUM(pgs.time_on_ice_seconds) AS timeOnIceSeconds,
                SUM(CASE WHEN pgs.starter = true THEN 1 ELSE 0 END) AS starts,
                SUM(pgs.saves) AS saves,
                SUM(pgs.shots_against) AS shotsAgainst,
                SUM(pgs.goals_against) AS goalsAgainst,
                CASE WHEN SUM(pgs.shots_against) > 0
                     THEN ROUND(SUM(pgs.saves)::numeric / SUM(pgs.shots_against), 3)
                     ELSE NULL END AS savePercentage
            FROM player_game_stats pgs
            JOIN games g ON g.nhl_id = pgs.game_id
            WHERE g.season = :season AND g.game_type = :gameType
            GROUP BY pgs.player_id, g.season
            """, nativeQuery = true)
    List<PlayerSeasonTotalsProjection> findSeasonTotals(@Param("season") String season, @Param("gameType") String gameType);

    @Query(value = """
            SELECT
                pgs.player_id AS playerId,
                g.season AS season,
                COUNT(*) AS gamesPlayed,
                SUM(pgs.goals) AS goals,
                SUM(pgs.assists) AS assists,
                SUM(pgs.goals + pgs.assists) AS points,
                SUM(pgs.shots) AS shots,
                SUM(pgs.hits) AS hits,
                SUM(pgs.blocks) AS blocks,
                SUM(pgs.pim) AS pim,
                SUM(pgs.plus_minus) AS plusMinus,
                SUM(pgs.giveaways) AS giveaways,
                SUM(pgs.takeaways) AS takeaways,
                SUM(pgs.time_on_ice_seconds) AS timeOnIceSeconds,
                SUM(CASE WHEN pgs.starter = true THEN 1 ELSE 0 END) AS starts,
                SUM(pgs.saves) AS saves,
                SUM(pgs.shots_against) AS shotsAgainst,
                SUM(pgs.goals_against) AS goalsAgainst,
                CASE WHEN SUM(pgs.shots_against) > 0
                     THEN ROUND(SUM(pgs.saves)::numeric / SUM(pgs.shots_against), 3)
                     ELSE NULL END AS savePercentage
            FROM player_game_stats pgs
            JOIN games g ON g.nhl_id = pgs.game_id
            WHERE g.season = :season AND g.game_type = :gameType AND pgs.player_id = :playerId
            GROUP BY pgs.player_id, g.season
            """, nativeQuery = true)
    Optional<PlayerSeasonTotalsProjection> findSeasonTotalsForPlayer(
            @Param("season") String season, @Param("gameType") String gameType, @Param("playerId") Integer playerId);

    // Per-game stat log for a player, most recent first. Opponent/home-away
    // are derived by comparing the player's team for that game (team_id,
    // an NHL team id) against the game's home team NHL id.
    @Query(value = """
            SELECT
                g.game_date::text AS gameDate,
                CASE WHEN pgs.team_id = ht.nhl_id THEN at.tri_code ELSE ht.tri_code END AS opponent,
                CASE WHEN pgs.team_id = ht.nhl_id THEN true ELSE false END AS isHome,
                pgs.goals AS goals,
                pgs.assists AS assists,
                (pgs.goals + pgs.assists) AS points,
                pgs.plus_minus AS plusMinus,
                pgs.shots AS shots,
                pgs.hits AS hits,
                pgs.blocks AS blocks,
                pgs.pim AS pim,
                pgs.time_on_ice_seconds AS timeOnIceSeconds,
                pgs.power_play_points AS powerPlayPoints,
                pgs.shorthanded_goals AS shorthandedGoals,
                pgs.game_winning_goals AS gameWinningGoals,
                pgs.goal_highlight_url AS goalHighlightUrl,
                pgs.saves AS saves,
                pgs.shots_against AS shotsAgainst,
                pgs.goals_against AS goalsAgainst,
                pgs.save_percentage AS savePercentage,
                pgs.starter AS starter
            FROM player_game_stats pgs
            JOIN games g ON g.nhl_id = pgs.game_id
            JOIN teams ht ON ht.id = g.home_team_id
            JOIN teams at ON at.id = g.away_team_id
            WHERE pgs.player_id = :playerId AND g.season = :season AND g.game_type = :gameType
            ORDER BY g.game_date DESC
            """, nativeQuery = true)
    List<PlayerGameLogEntryProjection> findGameLog(
            @Param("playerId") Integer playerId, @Param("season") String season, @Param("gameType") String gameType);
}
