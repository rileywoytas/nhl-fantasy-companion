package com.rileywoytas.nhl_stats_api.service;

import com.rileywoytas.nhl_stats_api.dto.ImportProgressDTO;
import org.springframework.stereotype.Component;

// Single shared progress tracker for the (personal-use, single-operator)
// scoring-details import. Not designed for multiple concurrent imports —
// just gives the frontend something to poll instead of watching the
// terminal during a long-running job.
@Component
public class ImportProgressTracker {

    private volatile String status = "idle";
    private volatile String season;
    private volatile int totalGames;
    private volatile int processedGames;
    private volatile int failedGames;
    private volatile Long lastGameId;
    private volatile String lastGameDate;
    private volatile String lastEventSummary;
    private volatile long startedAtMillis;

    public synchronized void reset(String season, int totalGames) {
        this.status = "running";
        this.season = season;
        this.totalGames = totalGames;
        this.processedGames = 0;
        this.failedGames = 0;
        this.lastGameId = null;
        this.lastGameDate = null;
        this.lastEventSummary = null;
        this.startedAtMillis = System.currentTimeMillis();
    }

    public synchronized void recordGameProcessed(Long gameId, String gameDate, String eventSummary) {
        this.processedGames++;
        this.lastGameId = gameId;
        this.lastGameDate = gameDate;
        this.lastEventSummary = eventSummary;
    }

    public synchronized void recordGameFailed(Long gameId) {
        this.failedGames++;
        this.lastGameId = gameId;
        this.lastEventSummary = "Failed after retries";
    }

    public synchronized void complete() {
        this.status = "done";
    }

    public synchronized ImportProgressDTO snapshot() {
        ImportProgressDTO dto = new ImportProgressDTO();
        dto.setStatus(status);
        dto.setSeason(season);
        dto.setTotalGames(totalGames);
        dto.setProcessedGames(processedGames);
        dto.setFailedGames(failedGames);
        dto.setLastGameId(lastGameId);
        dto.setLastGameDate(lastGameDate);
        dto.setLastEventSummary(lastEventSummary);
        dto.setElapsedSeconds(startedAtMillis == 0 ? 0 : (System.currentTimeMillis() - startedAtMillis) / 1000);
        return dto;
    }
}
