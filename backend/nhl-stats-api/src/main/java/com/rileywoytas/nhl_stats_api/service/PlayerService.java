package com.rileywoytas.nhl_stats_api.service;

import com.rileywoytas.nhl_stats_api.entity.Player;
import com.rileywoytas.nhl_stats_api.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player getByNhlId(Integer nhlId) {
        return playerRepository.findByNhlId(nhlId).orElseThrow(() -> new RuntimeException("Player with NHL ID " + nhlId + " not found"));
    }
}
