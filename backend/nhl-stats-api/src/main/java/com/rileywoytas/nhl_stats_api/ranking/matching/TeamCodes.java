package com.rileywoytas.nhl_stats_api.ranking.matching;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Maps the abbreviations fantasy platforms use onto the NHL API's tri-codes. */
public final class TeamCodes {

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("TB", "TBL"), Map.entry("TAM", "TBL"),
            Map.entry("NJ", "NJD"), Map.entry("NJE", "NJD"),
            Map.entry("LA", "LAK"), Map.entry("LOS", "LAK"),
            Map.entry("SJ", "SJS"), Map.entry("SAN", "SJS"),
            Map.entry("MON", "MTL"), Map.entry("MTR", "MTL"),
            Map.entry("WAS", "WSH"),
            Map.entry("CLS", "CBJ"), Map.entry("CLB", "CBJ"),
            Map.entry("NAS", "NSH"),
            Map.entry("CAL", "CGY"),
            Map.entry("WIN", "WPG"),
            Map.entry("VEG", "VGK"), Map.entry("LV", "VGK"), Map.entry("LVK", "VGK"),
            Map.entry("UTAH", "UTA"), Map.entry("UHC", "UTA"),
            Map.entry("PHO", "ARI"), Map.entry("PHX", "ARI")
    );

    private static final Set<String> NO_TEAM = Set.of("FA", "UFA", "RFA", "N/A", "NA", "-", "--");

    private TeamCodes() {
    }

    /** Canonical NHL tri-code, or null if the input is blank or means "no team". */
    public static String canonical(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String code = raw.trim().toUpperCase(Locale.ROOT);
        if (NO_TEAM.contains(code)) {
            return null;
        }
        return ALIASES.getOrDefault(code, code);
    }
}
