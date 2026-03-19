package com.rileywoytas.nhl_stats_api.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(
        name = "player_game_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "player_id"})
)
@Data
public class PlayerGameStats {

    @Id
    @GeneratedValue
    private Long id;

    private Long gameId;
    private Long playerId;
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

    // Goalie stats
    private Integer saves;
    private Integer shotsAgainst;
    private Integer goalsAgainst;
    private Double savePercentage;

    // Shared
    private Integer timeOnIceSeconds;
}