package com.rileywoytas.nhl_stats_api.ranking.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Matches a ranking row (name, team, positions as the fantasy platform writes them)
 * to a player in the database.
 *
 * Strategy:
 *   1. Exact normalized full name. If several players share it (e.g. the two Sebastian Ahos),
 *      narrow by team, then by position group (e.g. the two Elias Petterssons on VAN).
 *   2. Otherwise fall back to surname + team + same first initial, which covers
 *      nicknames like Mitch/Mitchell or Nick/Nicholas.
 */
public class PlayerMatcher {

    private final Map<String, List<PlayerIdentity>> byFullName = new HashMap<>();
    private final Map<String, List<PlayerIdentity>> byLastName = new HashMap<>();

    public PlayerMatcher(Collection<PlayerIdentity> players) {
        for (PlayerIdentity player : players) {
            String fullName = NameNormalizer.normalize(player.fullName());
            if (fullName.isEmpty()) {
                continue;
            }
            byFullName.computeIfAbsent(fullName, key -> new ArrayList<>()).add(player);
            byLastName.computeIfAbsent(NameNormalizer.lastToken(fullName), key -> new ArrayList<>()).add(player);
        }
    }

    public MatchResult match(String rawName, String rawTeam, String rawPositions) {
        String name = NameNormalizer.normalize(rawName);
        if (name.isEmpty()) {
            return MatchResult.notFound();
        }
        String team = TeamCodes.canonical(rawTeam);
        String group = PositionGroup.of(rawPositions);

        List<PlayerIdentity> exact = byFullName.getOrDefault(name, List.of());
        if (!exact.isEmpty()) {
            return narrow(exact, team, group);
        }

        if (team == null) {
            return MatchResult.notFound();
        }
        char initial = NameNormalizer.firstInitial(name);
        List<PlayerIdentity> surnameAndTeam = byLastName.getOrDefault(NameNormalizer.lastToken(name), List.of())
                .stream()
                .filter(player -> team.equals(TeamCodes.canonical(player.teamCode())))
                .filter(player -> NameNormalizer.firstInitial(NameNormalizer.normalize(player.fullName())) == initial)
                .toList();
        if (surnameAndTeam.isEmpty()) {
            return MatchResult.notFound();
        }
        return narrow(surnameAndTeam, team, group);
    }

    private static MatchResult narrow(List<PlayerIdentity> candidates, String team, String group) {
        if (candidates.size() == 1) {
            return MatchResult.matched(candidates.get(0));
        }

        List<PlayerIdentity> sameTeam = team == null
                ? List.of()
                : candidates.stream().filter(p -> team.equals(TeamCodes.canonical(p.teamCode()))).toList();
        if (sameTeam.size() == 1) {
            return MatchResult.matched(sameTeam.get(0));
        }

        List<PlayerIdentity> pool = sameTeam.isEmpty() ? candidates : sameTeam;
        if (group != null) {
            List<PlayerIdentity> sameGroup = pool.stream()
                    .filter(p -> Objects.equals(group, PositionGroup.of(p.position())))
                    .toList();
            if (sameGroup.size() == 1) {
                return MatchResult.matched(sameGroup.get(0));
            }
        }
        return MatchResult.ambiguous(candidates);
    }
}
