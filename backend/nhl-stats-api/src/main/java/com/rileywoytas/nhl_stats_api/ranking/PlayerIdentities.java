package com.rileywoytas.nhl_stats_api.ranking;

import com.rileywoytas.nhl_stats_api.entity.Player;
import com.rileywoytas.nhl_stats_api.ranking.matching.PlayerIdentity;

/**
 * The one place that knows the shape of the Player entity for name matching.
 * Adjust the getters here if your entity differs.
 */
final class PlayerIdentities {

    private PlayerIdentities() {
    }

    static PlayerIdentity from(Player player) {
        String team = player.getTeam() == null ? null : player.getTeam().getTriCode();
        return new PlayerIdentity(
                player.getId(),
                player.getFirstName() + " " + player.getLastName(),
                team,
                player.getPosition());
    }
}
