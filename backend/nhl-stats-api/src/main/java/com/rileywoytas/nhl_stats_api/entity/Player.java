package com.rileywoytas.nhl_stats_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(
        name = "players",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_player_nhl_id", columnNames = "nhl_id")
        }
)
@Data
public class Player {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "nhl_id", nullable = false)
    private Integer nhlId;

    private String firstName;
    private String lastName;
    private int sweaterNumber;
    private String headshot;
    private String teamLogo;
    private String position;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

//    @OneToMany(mappedBy = "player")
//    private List<PlayerGameStats> gameStats;
}
