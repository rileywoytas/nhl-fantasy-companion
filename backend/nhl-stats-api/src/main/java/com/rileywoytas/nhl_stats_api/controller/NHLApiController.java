package com.rileywoytas.nhl_stats_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class NHLApiController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/nhl/scoreboard")
    public String getScoreboard() {
        String url = "https://api-web.nhle.com/v1/scoreboard/now";
        return restTemplate.getForObject(url, String.class);
    }
}