package com.rileywoytas.nhl_stats_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BoxScoreDTO {

    private Long id;
    private Integer season;
    private Integer gameType;
    private String gameDate;
    private String gameState;
    //Won't object map
    private String gameEndType;

    private TeamDTO homeTeam;
    private TeamDTO awayTeam;

    private List<SkaterDTO>  skaters;
    private List<GoalieDTO> goalies;


//    private PeriodDescriptorDto periodDescriptor;

//    private JsonNode playerByGameStats;

}
