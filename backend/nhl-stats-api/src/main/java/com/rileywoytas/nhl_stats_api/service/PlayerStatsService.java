package com.rileywoytas.nhl_stats_api.service;

import com.rileywoytas.nhl_stats_api.dto.PlayerSeasonStatsDTO;
import com.rileywoytas.nhl_stats_api.entity.Player;
import com.rileywoytas.nhl_stats_api.repository.PlayerGameStatsRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerSeasonTotalsProjection;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlayerStatsService {

    private final PlayerGameStatsRepository playerGameStatsRepository;
    private final PlayerRepository playerRepository;

    public PlayerStatsService(PlayerGameStatsRepository playerGameStatsRepository, PlayerRepository playerRepository) {
        this.playerGameStatsRepository = playerGameStatsRepository;
        this.playerRepository = playerRepository;
    }

    public List<PlayerSeasonStatsDTO> getSeasonTotals(String season) {
        List<PlayerSeasonTotalsProjection> totals = playerGameStatsRepository.findSeasonTotals(season);

        Map<Integer, Player> playersByNhlId = playerRepository.findByNhlIdIn(
                totals.stream().map(PlayerSeasonTotalsProjection::getPlayerId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(Player::getNhlId, Function.identity()));

        return totals.stream()
                .map(t -> toDto(t, playersByNhlId.get(t.getPlayerId())))
                .collect(Collectors.toList());
    }

    public Optional<PlayerSeasonStatsDTO> getSeasonTotalsForPlayer(String season, Integer playerNhlId) {
        return playerGameStatsRepository.findSeasonTotalsForPlayer(season, playerNhlId)
                .map(t -> toDto(t, playerRepository.findByNhlId(playerNhlId).orElse(null)));
    }

    private PlayerSeasonStatsDTO toDto(PlayerSeasonTotalsProjection t, Player player) {
        PlayerSeasonStatsDTO dto = new PlayerSeasonStatsDTO();

        dto.setPlayerId(t.getPlayerId());
        if (player != null) {
            dto.setFirstName(player.getFirstName());
            dto.setLastName(player.getLastName());
            dto.setPosition(player.getPosition());
            dto.setHeadshot(player.getHeadshot());
            if (player.getTeam() != null) {
                dto.setTeamTriCode(player.getTeam().getTriCode());
            }
        }

        dto.setSeason(t.getSeason());
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

        return dto;
    }
}