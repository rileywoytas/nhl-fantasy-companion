package com.rileywoytas.nhl_stats_api.draft.order;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Pure snake-draft ordering: odd rounds run 1..N, even rounds run N..1.
 * All numbers are 1-based.
 */
public final class SnakeOrder {

    private final int teamCount;
    private final int rounds;

    public SnakeOrder(int teamCount, int rounds) {
        if (teamCount < 2) {
            throw new IllegalArgumentException("teamCount must be at least 2, was " + teamCount);
        }
        if (rounds < 1) {
            throw new IllegalArgumentException("rounds must be at least 1, was " + rounds);
        }
        this.teamCount = teamCount;
        this.rounds = rounds;
    }

    public int teamCount() {
        return teamCount;
    }

    public int rounds() {
        return rounds;
    }

    public int totalPicks() {
        return teamCount * rounds;
    }

    public PickSlot slotFor(int overallPick) {
        if (overallPick < 1 || overallPick > totalPicks()) {
            throw new IllegalArgumentException(
                    "overallPick must be between 1 and " + totalPicks() + ", was " + overallPick);
        }
        int zeroBased = overallPick - 1;
        int round = zeroBased / teamCount + 1;
        int pickInRound = zeroBased % teamCount + 1;
        return new PickSlot(overallPick, round, pickInRound, teamSlotFor(round, pickInRound));
    }

    public List<PickSlot> slotsForTeam(int teamSlot) {
        requireValidTeamSlot(teamSlot);
        return IntStream.rangeClosed(1, rounds)
                .mapToObj(round -> {
                    int pickInRound = isForwardRound(round) ? teamSlot : teamCount - teamSlot + 1;
                    int overall = (round - 1) * teamCount + pickInRound;
                    return new PickSlot(overall, round, pickInRound, teamSlot);
                })
                .toList();
    }

    /** The team's next pick at or after {@code fromOverallInclusive}, or empty if it has none left. */
    public OptionalInt nextPickForTeam(int fromOverallInclusive, int teamSlot) {
        return slotsForTeam(teamSlot).stream()
                .mapToInt(PickSlot::overall)
                .filter(overall -> overall >= fromOverallInclusive)
                .findFirst();
    }

    private int teamSlotFor(int round, int pickInRound) {
        return isForwardRound(round) ? pickInRound : teamCount - pickInRound + 1;
    }

    private static boolean isForwardRound(int round) {
        return round % 2 == 1;
    }

    private void requireValidTeamSlot(int teamSlot) {
        if (teamSlot < 1 || teamSlot > teamCount) {
            throw new IllegalArgumentException(
                    "teamSlot must be between 1 and " + teamCount + ", was " + teamSlot);
        }
    }
}
