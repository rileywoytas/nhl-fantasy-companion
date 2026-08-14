package com.rileywoytas.nhl_stats_api.service;

import com.rileywoytas.nhl_stats_api.dto.PlayerGameLogEntryDTO;
import com.rileywoytas.nhl_stats_api.dto.PlayerSeasonStatsDTO;
import com.rileywoytas.nhl_stats_api.entity.GameType;
import com.rileywoytas.nhl_stats_api.entity.Player;
import com.rileywoytas.nhl_stats_api.entity.PlayerAdvancedSeasonStats;
import com.rileywoytas.nhl_stats_api.repository.PlayerAdvancedSeasonStatsRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerGameLogEntryProjection;
import com.rileywoytas.nhl_stats_api.repository.PlayerGameStatsRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerSeasonTotalsProjection;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlayerStatsService {

    private final PlayerGameStatsRepository playerGameStatsRepository;
    private final PlayerRepository playerRepository;
    private final PlayerAdvancedSeasonStatsRepository advancedStatsRepository;

    public PlayerStatsService(PlayerGameStatsRepository playerGameStatsRepository,
                               PlayerRepository playerRepository,
                               PlayerAdvancedSeasonStatsRepository advancedStatsRepository) {
        this.playerGameStatsRepository = playerGameStatsRepository;
        this.playerRepository = playerRepository;
        this.advancedStatsRepository = advancedStatsRepository;
    }

    public List<PlayerSeasonStatsDTO> getSeasonTotals(String season, String gameType) {
        List<PlayerSeasonTotalsProjection> totals = playerGameStatsRepository.findSeasonTotals(season, gameType);

        Map<Integer, Player> playersByNhlId = playerRepository.findByNhlIdIn(
                totals.stream().map(PlayerSeasonTotalsProjection::getPlayerId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Player::getNhlId, Function.identity()));

        Map<Integer, PlayerAdvancedSeasonStats> advancedByPlayerId = advancedStatsMap(season, gameType);

        return totals.stream()
                .map(t -> toDto(t, playersByNhlId.get(t.getPlayerId()), gameType, advancedByPlayerId.get(t.getPlayerId())))
                .collect(Collectors.toList());
    }

    public Optional<PlayerSeasonStatsDTO> getSeasonTotalsForPlayer(String season, String gameType, Integer playerNhlId) {
        Map<Integer, PlayerAdvancedSeasonStats> advancedByPlayerId = advancedStatsMap(season, gameType);

        return playerGameStatsRepository.findSeasonTotalsForPlayer(season, gameType, playerNhlId)
                .map(t -> toDto(t, playerRepository.findByNhlId(playerNhlId).orElse(null), gameType, advancedByPlayerId.get(playerNhlId)));
    }

    // Looks up advanced (Stats REST API) totals for the given season/gameType.
    // Returns an empty map (rather than failing) if the gameType string
    // doesn't match a known GameType or nothing's been imported yet — the
    // rest of the response still works, just without PPP/SHG/GWG/W/L/SHO.
    private Map<Integer, PlayerAdvancedSeasonStats> advancedStatsMap(String season, String gameType) {
        GameType parsed;
        try {
            parsed = GameType.valueOf(gameType.toUpperCase());
        } catch (Exception e) {
            return Collections.emptyMap();
        }

        return advancedStatsRepository.findBySeasonAndGameType(season, parsed).stream()
                .collect(Collectors.toMap(PlayerAdvancedSeasonStats::getPlayerId, Function.identity(), (a, b) -> a));
    }

    private PlayerSeasonStatsDTO toDto(PlayerSeasonTotalsProjection t, Player player, String gameType, PlayerAdvancedSeasonStats advanced) {
        PlayerSeasonStatsDTO dto = new PlayerSeasonStatsDTO();

        dto.setPlayerId(t.getPlayerId());
        if (player != null) {
            dto.setFirstName(player.getFirstName());
            dto.setLastName(player.getLastName());
            dto.setPosition(player.getPosition());
            dto.setHeadshot(player.getHeadshot());
            dto.setTeamLogo(player.getTeamLogo());
            if (player.getTeam() != null) {
                dto.setTeamTriCode(player.getTeam().getTriCode());
            }
        }

        dto.setSeason(t.getSeason());
        dto.setGameType(gameType);
        dto.setGamesPlayed(t.getGamesPlayed());

        dto.setGoals(t.getGoals());
        dto.setAssists(t.getAssists());
        dto.setPoints(t.getPoints());
        dto.setShots(t.getShots());
        dto.setHits(t.getHits());
        dto.setBlocks(t.getBlocks());
        dto.setPim(t.getPim());
        dto.setPlusMinus(t.getPlusMinus());
        dto.setGiveaways(t.getGiveaways());
        dto.setTakeaways(t.getTakeaways());
        dto.setTimeOnIceSeconds(t.getTimeOnIceSeconds());

        dto.setStarts(t.getStarts());
        dto.setSaves(t.getSaves());
        dto.setShotsAgainst(t.getShotsAgainst());
        dto.setGoalsAgainst(t.getGoalsAgainst());
        dto.setSavePercentage(t.getSavePercentage());

        if (advanced != null) {
            dto.setPowerPlayPoints(advanced.getPowerPlayPoints());
            dto.setShorthandedGoals(advanced.getShorthandedGoals());
            dto.setGameWinningGoals(advanced.getGameWinningGoals());
            dto.setWins(advanced.getWins());
            dto.setLosses(advanced.getLosses());
            dto.setOtLosses(advanced.getOtLosses());
            dto.setShutouts(advanced.getShutouts());
        }

        return dto;
    }

    public List<PlayerGameLogEntryDTO> getGameLog(String season, String gameType, Integer playerNhlId) {
        return playerGameStatsRepository.findGameLog(playerNhlId, season, gameType).stream()
                .map(this::toGameLogDto)
                .collect(Collectors.toList());
    }

    private PlayerGameLogEntryDTO toGameLogDto(PlayerGameLogEntryProjection p) {
        PlayerGameLogEntryDTO dto = new PlayerGameLogEntryDTO();
        dto.setGameDate(p.getGameDate());
        dto.setOpponent(p.getOpponent());
        dto.setIsHome(p.getIsHome());
        dto.setGoals(p.getGoals());
        dto.setAssists(p.getAssists());
        dto.setPoints(p.getPoints());
        dto.setPlusMinus(p.getPlusMinus());
        dto.setShots(p.getShots());
        dto.setHits(p.getHits());
        dto.setBlocks(p.getBlocks());
        dto.setPim(p.getPim());
        dto.setTimeOnIceSeconds(p.getTimeOnIceSeconds());
        dto.setPowerPlayPoints(p.getPowerPlayPoints());
        dto.setShorthandedGoals(p.getShorthandedGoals());
        dto.setGameWinningGoals(p.getGameWinningGoals());
        dto.setGoalHighlightUrl(p.getGoalHighlightUrl());
        dto.setSaves(p.getSaves());
        dto.setShotsAgainst(p.getShotsAgainst());
        dto.setGoalsAgainst(p.getGoalsAgainst());
        dto.setSavePercentage(p.getSavePercentage());
        dto.setStarter(p.getStarter());
        return dto;
    }
}
