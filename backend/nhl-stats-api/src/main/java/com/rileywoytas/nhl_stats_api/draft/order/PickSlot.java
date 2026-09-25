package com.rileywoytas.nhl_stats_api.draft.order;

/**
 * One slot in the draft.
 *
 * @param overall     1-based overall pick number
 * @param round       1-based round
 * @param pickInRound 1-based position within the round
 * @param teamSlot    1-based draft slot of the team making this pick
 */
public record PickSlot(int overall, int round, int pickInRound, int teamSlot) {
}
