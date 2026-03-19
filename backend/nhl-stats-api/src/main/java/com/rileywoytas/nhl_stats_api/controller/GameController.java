package com.rileywoytas.nhl_stats_api.controller;

import com.rileywoytas.nhl_stats_api.entity.Game;
import com.rileywoytas.nhl_stats_api.service.GameService;
import com.rileywoytas.nhl_stats_api.service.NHLImportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;
    private final NHLImportService importService;

    public GameController(GameService gameService, NHLImportService importService) {
        this.gameService = gameService;
        this.importService = importService;
    }

    @GetMapping
    public List<Game> getGames() {
        return gameService.getAllGames();
    }

    @PostMapping("/import/{startingYear}")
    public int importGames(@PathVariable int startingYear) throws Exception{
        return importService.importGames(startingYear);
    }
}
