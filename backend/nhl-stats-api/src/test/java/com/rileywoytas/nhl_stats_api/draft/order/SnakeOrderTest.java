package com.rileywoytas.nhl_stats_api.draft.order;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnakeOrderTest {

    private final SnakeOrder order = new SnakeOrder(14, 3);

    @Test
    void firstRoundRunsInSlotOrder() {
        assertThat(order.slotFor(1).teamSlot()).isEqualTo(1);
        assertThat(order.slotFor(14).teamSlot()).isEqualTo(14);
    }

    @Test
    void secondRoundReverses() {
        PickSlot firstOfRoundTwo = order.slotFor(15);

        assertThat(firstOfRoundTwo.round()).isEqualTo(2);
        assertThat(firstOfRoundTwo.pickInRound()).isEqualTo(1);
        assertThat(firstOfRoundTwo.teamSlot()).isEqualTo(14);
        assertThat(order.slotFor(28).teamSlot()).isEqualTo(1);
    }

    @Test
    void teamsAtTheTurnPickBackToBack() {
        assertThat(order.slotFor(14).teamSlot()).isEqualTo(order.slotFor(15).teamSlot());
        assertThat(order.slotFor(28).teamSlot()).isEqualTo(order.slotFor(29).teamSlot());
    }

    @Test
    void listsEveryPickForATeam() {
        assertThat(order.slotsForTeam(5))
                .extracting(PickSlot::overall)
                .containsExactly(5, 24, 33);
    }

    @Test
    void everyPickBelongsToExactlyOneTeamAndEachTeamPicksOncePerRound() {
        List<Integer> allOveralls = IntStream.rangeClosed(1, 14)
                .boxed()
                .flatMap(team -> order.slotsForTeam(team).stream())
                .map(PickSlot::overall)
                .sorted()
                .toList();

        assertThat(allOveralls).containsExactlyElementsOf(IntStream.rangeClosed(1, 42).boxed().toList());
        IntStream.rangeClosed(1, 14).forEach(team ->
                assertThat(order.slotsForTeam(team)).extracting(PickSlot::round).containsExactly(1, 2, 3));
    }

    @Test
    void slotsForTeamAgreesWithSlotFor() {
        IntStream.rangeClosed(1, 14).forEach(team ->
                order.slotsForTeam(team).forEach(slot ->
                        assertThat(order.slotFor(slot.overall())).isEqualTo(slot)));
    }

    @Test
    void findsATeamsNextPick() {
        assertThat(order.nextPickForTeam(6, 5)).hasValue(24);
        assertThat(order.nextPickForTeam(24, 5)).hasValue(24);
        assertThat(order.nextPickForTeam(34, 5)).isEmpty();
    }

    @Test
    void rejectsPicksOutsideTheDraft() {
        assertThatThrownBy(() -> order.slotFor(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> order.slotFor(43)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> order.slotsForTeam(15)).isInstanceOf(IllegalArgumentException.class);
    }
}
