package com.rileywoytas.nhl_stats_api.ranking.matching;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Collapses positions ("C,LW", "RW", "D", "G", NHL's "L"/"R") into F / D / G. */
public final class PositionGroup {

    public static final String FORWARD = "F";
    public static final String DEFENCE = "D";
    public static final String GOALIE = "G";

    private static final Set<String> FORWARD_CODES = Set.of("C", "L", "R", "LW", "RW", "W", "F");

    private PositionGroup() {
    }

    /** F, D or G; null if the positions are blank or unrecognized. */
    public static String of(String positions) {
        if (positions == null || positions.isBlank()) {
            return null;
        }
        Set<String> tokens = Arrays.stream(positions.toUpperCase(Locale.ROOT).split("[,/\\s]+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toSet());
        if (tokens.contains("G")) {
            return GOALIE;
        }
        if (tokens.stream().anyMatch(FORWARD_CODES::contains)) {
            return FORWARD;
        }
        if (tokens.contains("D")) {
            return DEFENCE;
        }
        return null;
    }
}
