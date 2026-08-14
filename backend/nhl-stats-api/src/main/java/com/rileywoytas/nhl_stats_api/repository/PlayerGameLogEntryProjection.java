package com.rileywoytas.nhl_stats_api.repository;

public interface PlayerGameLogEntryProjection {
    String getGameDate();
    String getOpponent();
    Boolean getIsHome();

    Integer getGoals();
    Integer getAssists();
    Integer getPoints();
    Integer getPlusMinus();
    Integer getShots();
    Integer getHits();
    Integer getBlocks();
    Integer getPim();
    Integer getTimeOnIceSeconds();
    Integer getPowerPlayPoints();
    Integer getShorthandedGoals();
    Integer getGameWinningGoals();
    String getGoalHighlightUrl();

    Integer getSaves();
    Integer getShotsAgainst();
    Integer getGoalsAgainst();
    Double getSavePercentage();
    Boolean getStarter();
}
