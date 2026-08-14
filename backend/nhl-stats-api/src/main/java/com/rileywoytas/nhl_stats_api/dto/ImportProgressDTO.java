package com.rileywoytas.nhl_stats_api.dto;

import lombok.Data;

@Data
public class ImportProgressDTO {
    private String status; // idle | running | done
    private String season;
    private int totalGames;
    private int processedGames;
    private int failedGames;

    private Long lastGameId;
    private String lastGameDate;
    private String lastEventSummary;

    private long elapsedSeconds;
}
