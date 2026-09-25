package com.rileywoytas.nhl_stats_api.ranking.matching;

import java.util.List;

public record MatchResult(MatchStatus status, PlayerIdentity player, List<PlayerIdentity> candidates) {

    public static MatchResult matched(PlayerIdentity player) {
        return new MatchResult(MatchStatus.MATCHED, player, List.of(player));
    }

    public static MatchResult ambiguous(List<PlayerIdentity> candidates) {
        return new MatchResult(MatchStatus.AMBIGUOUS, null, List.copyOf(candidates));
    }

    public static MatchResult notFound() {
        return new MatchResult(MatchStatus.NOT_FOUND, null, List.of());
    }

    public boolean isMatched() {
        return status == MatchStatus.MATCHED;
    }
}
