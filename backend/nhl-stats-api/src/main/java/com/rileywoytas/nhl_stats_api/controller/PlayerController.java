package com.rileywoytas.nhl_stats_api.controller;

import com.rileywoytas.nhl_stats_api.entity.Player;
import com.rileywoytas.nhl_stats_api.service.NHLImportService;
import com.rileywoytas.nhl_stats_api.service.PlayerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;
    private final NHLImportService importService;

    public PlayerController(PlayerService playerService, NHLImportService importService) {
        this.playerService = playerService;
        this.importService = importService;
    }

    @GetMapping
    public List<Player> getPlayers() {
        return playerService.getAllPlayers();
    }

    @PostMapping("/import/skaters")
    public int importPlayers() throws Exception {
        return importService.importSkaters();
    }

    @PostMapping("/import/goalies")
    public int importGoalies() throws Exception {
        return importService.importGoalies();
    }
}
