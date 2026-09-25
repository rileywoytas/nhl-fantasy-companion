package com.rileywoytas.nhl_stats_api.ranking.model;

import com.rileywoytas.nhl_stats_api.entity.Player;
import com.rileywoytas.nhl_stats_api.ranking.importer.RankingRow;
import com.rileywoytas.nhl_stats_api.ranking.matching.MatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * One row of an imported rankings/ADP file. Unmatched rows are kept (player = null)
 * so they can be reviewed and fixed later instead of silently disappearing.
 */
@Entity
@Table(name = "player_ranking",
        indexes = @Index(name = "idx_player_ranking_source", columnList = "source"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_player_ranking_source_player", columnNames = {"source", "player_id"}))
public class PlayerRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String source;

    @Column(nullable = false)
    private int sourceRow;

    @Column(nullable = false, length = 120)
    private String rawName;

    @Column(length = 10)
    private String rawTeam;

    @Column(length = 20)
    private String rawPositions;

    @Column(name = "source_rank")
    private Integer rank;

    private Double adp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MatchStatus matchStatus;

    @Column(nullable = false)
    private Instant importedAt;

    protected PlayerRanking() {
        // for JPA
    }

    public PlayerRanking(String source, RankingRow row, Player player, MatchStatus matchStatus, Instant importedAt) {
        this.source = source;
        this.sourceRow = row.sourceRow();
        this.rawName = row.name();
        this.rawTeam = row.team();
        this.rawPositions = row.positions();
        this.rank = row.rank();
        this.adp = row.adp();
        this.player = player;
        this.matchStatus = matchStatus;
        this.importedAt = importedAt;
    }

    /** ADP when present, otherwise overall rank. Lower is better. */
    public Double draftValue() {
        if (adp != null) {
            return adp;
        }
        return rank == null ? null : rank.doubleValue();
    }

    public Long getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public int getSourceRow() {
        return sourceRow;
    }

    public String getRawName() {
        return rawName;
    }

    public String getRawTeam() {
        return rawTeam;
    }

    public String getRawPositions() {
        return rawPositions;
    }

    public Integer getRank() {
        return rank;
    }

    public Double getAdp() {
        return adp;
    }

    public Player getPlayer() {
        return player;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public Instant getImportedAt() {
        return importedAt;
    }
}
