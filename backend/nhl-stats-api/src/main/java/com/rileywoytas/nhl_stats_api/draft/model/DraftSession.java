package com.rileywoytas.nhl_stats_api.draft.model;

import com.rileywoytas.nhl_stats_api.draft.order.PickSlot;
import com.rileywoytas.nhl_stats_api.draft.order.SnakeOrder;
import com.rileywoytas.nhl_stats_api.entity.Player;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "draft_session")
public class DraftSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DraftMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DraftStatus status;

    @Column(nullable = false)
    private int teamCount;

    @Column(nullable = false)
    private int rounds;

    @Column(nullable = false)
    private int userTeamSlot;

    /** The overall pick number that will be made next (1-based). */
    @Column(nullable = false)
    private int nextOverallPick;

    /** Which imported ranking set CPU drafters use; matches {@code PlayerRanking.source}. */
    @Column(length = 40)
    private String rankingSource;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("overallPick ASC")
    private List<DraftPick> picks = new ArrayList<>();

    protected DraftSession() {
        // for JPA
    }

    public DraftSession(DraftMode mode, int teamCount, int rounds, int userTeamSlot, String rankingSource) {
        this.mode = Objects.requireNonNull(mode, "mode");
        new SnakeOrder(teamCount, rounds); // validates teamCount and rounds
        if (userTeamSlot < 1 || userTeamSlot > teamCount) {
            throw new IllegalArgumentException(
                    "userTeamSlot must be between 1 and " + teamCount + ", was " + userTeamSlot);
        }
        this.teamCount = teamCount;
        this.rounds = rounds;
        this.userTeamSlot = userTeamSlot;
        this.rankingSource = rankingSource;
        this.status = DraftStatus.IN_PROGRESS;
        this.nextOverallPick = 1;
        this.createdAt = Instant.now();
    }

    public SnakeOrder order() {
        return new SnakeOrder(teamCount, rounds);
    }

    public PickSlot currentSlot() {
        if (isComplete()) {
            throw new IllegalStateException("Draft " + id + " is complete");
        }
        return order().slotFor(nextOverallPick);
    }

    public boolean isComplete() {
        return status == DraftStatus.COMPLETE;
    }

    public boolean isUsersTurn() {
        return !isComplete() && currentSlot().teamSlot() == userTeamSlot;
    }

    public boolean hasBeenDrafted(Player player) {
        return picks.stream().anyMatch(pick -> pick.getPlayer().getId().equals(player.getId()));
    }

    /** Records the pick for whichever team is currently on the clock and advances the draft. */
    public DraftPick recordPick(Player player, boolean cpuPick) {
        Objects.requireNonNull(player, "player");
        PickSlot slot = currentSlot();
        if (hasBeenDrafted(player)) {
            throw new IllegalStateException("Player " + player.getId() + " has already been drafted");
        }
        DraftPick pick = new DraftPick(this, slot, player, cpuPick);
        picks.add(pick);
        nextOverallPick++;
        if (nextOverallPick > order().totalPicks()) {
            status = DraftStatus.COMPLETE;
        }
        return pick;
    }

    /** Removes the most recent pick and puts that slot back on the clock. */
    public DraftPick undoLastPick() {
        if (picks.isEmpty()) {
            throw new IllegalStateException("Draft " + id + " has no picks to undo");
        }
        DraftPick last = picks.remove(picks.size() - 1);
        nextOverallPick = last.getOverallPick();
        status = DraftStatus.IN_PROGRESS;
        return last;
    }

    public List<DraftPick> picksForTeam(int teamSlot) {
        return picks.stream().filter(pick -> pick.getTeamSlot() == teamSlot).toList();
    }

    public Long getId() {
        return id;
    }

    public DraftMode getMode() {
        return mode;
    }

    public DraftStatus getStatus() {
        return status;
    }

    public int getTeamCount() {
        return teamCount;
    }

    public int getRounds() {
        return rounds;
    }

    public int getUserTeamSlot() {
        return userTeamSlot;
    }

    public int getNextOverallPick() {
        return nextOverallPick;
    }

    public String getRankingSource() {
        return rankingSource;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<DraftPick> getPicks() {
        return Collections.unmodifiableList(picks);
    }
}
