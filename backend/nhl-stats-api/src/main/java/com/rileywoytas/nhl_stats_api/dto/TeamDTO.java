package com.rileywoytas.nhl_stats_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TeamDTO {

    private Integer id;

    private Integer score;
    private Integer sog;

}
