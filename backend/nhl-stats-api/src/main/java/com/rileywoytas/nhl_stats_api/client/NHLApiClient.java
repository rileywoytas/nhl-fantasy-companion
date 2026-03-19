package com.rileywoytas.nhl_stats_api.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NHLApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getTeams() {
        String url = "https://api.nhle.com/stats/rest/en/team";
        return restTemplate.getForObject(url, String.class);
    }

    //Players with Time-On-Ice > 0
    public String getCurrentPlayers() {
        String url = "https://api-web.nhle.com/v1/skater-stats-leaders/current?categories=toi&limit=-1";
        return restTemplate.getForObject(url, String.class);
    }

    //Goalies with
    public String getCurrentGoalies() {
        String url = "https://api-web.nhle.com/v1/goalie-stats-leaders/current?categories=wins&limit=-1";
        return restTemplate.getForObject(url, String.class);
    }

    public String getTeamsHomeGamesForSeason(String teamAbbrev, String season) {
        String url = "https://api-web.nhle.com/v1/club-schedule-season/" + teamAbbrev + "/" + season;
        return restTemplate.getForObject(url, String.class);
    }
}
