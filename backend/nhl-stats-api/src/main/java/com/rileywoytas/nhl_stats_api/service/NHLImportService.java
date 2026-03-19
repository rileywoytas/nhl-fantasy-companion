package com.rileywoytas.nhl_stats_api.service;

import com.rileywoytas.nhl_stats_api.client.NHLApiClient;
import com.rileywoytas.nhl_stats_api.entity.Game;
import com.rileywoytas.nhl_stats_api.entity.GameType;
import com.rileywoytas.nhl_stats_api.entity.Player;
import com.rileywoytas.nhl_stats_api.entity.Team;
import com.rileywoytas.nhl_stats_api.repository.GameRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerRepository;
import com.rileywoytas.nhl_stats_api.repository.TeamRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class NHLImportService {

    private final NHLApiClient apiClient;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final ObjectMapper mapper;

    private static final int CURRENT_SEASON_START_YEAR = 2025;
    private Logger logger;

    public NHLImportService(NHLApiClient apiClient,
                            TeamRepository teamRepository,
                            PlayerRepository playerRepository,
                            GameRepository gameRepository,
                            ObjectMapper mapper) {
        this.apiClient = apiClient;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.mapper = mapper;
        this.logger = Logger.getLogger(NHLImportService.class.getName());
    }

    public int importTeams() throws Exception {
        String response = apiClient.getTeams();

        JsonNode root = mapper.readTree(response);
        JsonNode teams = root.get("data");

        List<Team> teamList = new ArrayList<>();
        for (JsonNode teamNode : teams) {

            Integer nhlId = teamNode.get("id").asInt();

            Team team = teamRepository.findByNhlId(nhlId).orElseGet(Team::new);

            team.setNhlId(nhlId);
            team.setName(teamNode.get("fullName").asString());
            team.setTriCode(teamNode.get("triCode").asString());

            teamList.add(team);
        }

        if(!teamList.isEmpty()){
            teamRepository.saveAll(teamList);
        }

        return teamList.size();
    }

    public int importSkaters() throws Exception {
        String response = apiClient.getCurrentPlayers();

        List<Player> skaterList = parsePlayers(response, "toi");

        playerRepository.saveAll(skaterList);

        return skaterList.size();
    }

    public int importGoalies() throws Exception {
        String response = apiClient.getCurrentGoalies();

        List<Player> goalieList = parsePlayers(response, "wins");
        playerRepository.saveAll(goalieList);

        return goalieList.size();

    }

    private List<Player> parsePlayers(String jsonResponse, String enclosingFieldName){
        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode players = root.get(enclosingFieldName);

        Map<String, Team> teamMap = teamRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Team::getTriCode, t -> t));

        List<Player> playerList = new ArrayList<>();
        for (JsonNode node : players) {

            Integer nhlId = node.get("id").asInt();

            Player player = playerRepository.findByNhlId(nhlId)
                    .orElseGet(Player::new);

            player.setNhlId(nhlId);
            player.setFirstName(node.get("firstName").get("default").asString());
            player.setLastName(node.get("lastName").get("default").asString());
            if(node.get("sweaterNumber") != null){
                player.setSweaterNumber(node.get("sweaterNumber").asInt());
            } else {
                player.setSweaterNumber(-1);
            }
            player.setHeadshot(node.get("headshot").asString());
            player.setTeamLogo(node.get("teamLogo").asString());
            player.setPosition(node.get("position").asString());

            String teamAbbrev = node.get("teamAbbrev").asString();
            Team team = teamMap.get(teamAbbrev);

            if(team != null){
                player.setTeam(team);
            } else {
                logger.log(Level.WARNING, "Team with abbrev " + teamAbbrev + " not found for player: " + player.getFirstName() + " " + player.getLastName() + " ID: " + player.getNhlId());
            }

            playerList.add(player);
        }

        return playerList;
    }

    public int importGames(int startingYear) throws Exception {
        List<Team> allTeams =  teamRepository.findAll();

        int totalGames = 0;
        for(int year = startingYear; year <= CURRENT_SEASON_START_YEAR; year++){
            String season = year + "" + (year + 1);
            for (Team team : allTeams) {
                List<Game> seasonHomeGames = parseSeasonHomeGames(apiClient.getTeamsHomeGamesForSeason(team.getTriCode(), season), team.getTriCode());
                totalGames += seasonHomeGames.size();
                gameRepository.saveAll(seasonHomeGames);
                logger.log(Level.INFO, "Imported " + seasonHomeGames.size() + " home games from " + team.getTriCode() + " " + season + " season.");
            }
        }


        return totalGames;
    }

    private List<Game> parseSeasonHomeGames(String jsonResponse, String teamAbbreviation){

        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode games = root.get("games");

        Map<String, Team> teamMap = teamRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Team::getTriCode, t -> t));

        List<Game> gameList = new ArrayList<>();

        for(JsonNode node : games){

            //Only processing home games to avoid processing every game twice
            if(node.get("homeTeam").get("abbrev") == null ||
                    !node.get("homeTeam").get("abbrev").asString().equals(teamAbbreviation)){
                continue;
            }

            Long nhlId = node.get("id").asLong();

            Game game = gameRepository.findByNhlId(nhlId).orElseGet(Game::new);

            game.setNhlId(nhlId);

            String dateStr = node.get("startTimeUTC").asString();
            game.setGameDate(OffsetDateTime.parse(dateStr));

            String homeAbbrev = node.get("homeTeam").get("abbrev").asString();
            String awayAbbrev = node.get("awayTeam").get("abbrev").asString();

            Team home = teamMap.get(homeAbbrev);
            Team away = teamMap.get(awayAbbrev);

            if (home == null || away == null) continue;

            game.setHomeTeam(home);
            game.setAwayTeam(away);
            game.setSeason(node.get("season").asString());

            int gameType = 0;
            if(node.get("gameType") != null){
                gameType = node.get("gameType").asInt();
            }

            game.setGameType(mapGameType(gameType));

            gameList.add(game);
        }

        return gameList;
    }

    private GameType mapGameType(int gameType){
        return switch (gameType) {
            case 1 -> GameType.PRESEASON;
            case 2 -> GameType.REGULAR_SEASON;
            case 3 -> GameType.PLAYOFFS;
            default -> throw new IllegalArgumentException("Unknown game type: " + gameType);
        };
    }
}
