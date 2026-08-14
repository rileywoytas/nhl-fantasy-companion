package com.rileywoytas.nhl_stats_api.service;

import com.rileywoytas.nhl_stats_api.client.NHLApiClient;
import com.rileywoytas.nhl_stats_api.dto.BoxScoreDTO;
import com.rileywoytas.nhl_stats_api.dto.GoalieDTO;
import com.rileywoytas.nhl_stats_api.dto.SkaterDTO;
import com.rileywoytas.nhl_stats_api.entity.*;
import com.rileywoytas.nhl_stats_api.repository.GameRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerAdvancedSeasonStatsRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerGameStatsRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerRepository;
import com.rileywoytas.nhl_stats_api.repository.TeamRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class NHLImportService {

    private final NHLApiClient apiClient;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final PlayerGameStatsRepository playerGameStatsRepository;
    private final PlayerAdvancedSeasonStatsRepository advancedStatsRepository;
    private final ObjectMapper mapper;

    private static final int CURRENT_SEASON_START_YEAR = 2025;
    private final Logger logger;

    public NHLImportService(NHLApiClient apiClient,
                            TeamRepository teamRepository,
                            PlayerRepository playerRepository,
                            GameRepository gameRepository,
                            PlayerGameStatsRepository playerGameStatsRepository,
                            PlayerAdvancedSeasonStatsRepository advancedStatsRepository,
                            ObjectMapper mapper) {
        this.apiClient = apiClient;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.playerGameStatsRepository = playerGameStatsRepository;
        this.advancedStatsRepository = advancedStatsRepository;
        this.mapper = mapper;
        this.logger = Logger.getLogger(NHLImportService.class.getName());
    }

    public int importTeams() {
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

    public int importSkaters() {
        String response = apiClient.getCurrentPlayers();

        List<Player> skaterList = parsePlayers(response, "toi");

        playerRepository.saveAll(skaterList);

        return skaterList.size();
    }

    public int importGoalies() {
        String response = apiClient.getCurrentGoalies();

        List<Player> goalieList = parsePlayers(response, "wins");
        playerRepository.saveAll(goalieList);

        return goalieList.size();

    }

    // Backfills players who have box score stats but no row in `players` —
    // typically players who've left the league since box scores started
    // being imported. Fetches each individually from the player landing
    // endpoint, so this can be slow for large batches; each player is
    // isolated in a try/catch so one bad/missing ID doesn't abort the rest.
    public String backfillMissingPlayers() {
        List<Integer> missingIds = playerRepository.findPlayerIdsMissingFromPlayers();

        Map<String, Team> teamMap = teamRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Team::getTriCode, t -> t));

        List<Player> playersToSave = new ArrayList<>();
        List<Integer> failedIds = new ArrayList<>();

        for (Integer nhlId : missingIds) {
            try {
                String response = apiClient.getPlayerLanding(nhlId);
                if (response == null) {
                    failedIds.add(nhlId);
                    continue;
                }

                Player player = parsePlayerLanding(response, teamMap);
                if (player != null) {
                    playersToSave.add(player);
                } else {
                    failedIds.add(nhlId);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to backfill player " + nhlId + ": " + e.getMessage());
                failedIds.add(nhlId);
            }
        }

        for (List<Player> chunk : partition(playersToSave, 500)) {
            playerRepository.saveAll(chunk);
        }

        return "Backfilled " + playersToSave.size() + " of " + missingIds.size() + " missing players." +
                (failedIds.isEmpty() ? "" : " Failed IDs: " + failedIds);
    }

    private Player parsePlayerLanding(String jsonResponse, Map<String, Team> teamMap) {
        JsonNode root = mapper.readTree(jsonResponse);

        if (root.get("playerId") == null) {
            return null;
        }

        Integer nhlId = root.get("playerId").asInt();
        Player player = playerRepository.findByNhlId(nhlId).orElseGet(Player::new);
        player.setNhlId(nhlId);

        if (root.get("firstName") != null && root.get("firstName").get("default") != null) {
            player.setFirstName(root.get("firstName").get("default").asString());
        }
        if (root.get("lastName") != null && root.get("lastName").get("default") != null) {
            player.setLastName(root.get("lastName").get("default").asString());
        }
        if (root.get("sweaterNumber") != null) {
            player.setSweaterNumber(root.get("sweaterNumber").asInt());
        } else {
            player.setSweaterNumber(-1);
        }
        if (root.get("headshot") != null) {
            player.setHeadshot(root.get("headshot").asString());
        }
        if (root.get("position") != null) {
            player.setPosition(root.get("position").asString());
        }

        // The landing endpoint uses "currentTeamAbbrev" (unlike the current
        // skaters/goalies leaders endpoints, which use "teamAbbrev") since it
        // also carries team history. Falling back to "teamAbbrev" just in
        // case the shape differs from what's expected here.
        JsonNode teamAbbrevNode = root.get("currentTeamAbbrev") != null
                ? root.get("currentTeamAbbrev")
                : root.get("teamAbbrev");

        if (teamAbbrevNode != null) {
            Team team = teamMap.get(teamAbbrevNode.asString());
            if (team != null) {
                player.setTeam(team);
                if (root.get("teamLogo") != null) {
                    player.setTeamLogo(root.get("teamLogo").asString());
                }
            }
        }

        return player;
    }

    public String importSeasonBoxScores(String season) {

        List<Game> games = gameRepository.findAllBySeason(season);

        List<Long> gameIds = gameRepository.getNonFutureNhlIdsBySeason(season);
        int totalPlayerGameStats = 0;
        int gamesSaved = 0;
        List<Long> failedGameIds = new java.util.concurrent.CopyOnWriteArrayList<>();

        Long start = System.currentTimeMillis();

        for (List<Long> batch : partition(gameIds, 50)) {
            // Isolate each game's fetch so one bad/still-failing request
            // (after retries inside getBoxScore) doesn't take down the whole
            // batch — CompletableFuture.join() rethrows by default, which
            // previously meant a single transient failure aborted the entire
            // season import and discarded everything fetched so far.
            List<CompletableFuture<BoxScoreDTO>> futures = batch.stream()
                    .map(id -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return apiClient.getBoxScore(id.toString());
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Giving up on game " + id + " after retries: " + e.getMessage());
                            failedGameIds.add(id);
                            return null;
                        }
                    })).toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            List<BoxScoreDTO> batchResults = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            // Save each batch as it completes rather than accumulating
            // everything in memory until the end, so progress already made
            // is never lost even if a later batch has problems.
            List<Game> gamesList = new ArrayList<>();
            List<PlayerGameStats> statsList = new ArrayList<>();
            for (BoxScoreDTO boxScoreDTO : batchResults) {
                gamesList.add(parseGameFromBoxScoreDTO(boxScoreDTO));
                statsList.addAll(parsePlayerGameStatsFromBoxScoreDTO(boxScoreDTO));
            }

            for (List<Game> chunk : partition(gamesList, 500)) {
                gameRepository.saveAll(chunk);
            }
            for (List<PlayerGameStats> chunk : partition(statsList, 500)) {
                playerGameStatsRepository.saveAll(chunk);
            }

            gamesSaved += gamesList.size();
            totalPlayerGameStats += statsList.size();
        }

        Long totalImportTime = (System.currentTimeMillis() - start) / 1000;

        String result = "Imported " + totalPlayerGameStats + " player game stats across " + gamesSaved
                + " of " + games.size() + " games in " + totalImportTime + "s.";
        if (!failedGameIds.isEmpty()) {
            result += "\nFailed after retries (" + failedGameIds.size() + "): " + failedGameIds
                    + " — re-run this import to retry just these, existing games are unaffected.";
        }
        return result;
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



    public int importGames(int startingYear) {
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
            playerGameStats.setTeamId(skater.getTeamId() != null ? skater.getTeamId().longValue() : null);
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
            playerGameStats.setTeamId(goalieDTO.getTeamId() != null ? goalieDTO.getTeamId().longValue() : null);
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

    private int mapGameTypeToId(GameType gameType) {
        return switch (gameType) {
            case PRESEASON -> 1;
            case REGULAR_SEASON -> 2;
            case PLAYOFFS -> 3;
        };
    }

    // Imports season-total PPP/SHG/GWG for skaters from the Stats REST API.
    public String importAdvancedSkaterStats(String season, GameType gameType) {
        int gameTypeId = mapGameTypeToId(gameType);
        String response = apiClient.getSkaterSeasonSummary(season, gameTypeId);

        JsonNode root = mapper.readTree(response);
        JsonNode data = root.get("data");

        List<PlayerAdvancedSeasonStats> statsList = new ArrayList<>();
        if (data != null) {
            for (JsonNode node : data) {
                if (node.get("playerId") == null) continue;
                Integer playerId = node.get("playerId").asInt();

                PlayerAdvancedSeasonStats stats = advancedStatsRepository
                        .findByPlayerIdAndSeasonAndGameType(playerId, season, gameType)
                        .orElseGet(PlayerAdvancedSeasonStats::new);

                stats.setPlayerId(playerId);
                stats.setSeason(season);
                stats.setGameType(gameType);
                stats.setPowerPlayPoints(node.get("ppPoints") != null ? node.get("ppPoints").asInt() : null);
                stats.setShorthandedGoals(node.get("shGoals") != null ? node.get("shGoals").asInt() : null);
                stats.setGameWinningGoals(node.get("gameWinningGoals") != null ? node.get("gameWinningGoals").asInt() : null);

                statsList.add(stats);
            }
        }

        for (List<PlayerAdvancedSeasonStats> chunk : partition(statsList, 500)) {
            advancedStatsRepository.saveAll(chunk);
        }

        return "Imported advanced stats for " + statsList.size() + " skaters (" + season + ", " + gameType + ").";
    }

    // Imports season-total W/L/OTL/SHO for goalies from the Stats REST API.
    public String importAdvancedGoalieStats(String season, GameType gameType) {
        int gameTypeId = mapGameTypeToId(gameType);
        String response = apiClient.getGoalieSeasonSummary(season, gameTypeId);

        JsonNode root = mapper.readTree(response);
        JsonNode data = root.get("data");

        List<PlayerAdvancedSeasonStats> statsList = new ArrayList<>();
        if (data != null) {
            for (JsonNode node : data) {
                if (node.get("playerId") == null) continue;
                Integer playerId = node.get("playerId").asInt();

                PlayerAdvancedSeasonStats stats = advancedStatsRepository
                        .findByPlayerIdAndSeasonAndGameType(playerId, season, gameType)
                        .orElseGet(PlayerAdvancedSeasonStats::new);

                stats.setPlayerId(playerId);
                stats.setSeason(season);
                stats.setGameType(gameType);
                stats.setWins(node.get("wins") != null ? node.get("wins").asInt() : null);
                stats.setLosses(node.get("losses") != null ? node.get("losses").asInt() : null);
                stats.setOtLosses(node.get("otLosses") != null ? node.get("otLosses").asInt() : null);
                stats.setShutouts(node.get("shutouts") != null ? node.get("shutouts").asInt() : null);

                statsList.add(stats);
            }
        }

        for (List<PlayerAdvancedSeasonStats> chunk : partition(statsList, 500)) {
            advancedStatsRepository.saveAll(chunk);
        }

        return "Imported advanced stats for " + statsList.size() + " goalies (" + season + ", " + gameType + ").";
    }
}