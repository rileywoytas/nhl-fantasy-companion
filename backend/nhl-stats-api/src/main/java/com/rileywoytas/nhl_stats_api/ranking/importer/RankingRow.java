package com.rileywoytas.nhl_stats_api.ranking.importer;

/**
 * One parsed CSV row, exactly as the fantasy platform wrote it.
 *
 * @param sourceRow spreadsheet row number (header is row 1), for pointing back at the file
 */
public record RankingRow(int sourceRow, String name, String team, String positions, Integer rank, Double adp) {

    /** What CPU drafters sort by: ADP when present, otherwise overall rank. Lower is better. */
    public Double draftValue() {
        if (adp != null) {
            return adp;
        }
        return rank == null ? null : rank.doubleValue();
    }
}
