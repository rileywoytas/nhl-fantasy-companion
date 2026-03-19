package com.rileywoytas.nhl_stats_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "games",
        uniqueConstraints = @UniqueConstraint(name = "uk_game_nhl_id", columnNames = "nhl_id"),
        indexes = @Index(name = "idx_game_nhl_id", columnList = "nhl_id")
)
@Data
public class Game {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "nhl_id", nullable = false)
    private Long nhlId;

    private OffsetDateTime gameDate;

    private String season;

    @ManyToOne(optional = false)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    private Integer homeScore;
    private Integer awayScore;

    private Integer homeShots;
    private Integer awayShots;

    private String gameState;
    private String gameEndType;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false)
    private GameType gameType;
}
