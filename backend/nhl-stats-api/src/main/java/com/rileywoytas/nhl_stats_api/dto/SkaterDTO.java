package com.rileywoytas.nhl_stats_api.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SkaterDTO {

    private Integer playerId;
    private String position;

    private Integer teamId;

    private Integer goals;
    private Integer assists;
    private Integer points;
    private Integer plusMinus;
    private Integer pim;
    private Integer hits;

    @JsonProperty("sog")
    private Integer shots;

    @JsonProperty("blockedShots")
    private Integer blocks;

    @JsonProperty("toi")
    private String timeOnIce;

    private Integer shifts;
    private Integer giveaways;
    private Integer takeaways;
}
