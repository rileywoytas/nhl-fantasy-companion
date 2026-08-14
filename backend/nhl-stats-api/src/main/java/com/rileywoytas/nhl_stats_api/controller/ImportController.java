package com.rileywoytas.nhl_stats_api.controller;

import com.rileywoytas.nhl_stats_api.dto.ImportProgressDTO;
import com.rileywoytas.nhl_stats_api.service.ImportProgressTracker;
import com.rileywoytas.nhl_stats_api.service.NHLImportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/import")
public class ImportController {

    private final NHLImportService importService;
    private final ImportProgressTracker progressTracker;

    public ImportController(NHLImportService importService, ImportProgressTracker progressTracker) {
        this.importService = importService;
        this.progressTracker = progressTracker;
    }

    @PostMapping("/boxscore/season/{season}")
    public String importSeasonBoxScores(@PathVariable String season) throws Exception{
        return importService.importSeasonBoxScores(season);
    }

    @PostMapping("/boxscore/game/{game}")
    public int importGameBoxScore(@PathVariable String game) throws Exception{
        return importService.importGameBoxScores(game);
    }

    @PostMapping("/scoring-details/season/{season}")
    public String importGameScoringDetails(@PathVariable String season) throws Exception {
        return importService.importGameScoringDetails(season);
    }

    @GetMapping("/scoring-details/progress")
    public ImportProgressDTO getScoringDetailsProgress() {
        return progressTracker.snapshot();
    }
}
