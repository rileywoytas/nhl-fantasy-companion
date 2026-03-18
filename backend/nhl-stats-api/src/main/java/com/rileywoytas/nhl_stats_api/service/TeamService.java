package com.rileywoytas.nhl_stats_api.service;

import com.rileywoytas.nhl_stats_api.entity.Team;
import com.rileywoytas.nhl_stats_api.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getByNhlId(Integer nhlId) {
        return teamRepository.findByNhlId(nhlId)
                .orElseThrow(() -> new RuntimeException("Team with NHL ID " + nhlId + " not found"));
    }
}