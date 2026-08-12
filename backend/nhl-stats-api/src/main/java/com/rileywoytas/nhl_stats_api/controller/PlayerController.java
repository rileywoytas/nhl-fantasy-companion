package com.rileywoytas.nhl_stats_api.controller;

import com.rileywoytas.nhl_stats_api.dto.PlayerSeasonStatsDTO;
import com.rileywoytas.nhl_stats_api.entity.GameType;
import com.rileywoytas.nhl_stats_api.entity.Player;
import com.rileywoytas.nhl_stats_api.service.NHLImportService;
import com.rileywoytas.nhl_stats_api.service.PlayerService;
import com.rileywoytas.nhl_stats_api.service.PlayerStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerStatsService playerStatsService;
    private final NHLImportService importService;

    public PlayerController(PlayerService playerService, PlayerStatsService playerStatsService, NHLImportService importService) {
        this.playerService = playerService;
        this.playerStatsService = playerStatsService;
        this.importService = importService;
    }

    @GetMapping
    public List<Player> getPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/stats/{season}")
    public List<PlayerSeasonStatsDTO> getSeasonStats(
            @PathVariable String season,
            @RequestParam(defaultValue = "REGULAR_SEASON") String gameType) {
        return playerStatsService.getSeasonTotals(season, gameType);
    }

    @GetMapping("/{nhlId}/stats/{season}")
    public PlayerSeasonStatsDTO getPlayerSeasonStats(
            @PathVariable Integer nhlId,
            @PathVariable String season,
            @RequestParam(defaultValue = "REGULAR_SEASON") String gameType) {
        return playerStatsService.getSeasonTotalsForPlayer(season, gameType, nhlId)
                .orElseThrow(() -> new RuntimeException(
                        "No stats found for player " + nhlId + " in " + season + " (" + gameType + ")"));
    }

    @PostMapping("/import/skaters")
    public int importPlayers() throws Exception {
        return importService.importSkaters();
    }

    @PostMapping("/import/goalies")
    public int importGoalies() throws Exception {
        return importService.importGoalies();
    }

    @PostMapping("/backfill")
    public String backfillMissingPlayers() {
        return importService.backfillMissingPlayers();
    }

    @PostMapping("/import/advanced-stats")
    public String importAdvancedStats(
            @RequestParam String season,
            @RequestParam(defaultValue = "REGULAR_SEASON") String gameType) throws Exception {
        GameType type = GameType.valueOf(gameType.toUpperCase());
        String skaterResult = importService.importAdvancedSkaterStats(season, type);
        String goalieResult = importService.importAdvancedGoalieStats(season, type);
        return skaterResult + "\n" + goalieResult;
    }
}
