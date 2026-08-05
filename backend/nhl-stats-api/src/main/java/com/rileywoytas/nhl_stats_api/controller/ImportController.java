package com.rileywoytas.nhl_stats_api.controller;

import com.rileywoytas.nhl_stats_api.service.NHLImportService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/import")
public class ImportController {

    private final NHLImportService importService;

    public ImportController(NHLImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/boxscore/season/{season}")
    public String importSeasonBoxScores(@PathVariable String season) throws Exception{
        return importService.importSeasonBoxScores(season);
    }

    @PostMapping("/boxscore/game/{game}")
    public int importGameBoxScore(@PathVariable String game) throws Exception{
        return importService.importGameBoxScores(game);
    }
}
