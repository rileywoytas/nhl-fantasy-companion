package com.rileywoytas.nhl_stats_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;


@Entity
@Data
@Table(
        name = "teams",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_team_nhl_id", columnNames = "nhl_id"),
                @UniqueConstraint(name = "uk_team_tri_code", columnNames = "tri_code")
        },
        indexes = {
                @Index(name = "idx_team_nhl_id", columnList = "nhl_id")
        }
)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nhl_id", nullable = false)
    private Integer nhlId;
    private String name;
    @Column(name = "tri_code",  nullable = false)
    private String triCode;

}
