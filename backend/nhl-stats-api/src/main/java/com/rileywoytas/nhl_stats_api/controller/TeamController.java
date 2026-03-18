package com.rileywoytas.nhl_stats_api.controller;

import com.rileywoytas.nhl_stats_api.entity.Team;
import com.rileywoytas.nhl_stats_api.service.NHLImportService;
import com.rileywoytas.nhl_stats_api.service.TeamService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;
    private final NHLImportService importService;

    public TeamController(TeamService teamService, NHLImportService importService) {
        this.teamService = teamService;
        this.importService = importService;
    }

    @GetMapping
    public List<Team> getTeams() {
        return teamService.getAllTeams();
    }

    @PostMapping("/import")
    public int importTeams() throws Exception {
        return importService.importTeams();
    }
}