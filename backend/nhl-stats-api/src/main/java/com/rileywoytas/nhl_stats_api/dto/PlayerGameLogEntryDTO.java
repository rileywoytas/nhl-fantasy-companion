package com.rileywoytas.nhl_stats_api.dto;

import lombok.Data;

@Data
public class PlayerGameLogEntryDTO {
    private String gameDate;
    private String opponent;
    private Boolean isHome;

    private Integer goals;
    private Integer assists;
    private Integer points;
    private Integer plusMinus;
    private Integer shots;
    private Integer hits;
    private Integer blocks;
    private Integer pim;
    private Integer timeOnIceSeconds;

    private Integer saves;
    private Integer shotsAgainst;
    private Integer goalsAgainst;
    private Double savePercentage;
    private Boolean starter;
}
