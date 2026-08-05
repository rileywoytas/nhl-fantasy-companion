package com.rileywoytas.nhl_stats_api.client;

import com.rileywoytas.nhl_stats_api.dto.BoxScoreDTO;
import com.rileywoytas.nhl_stats_api.dto.GoalieDTO;
import com.rileywoytas.nhl_stats_api.dto.SkaterDTO;
import com.rileywoytas.nhl_stats_api.dto.TeamDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class NHLApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

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

    public BoxScoreDTO getBoxScore(String gameNhlId) {
        String url = "https://api-web.nhle.com/v1/gamecenter/" + gameNhlId + "/boxscore";
        String response = restTemplate.getForObject(url, String.class);

        return parseBoxScore(response);
    }

    private BoxScoreDTO parseBoxScore(String response) {
        BoxScoreDTO boxScoreDTO = new BoxScoreDTO();

        JsonNode root = mapper.readTree(response);
        boxScoreDTO.setId(root.get("id").asLong());
        boxScoreDTO.setSeason(root.get("season").asInt());
        boxScoreDTO.setGameType(root.get("gameType").asInt());
        boxScoreDTO.setGameDate(root.get("gameDate").asString());
        boxScoreDTO.setGameState(root.get("gameState").asString());
        if("FUT".equals(root.get("gameState").asString())) {
            return null;
        }
        if("OFF".equals(boxScoreDTO.getGameState())){
            boxScoreDTO.setGameEndType(root.get("gameOutcome").get("lastPeriodType").asString());
        }
        boxScoreDTO.setHomeTeam(parseTeam(root.get("homeTeam")));
        boxScoreDTO.setAwayTeam(parseTeam(root.get("awayTeam")));

        boxScoreDTO.setSkaters(new ArrayList<>());
        boxScoreDTO.setGoalies(new ArrayList<>());

        if(root.get("playerByGameStats") != null) {

            if (root.get("playerByGameStats").get("homeTeam") != null){
                JsonNode homePlayers = root.get("playerByGameStats").get("homeTeam");

                homePlayers.get("forwards").forEach(forward -> {
                    boxScoreDTO.getSkaters().add(parseSkater(forward));
                });
                homePlayers.get("defense").forEach(defense -> {
                    boxScoreDTO.getSkaters().add(parseSkater(defense));
                });
                homePlayers.get("goalies").forEach(goalie -> {
                    boxScoreDTO.getGoalies().add(parseGoalie(goalie));
                });

            }
            if(root.get("playerByGameStats").get("awayTeam") != null) {
                JsonNode awayPlayers = root.get("playerByGameStats").get("awayTeam");

                awayPlayers.get("forwards").forEach(forward -> {
                    boxScoreDTO.getSkaters().add(parseSkater(forward));
                });
                awayPlayers.get("defense").forEach(defense -> {
                    boxScoreDTO.getSkaters().add(parseSkater(defense));
                });
                awayPlayers.get("goalies").forEach(goalie -> {
                    boxScoreDTO.getGoalies().add(parseGoalie(goalie));
                });
            }
        } else {
            Logger.getLogger(NHLApiClient.class.getName()).log(Level.WARNING, "Player By Game Stats not found");
        }
        return boxScoreDTO;


    }

    private TeamDTO parseTeam(JsonNode teamNode) {
        TeamDTO teamDTO = new TeamDTO();

        teamDTO.setId(teamNode.get("id").asInt());
        if(teamNode.get("score") != null) {
            teamDTO.setScore(teamNode.get("score").asInt());
        }
        if(teamNode.get("sog") != null) {
            teamDTO.setSog(teamNode.get("sog").asInt());
        }

        return teamDTO;
    }

    private SkaterDTO parseSkater(JsonNode skaterNode) {
        SkaterDTO skaterDTO = new SkaterDTO();

        skaterDTO.setPlayerId(skaterNode.get("playerId").asInt());
        skaterDTO.setPosition(skaterNode.get("position").asString());
        skaterDTO.setGoals(skaterNode.get("goals").asInt());
        skaterDTO.setAssists(skaterNode.get("assists").asInt());
        skaterDTO.setPoints(skaterNode.get("points").asInt());
        skaterDTO.setPlusMinus(skaterNode.get("plusMinus").asInt());
        skaterDTO.setPim(skaterNode.get("pim").asInt());
        skaterDTO.setHits(skaterNode.get("hits").asInt());
        skaterDTO.setShots(skaterNode.get("sog").asInt());
        skaterDTO.setBlocks(skaterNode.get("blockedShots").asInt());
        if(skaterNode.get("toi") != null) {
            skaterDTO.setTimeOnIce(skaterNode.get("toi").asString());
        } else {
            skaterDTO.setTimeOnIce("00:00");
        }
        skaterDTO.setShifts(skaterNode.get("shifts") != null ? skaterNode.get("shifts").asInt() : 0);
        skaterDTO.setGiveaways(skaterNode.get("giveaways") != null ?  skaterNode.get("giveaways").asInt() : 0);
        skaterDTO.setTakeaways(skaterNode.get("takeways") != null ?  skaterNode.get("takeways").asInt() : 0);

        return skaterDTO;

    }

    private GoalieDTO parseGoalie(JsonNode goalieNode) {
        GoalieDTO goalieDTO = new GoalieDTO();

        goalieDTO.setPlayerId(goalieNode.get("playerId").asInt());
        goalieDTO.setTimeOnIce(goalieNode.get("toi").asString());
        goalieDTO.setSaves(goalieNode.get("saves").asInt());
        goalieDTO.setShotsAgainst(goalieNode.get("shotsAgainst").asInt());
        goalieDTO.setEvenStrengthGoalsAgainst(goalieNode.get("evenStrengthGoalsAgainst").asInt());
        goalieDTO.setPowerPlayGoalsAgainst(goalieNode.get("powerPlayGoalsAgainst").asInt());
        goalieDTO.setShorthandedGoalsAgainst(goalieNode.get("shorthandedGoalsAgainst").asInt());
        goalieDTO.setGoalsAgainst(goalieNode.get("goalsAgainst").asInt());
        if(goalieNode.get("savePctg") != null) {
            goalieDTO.setSavePercentage(goalieNode.get("savePctg").asDouble());
        }
        if(goalieNode.get("starter") != null) {
            goalieDTO.setStarter(goalieNode.get("starter").asBoolean());
        } else {
            goalieDTO.setStarter(false);
        }

        return goalieDTO;
    }
}
