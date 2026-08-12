package com.rileywoytas.nhl_stats_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GoalieDTO {

    private Integer playerId;

    private Integer teamId;

    @JsonProperty("toi")
    private String timeOnIce;

    private Integer saves;
    private Integer shotsAgainst;
    private Integer evenStrengthGoalsAgainst;
    private Integer powerPlayGoalsAgainst;
    private Integer shorthandedGoalsAgainst;
    private Integer goalsAgainst;

    @JsonProperty("savePct")
    private Double savePercentage;

    private Boolean starter;

}
