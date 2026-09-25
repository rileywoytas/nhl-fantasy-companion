package com.rileywoytas.nhl_stats_api.ranking;

import com.rileywoytas.nhl_stats_api.ranking.importer.RankingRow;

import java.util.List;

public record RankingImportResult(String source, int totalRows, int matched, List<Issue> issues) {

    public record Issue(int row, String name, String team, String problem, List<String> candidates) {

        static Issue of(RankingRow row, String problem, List<String> candidates) {
            return new Issue(row.sourceRow(), row.name(), row.team(), problem, candidates);
        }
    }
}
