package com.rileywoytas.nhl_stats_api.service;

import com.rileywoytas.nhl_stats_api.client.NHLApiClient;
import com.rileywoytas.nhl_stats_api.dto.BoxScoreDTO;
import com.rileywoytas.nhl_stats_api.dto.GoalieDTO;
import com.rileywoytas.nhl_stats_api.dto.SkaterDTO;
import com.rileywoytas.nhl_stats_api.entity.*;
import com.rileywoytas.nhl_stats_api.repository.GameRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerGameStatsRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerRepository;
import com.rileywoytas.nhl_stats_api.repository.TeamRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class NHLImportService {

    private final NHLApiClient apiClient;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final PlayerGameStatsRepository playerGameStatsRepository;
    private final ObjectMapper mapper;

    private static final int CURRENT_SEASON_START_YEAR = 2025;
    private Logger logger;

    public NHLImportService(NHLApiClient apiClient,
                            TeamRepository teamRepository,
                            PlayerRepository playerRepository,
                            GameRepository gameRepository,
                            PlayerGameStatsRepository playerGameStatsRepository,
                            ObjectMapper mapper) {
        this.apiClient = apiClient;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.playerGameStatsRepository = playerGameStatsRepository;
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

    public String importSeasonBoxScores(String season) throws Exception {

        List<Game> games = gameRepository.findAllBySeason(season);

        List<Long> gameIds = gameRepository.getNonFutureNhlIdsBySeason(season);
        List<BoxScoreDTO> boxScoreDTOS = new ArrayList<>();
        int totalPlayerGameStats = 0;
        Long start = System.currentTimeMillis();
        for(List<Long> batch : partition(gameIds, 50)){
            List<CompletableFuture<BoxScoreDTO>> futures = batch.stream()
                    .map(id -> CompletableFuture.supplyAsync(() -> apiClient.getBoxScore(id.toString()))).toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            futures.stream().map(CompletableFuture::join).forEach(boxScoreDTOS::add);
        }
        Long batchRequestTime = (System.currentTimeMillis() - start) / 1000;

        start = System.currentTimeMillis();
        Set<Long> allGameIds = boxScoreDTOS.stream()
                .map(BoxScoreDTO::getId)
                .collect(Collectors.toSet());


        Set<Integer> allPlayerIds = boxScoreDTOS.stream().flatMap(b -> Stream.concat(
                b.getSkaters().stream().map(SkaterDTO::getPlayerId),
                b.getGoalies().stream().map(GoalieDTO::getPlayerId)
        )).collect(Collectors.toSet());

//        Map<Long, Game> gameMap = gameRepository.findByNhlIdIn(allGameIds).stream()
//                .collect(Collectors.toMap(Game::getNhlId, Function.identity()));
//
//        Map<Integer, Player> playerMap = playerRepository.findByNhlIdIn(allPlayerIds).stream()
//                .collect(Collectors.toMap(Player::getNhlId, Function.identity()));

//        List<PlayerGameStats> statsList = boxScoreDTOS.stream()
//                .flatMap(box -> parsePlayerGameStatsFromBoxScoreDTO(box).stream())
//                .toList();

        List<Game> gamesList = new ArrayList<>();
        List<PlayerGameStats> statsList = new ArrayList<>();

        for (BoxScoreDTO boxScoreDTO : boxScoreDTOS) {
            Game game = parseGameFromBoxScoreDTO(boxScoreDTO);
            gamesList.add(game);
            statsList.addAll(parsePlayerGameStatsFromBoxScoreDTO(boxScoreDTO));
        }

        Long parseBoxScoresTime = (System.currentTimeMillis() - start) / 1000;

        start = System.currentTimeMillis();

        for (List<Game> chunk : partition(gamesList, 500)) {
            gameRepository.saveAll(chunk);
        }

        for(List<PlayerGameStats> chunk : partition(statsList, 500)){
            playerGameStatsRepository.saveAll(chunk);
        }
        Long batchSaveTime = (System.currentTimeMillis() - start) / 1000;

        Long totalImportTime = batchRequestTime + parseBoxScoresTime + batchSaveTime;

        return "Imported " + totalPlayerGameStats + " players game stats across " + games.size() + " games in " +  totalImportTime + "s." +
                "\nBatch Request Time: " + batchRequestTime  + "s" +
                "\nParse Boxscores Time: " + parseBoxScoresTime + "s" +
                "\nBatch Save Time: " + batchSaveTime + "s";
    }

    public int importGameBoxScores(String gameNhlId) {
        BoxScoreDTO boxScoreDTO = apiClient.getBoxScore(gameNhlId);

        if(boxScoreDTO == null){
            return 0;
        } else {

            Game game = parseGameFromBoxScoreDTO(boxScoreDTO);
            gameRepository.save(game);

            List<PlayerGameStats> playerGameStatsList = parsePlayerGameStatsFromBoxScoreDTO(boxScoreDTO);

            playerGameStatsRepository.saveAll(playerGameStatsList);

            return playerGameStatsList.size();
        }
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

    private Game parseGameFromBoxScoreDTO(BoxScoreDTO boxScoreDTO) {
        Game game = gameRepository.findByNhlId(boxScoreDTO.getId()).orElseGet(Game::new);
        game.setNhlId(boxScoreDTO.getId());
        game.setHomeScore(boxScoreDTO.getHomeTeam().getScore());
        game.setAwayScore(boxScoreDTO.getAwayTeam().getScore());
        game.setHomeShots(boxScoreDTO.getHomeTeam().getSog());
        game.setAwayShots(boxScoreDTO.getAwayTeam().getSog());
        game.setGameState(boxScoreDTO.getGameState());
        game.setGameEndType(boxScoreDTO.getGameEndType());
        return game;
    }

    private List<PlayerGameStats> parsePlayerGameStatsFromBoxScoreDTO(BoxScoreDTO boxScoreDTO) {
        List<PlayerGameStats> playerGameStatsList = new ArrayList<>();
        for(SkaterDTO skater : boxScoreDTO.getSkaters()){
            PlayerGameStats playerGameStats = playerGameStatsRepository.findByPlayerIdAndGameId(skater.getPlayerId(), boxScoreDTO.getId())
                    .orElseGet(PlayerGameStats::new);


            playerGameStats.setPlayerId(skater.getPlayerId());
            playerGameStats.setGameId(boxScoreDTO.getId());
            playerGameStats.setPosition(skater.getPosition());
            playerGameStats.setGoals(skater.getGoals());
            playerGameStats.setAssists(skater.getAssists());
            playerGameStats.setPlusMinus(skater.getPlusMinus());
            playerGameStats.setPim(skater.getPim());
            playerGameStats.setHits(skater.getHits());
            playerGameStats.setShots(skater.getShots());
            playerGameStats.setBlocks(skater.getBlocks());
            playerGameStats.setTimeOnIceSeconds(parseToSeconds(skater.getTimeOnIce()));
            playerGameStats.setShifts(skater.getShifts());
            playerGameStats.setGiveaways(skater.getGiveaways());
            playerGameStats.setTakeaways(skater.getTakeaways());

            playerGameStatsList.add(playerGameStats);
        }

        for(GoalieDTO goalieDTO : boxScoreDTO.getGoalies()){
            PlayerGameStats playerGameStats = playerGameStatsRepository.findByPlayerIdAndGameId(goalieDTO.getPlayerId(), boxScoreDTO.getId())
                    .orElseGet(PlayerGameStats::new);

            playerGameStats.setPlayerId(goalieDTO.getPlayerId());
            playerGameStats.setGameId(boxScoreDTO.getId());
            playerGameStats.setTimeOnIceSeconds(parseToSeconds(goalieDTO.getTimeOnIce()));
            playerGameStats.setSaves(goalieDTO.getSaves());
            playerGameStats.setShotsAgainst(goalieDTO.getShotsAgainst());
            playerGameStats.setEvenStrengthGoalsAgainst(goalieDTO.getEvenStrengthGoalsAgainst());
            playerGameStats.setPowerPlayGoalsAgainst(goalieDTO.getPowerPlayGoalsAgainst());
            playerGameStats.setShorthandedGoalsAgainst(goalieDTO.getShorthandedGoalsAgainst());
            playerGameStats.setGoalsAgainst(goalieDTO.getGoalsAgainst());
            playerGameStats.setSavePercentage(goalieDTO.getSavePercentage());
            playerGameStats.setStarter(goalieDTO.getStarter());
            playerGameStats.setPosition("G");

            playerGameStatsList.add(playerGameStats);
        }

        return playerGameStatsList;
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

            game.setGameState(node.get("gameState").asString());
            game.setGameType(mapGameType(gameType));

            gameList.add(game);
        }

        return gameList;
    }

    private <T> List<List<T>> partition(List<T> list, int size){
        List<List<T>> partitions = new ArrayList<>();
        for(int i = 0; i < list.size(); i+= size){
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    private int parseToSeconds(String time){
        if (time == null || !time.contains(":")) {
            return -1;
        }

        String[] parts = time.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);

        return minutes * 60 + seconds;
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
