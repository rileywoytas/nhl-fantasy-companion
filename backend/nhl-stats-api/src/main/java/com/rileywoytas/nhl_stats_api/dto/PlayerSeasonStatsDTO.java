package com.rileywoytas.nhl_stats_api.dto;

import lombok.Data;

@Data
public class PlayerSeasonStatsDTO {

    // Player identity
    private Integer playerId;
    private String firstName;
    private String lastName;
    private String position;
    private String teamTriCode;
    private String headshot;
    private String teamLogo;

    private String season;
    private String gameType; // REGULAR_SEASON or PLAYOFFS
    private Long gamesPlayed;

    // Skater totals
    private Long goals;
    private Long assists;
    private Long points;
    private Long shots;
    private Long hits;
    private Long blocks;
    private Long pim;
    private Long plusMinus;
    private Long giveaways;
    private Long takeaways;
    private Long timeOnIceSeconds;

    // Goalie totals
    private Long starts;
    private Long saves;
    private Long shotsAgainst;
    private Long goalsAgainst;
    private Double savePercentage;

    // Advanced (from Stats REST API) — skater
    private Integer powerPlayGoals;
    private Integer powerPlayAssists;
    private Integer shorthandedGoals;
    private Integer gameWinningGoals;

    // Advanced (from Stats REST API) — goalie
    private Integer wins;
    private Integer losses;
    private Integer otLosses;
    private Integer shutouts;
}
