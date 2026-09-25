package com.rileywoytas.nhl_stats_api.ranking.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses a rankings/ADP CSV exported from a fantasy platform. Columns are found by
 * header name (case, spacing and punctuation ignored), so column order doesn't matter.
 */
public class RankingCsvParser {

    private static final List<String> NAME = List.of("player", "name", "playername", "fullname");
    private static final List<String> FIRST_NAME = List.of("firstname", "first");
    private static final List<String> LAST_NAME = List.of("lastname", "last");
    private static final List<String> TEAM = List.of("team", "tm", "nhlteam", "teamabbrev");
    private static final List<String> POSITION = List.of("pos", "position", "positions", "eligible", "eligiblepositions");
    private static final List<String> RANK = List.of("rank", "rk", "rkov", "ovr", "overall", "overallrank", "ecr");
    private static final List<String> ADP = List.of("adp", "avgpick", "averagepick", "avgdraftpos", "averagedraftposition");

    public List<RankingRow> parse(Reader reader) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true)
                .setAllowMissingColumnNames(true)
                .build();

        try (CSVParser parser = format.parse(reader)) {
            Map<String, String> headers = normalizedHeaders(parser.getHeaderNames());

            String nameColumn = find(headers, NAME);
            String firstColumn = find(headers, FIRST_NAME);
            String lastColumn = find(headers, LAST_NAME);
            if (nameColumn == null && (firstColumn == null || lastColumn == null)) {
                throw new IllegalArgumentException(
                        "CSV needs a player name column (or first + last name columns). Found: " + parser.getHeaderNames());
            }
            String teamColumn = find(headers, TEAM);
            String positionColumn = find(headers, POSITION);
            String rankColumn = find(headers, RANK);
            String adpColumn = find(headers, ADP);
            if (rankColumn == null && adpColumn == null) {
                throw new IllegalArgumentException(
                        "CSV needs a rank or ADP column. Found: " + parser.getHeaderNames());
            }

            List<RankingRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                String name = nameColumn != null
                        ? value(record, nameColumn)
                        : joinName(value(record, firstColumn), value(record, lastColumn));
                if (name == null) {
                    continue;
                }
                rows.add(new RankingRow(
                        (int) record.getRecordNumber() + 1,
                        name,
                        value(record, teamColumn),
                        value(record, positionColumn),
                        parseInteger(value(record, rankColumn)),
                        parseDouble(value(record, adpColumn))));
            }
            return rows;
        }
    }

    private static Map<String, String> normalizedHeaders(List<String> headerNames) {
        Map<String, String> normalized = new LinkedHashMap<>();
        for (String header : headerNames) {
            String key = header.replace("\uFEFF", "") // Excel's UTF-8 byte-order mark
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]", "");
            normalized.putIfAbsent(key, header);
        }
        return normalized;
    }

    private static String find(Map<String, String> headers, List<String> candidates) {
        return candidates.stream().filter(headers::containsKey).map(headers::get).findFirst().orElse(null);
    }

    private static String value(CSVRecord record, String column) {
        if (column == null || !record.isSet(column)) {
            return null;
        }
        String value = record.get(column).trim();
        return value.isEmpty() ? null : value;
    }

    private static String joinName(String first, String last) {
        if (first == null && last == null) {
            return null;
        }
        return ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
    }

    private static Integer parseInteger(String value) {
        Double parsed = parseDouble(value);
        return parsed == null ? null : (int) Math.round(parsed);
    }

    private static Double parseDouble(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null; // "-", "N/A", etc.
        }
    }
}
