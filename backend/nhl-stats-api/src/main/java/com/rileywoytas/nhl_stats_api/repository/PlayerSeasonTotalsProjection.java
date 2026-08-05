package com.rileywoytas.nhl_stats_api.repository;

public interface PlayerSeasonTotalsProjection {
    Integer getPlayerId();
    String getSeason();
    Long getGamesPlayed();

    // Skater totals
    Long getGoals();
    Long getAssists();
    Long getPoints();
    Long getShots();
    Long getHits();
    Long getBlocks();
    Long getPim();
    Long getPlusMinus();
    Long getGiveaways();
    Long getTakeaways();
    Long getTimeOnIceSeconds();

    // Goalie totals
    Long getStarts();
    Long getSaves();
    Long getShotsAgainst();
    Long getGoalsAgainst();
    Double getSavePercentage();
}