package com.rileywoytas.nhl_stats_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

// Fantasy-relevant stats not available from the gamecenter box score endpoint
// (which only has goals/assists/points/shots/hits/blocks/TOI). Sourced from
// the separate Stats REST API's skater/summary and goalie/summary reports,
// which aggregate these correctly per season + game type.
@Entity
@Table(
        name = "player_advanced_season_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "season", "game_type"})
)
@Data
public class PlayerAdvancedSeasonStats {

    @Id
    @GeneratedValue
    private UUID id;

    private Integer playerId;
    private String season;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type")
    private GameType gameType;

    // Skater
    private Integer powerPlayGoals;
    private Integer powerPlayAssists;
    private Integer shorthandedGoals;
    private Integer gameWinningGoals;

    // Goalie
    private Integer wins;
    private Integer losses;
    private Integer otLosses;
    private Integer shutouts;
}
