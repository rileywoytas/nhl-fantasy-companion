package com.rileywoytas.nhl_stats_api.draft.model;

import com.rileywoytas.nhl_stats_api.draft.order.PickSlot;
import com.rileywoytas.nhl_stats_api.entity.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "draft_pick", uniqueConstraints = {
        @UniqueConstraint(name = "uk_draft_pick_overall", columnNames = {"session_id", "overall_pick"}),
        @UniqueConstraint(name = "uk_draft_pick_player", columnNames = {"session_id", "player_id"})
})
public class DraftPick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private DraftSession session;

    @Column(name = "overall_pick", nullable = false)
    private int overallPick;

    @Column(nullable = false)
    private int round;

    @Column(nullable = false)
    private int teamSlot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private boolean cpuPick;

    @Column(nullable = false)
    private Instant madeAt;

    protected DraftPick() {
        // for JPA
    }

    DraftPick(DraftSession session, PickSlot slot, Player player, boolean cpuPick) {
        this.session = session;
        this.overallPick = slot.overall();
        this.round = slot.round();
        this.teamSlot = slot.teamSlot();
        this.player = player;
        this.cpuPick = cpuPick;
        this.madeAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public DraftSession getSession() {
        return session;
    }

    public int getOverallPick() {
        return overallPick;
    }

    public int getRound() {
        return round;
    }

    public int getTeamSlot() {
        return teamSlot;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isCpuPick() {
        return cpuPick;
    }

    public Instant getMadeAt() {
        return madeAt;
    }
}
