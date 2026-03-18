package com.rileywoytas.nhl_stats_api.controller;

import com.rileywoytas.nhl_stats_api.service.NHLService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class NHLApiController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final NHLService nhlService;

    public NHLApiController(NHLService nhlService) {
        this.nhlService = nhlService;
    }

    @GetMapping("/importTeams")
    public String importTeams() {
        int teamsImported = nhlService.importTeams();

        return "Successfully imported " + teamsImported + " teams.";
    }
}