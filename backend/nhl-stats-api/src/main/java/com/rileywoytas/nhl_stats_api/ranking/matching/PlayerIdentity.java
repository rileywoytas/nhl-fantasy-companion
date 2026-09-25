package com.rileywoytas.nhl_stats_api.ranking.matching;

import java.util.UUID;

/**
 * The minimal view of a player needed for name matching. Kept separate from the
 * JPA entity so the matcher can be tested with plain objects.
 */
public record PlayerIdentity(UUID id, String fullName, String teamCode, String position) {

    public String describe() {
        return fullName + " (" + teamCode + ", " + position + ", id=" + id + ")";
    }
}
