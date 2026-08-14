package com.rileywoytas.nhl_stats_api.client;

import com.rileywoytas.nhl_stats_api.dto.BoxScoreDTO;
import com.rileywoytas.nhl_stats_api.dto.GoalieDTO;
import com.rileywoytas.nhl_stats_api.dto.SkaterDTO;
import com.rileywoytas.nhl_stats_api.dto.TeamDTO;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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

    private final RestTemplate restTemplate = buildRestTemplate(5000, 20000);
    // The "landing" endpoint returns a much heavier payload (full scoring
    // summary) than the box score, and times out much more easily under
    // concurrent load — give it more headroom.
    private final RestTemplate landingRestTemplate = buildRestTemplate(5000, 30000);
    private final ObjectMapper mapper = new ObjectMapper();

    // Bare `new RestTemplate()` has no timeouts at all, so a single stalled
    // connection can hang indefinitely. Reasonable bounds here mean a slow
    // request fails fast enough for the retry logic below to actually help.
    private static RestTemplate buildRestTemplate(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }

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

    // Used to backfill players who have box score stats but aren't in the
    // players table (retired/left the league since box scores were imported).
    // Returns null if the NHL API doesn't have a record for this ID (very old
    // player IDs sometimes 404 or 500).
    public String getPlayerLanding(Integer playerId) {
        String url = "https://api-web.nhle.com/v1/player/" + playerId + "/landing";
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            Logger.getLogger(NHLApiClient.class.getName())
                    .log(Level.WARNING, "Failed to fetch player landing for id " + playerId + ": " + e.getMessage());
            return null;
        }
    }

    // Season-total reports from the separate Stats REST API. Used for
    // fantasy-relevant fields (PPP/SHG/GWG, goalie W/L/SHO) that the
    // gamecenter box score endpoint doesn't expose per-player.
    public String getSkaterSeasonSummary(String season, int gameTypeId) {
        String cayenneExp = "seasonId=" + season + " and gameTypeId=" + gameTypeId;
        String url = "https://api.nhle.com/stats/rest/en/skater/summary?isAggregate=true&isGame=false&limit=-1&cayenneExp=" + cayenneExp;
        return restTemplate.getForObject(url, String.class);
    }

    public String getGoalieSeasonSummary(String season, int gameTypeId) {
        String cayenneExp = "seasonId=" + season + " and gameTypeId=" + gameTypeId;
        String url = "https://api.nhle.com/stats/rest/en/goalie/summary?isAggregate=true&isGame=false&limit=-1&cayenneExp=" + cayenneExp;
        return restTemplate.getForObject(url, String.class);
    }

    // Shared retry helper — fetching hundreds of games in a full-season
    // import means occasional transient I/O failures (timeouts, connection
    // resets) are expected, not exceptional. Retrying a couple times with a
    // short backoff resolves most of them without giving up on the caller.
    private String getWithRetry(RestTemplate client, String url, String description) {
        int maxAttempts = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return client.getForObject(url, String.class);
            } catch (Exception e) {
                lastException = e;
                Logger.getLogger(NHLApiClient.class.getName()).log(Level.WARNING,
                        "Attempt " + attempt + "/" + maxAttempts + " failed fetching " + description + ": " + e.getMessage());
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(1000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new RuntimeException("Failed to fetch " + description + " after " + maxAttempts + " attempts", lastException);
    }

    public BoxScoreDTO getBoxScore(String gameNhlId) {
        String url = "https://api-web.nhle.com/v1/gamecenter/" + gameNhlId + "/boxscore";
        String response = getWithRetry(restTemplate, url, "box score for game " + gameNhlId);
        return parseBoxScore(response);
    }

    // Raw JSON for the gamecenter "landing" endpoint — has goal-by-goal
    // scoring detail (strength state, assists, running score) that the box
    // score doesn't. Used to derive per-game PPP/SHG/GWG.
    public String getGameLanding(String gameNhlId) {
        String url = "https://api-web.nhle.com/v1/gamecenter/" + gameNhlId + "/landing";
        return getWithRetry(landingRestTemplate, url, "landing for game " + gameNhlId);
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
                Integer homeTeamId = boxScoreDTO.getHomeTeam().getId();

                homePlayers.get("forwards").forEach(forward -> {
                    SkaterDTO skater = parseSkater(forward);
                    skater.setTeamId(homeTeamId);
                    boxScoreDTO.getSkaters().add(skater);
                });
                homePlayers.get("defense").forEach(defense -> {
                    SkaterDTO skater = parseSkater(defense);
                    skater.setTeamId(homeTeamId);
                    boxScoreDTO.getSkaters().add(skater);
                });
                homePlayers.get("goalies").forEach(goalie -> {
                    GoalieDTO goalieDTO = parseGoalie(goalie);
                    goalieDTO.setTeamId(homeTeamId);
                    boxScoreDTO.getGoalies().add(goalieDTO);
                });

            }
            if(root.get("playerByGameStats").get("awayTeam") != null) {
                JsonNode awayPlayers = root.get("playerByGameStats").get("awayTeam");
                Integer awayTeamId = boxScoreDTO.getAwayTeam().getId();

                awayPlayers.get("forwards").forEach(forward -> {
                    SkaterDTO skater = parseSkater(forward);
                    skater.setTeamId(awayTeamId);
                    boxScoreDTO.getSkaters().add(skater);
                });
                awayPlayers.get("defense").forEach(defense -> {
                    SkaterDTO skater = parseSkater(defense);
                    skater.setTeamId(awayTeamId);
                    boxScoreDTO.getSkaters().add(skater);
                });
                awayPlayers.get("goalies").forEach(goalie -> {
                    GoalieDTO goalieDTO = parseGoalie(goalie);
                    goalieDTO.setTeamId(awayTeamId);
                    boxScoreDTO.getGoalies().add(goalieDTO);
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
