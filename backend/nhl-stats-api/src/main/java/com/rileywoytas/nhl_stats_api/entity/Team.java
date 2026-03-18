package com.rileywoytas.nhl_stats_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;


@Entity
@Data
@Table(
        name = "teams",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_team_nhl_id", columnNames = "nhl_id")
        },
        indexes = {
                @Index(name = "idx_team_nhl_id", columnList = "nhl_id")
        }
)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private Integer nhlId;
    private String name;
    private String triCode;

}
