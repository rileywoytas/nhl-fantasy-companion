package com.rileywoytas.nhl_stats_api.service;

import com.rileywoytas.nhl_stats_api.client.NHLApiClient;
import com.rileywoytas.nhl_stats_api.entity.Team;
import com.rileywoytas.nhl_stats_api.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class NHLService {

    private final NHLApiClient apiClient;
    private final TeamRepository teamRepository;

    public NHLService(NHLApiClient apiClient,  TeamRepository teamRepository) {
        this.apiClient = apiClient;
        this.teamRepository = teamRepository;
    }

    public int importTeams() {
        String response = apiClient.getTeams();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);

        JsonNode teams = node.get("data");

        int teamCount = teams.size();
        for (JsonNode team : teams) {
            Team t = new Team();
            t.setNhlId(team.get("id").asInt());
            t.setName(team.get("fullName").asString());
            t.setTriCode(team.get("triCode").asString());

            teamRepository.save(t);
        }


        return teamCount;

    }
}
