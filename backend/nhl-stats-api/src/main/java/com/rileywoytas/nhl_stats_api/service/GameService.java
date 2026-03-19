package com.rileywoytas.nhl_stats_api.service;

import com.rileywoytas.nhl_stats_api.entity.Game;
import com.rileywoytas.nhl_stats_api.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }
}
