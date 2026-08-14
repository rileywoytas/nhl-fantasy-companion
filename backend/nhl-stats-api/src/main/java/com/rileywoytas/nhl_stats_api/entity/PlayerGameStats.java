package com.rileywoytas.nhl_stats_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;


@Entity
@Table(
        name = "player_game_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "player_id"})
)
@Data
public class PlayerGameStats {

    @Id
    @GeneratedValue
    private UUID id;

    private Long gameId;
    private Integer playerId;

    private Long teamId;

    private String position;

    // Skater stats
    private Integer goals;
    private Integer assists;
    private Integer shots;
    private Integer hits;
    private Integer blocks;
    private Integer pim;
    private Integer plusMinus;
    private Integer shifts;
    private Integer giveaways;
    private Integer takeaways;

    // Per-game scoring detail — derived from the gamecenter "landing" endpoint's
    // goal-by-goal scoring summary, populated separately from the box score
    // import via importGameScoringDetails(). Null until that's run for a game.
    private Integer powerPlayPoints;
    private Integer shorthandedGoals;
    private Integer gameWinningGoals; // 0 or 1 — whether this player scored the GWG in this game
    private String goalHighlightUrl; // link to a highlight clip for a goal this player scored this game, if any

    // Goalie stats
    private Integer saves;
    private Integer shotsAgainst;
    private Integer evenStrengthGoalsAgainst;
    private Integer powerPlayGoalsAgainst;
    private Integer shorthandedGoalsAgainst;
    private Integer goalsAgainst;
    private Double savePercentage;
    private Boolean starter;

    // Shared
    private Integer timeOnIceSeconds;
}