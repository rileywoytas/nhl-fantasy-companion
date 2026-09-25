package com.rileywoytas.nhl_stats_api.ranking.matching;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Normalizes player names so "Tim Stützle", "Tim Stutzle" and "Stutzle, Tim"
 * all compare equal.
 */
public final class NameNormalizer {

    private NameNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String name = raw.trim();

        // "Last, First" -> "First Last"
        int comma = name.indexOf(',');
        if (comma > 0) {
            name = name.substring(comma + 1).trim() + " " + name.substring(0, comma).trim();
        }

        // Letters NFD can't decompose into base + accent
        name = name.replace("ø", "o").replace("Ø", "O")
                .replace("æ", "ae").replace("Æ", "Ae")
                .replace("ß", "ss")
                .replace("ł", "l").replace("Ł", "L")
                .replace("đ", "d").replace("Đ", "D");

        name = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        name = name.toLowerCase(Locale.ROOT);
        name = name.replaceAll("[.'’`]", "");          // J.T. -> jt, O'Reilly -> oreilly
        name = name.replaceAll("[^a-z0-9]+", " ").trim(); // hyphens and anything else -> space
        name = name.replaceAll("\\s+(jr|sr|ii|iii|iv)$", "");
        return name;
    }

    /** Last token of an already-normalized name. */
    public static String lastToken(String normalized) {
        int space = normalized.lastIndexOf(' ');
        return space < 0 ? normalized : normalized.substring(space + 1);
    }

    /** First character of an already-normalized name, or 0 if empty. */
    public static char firstInitial(String normalized) {
        return normalized.isEmpty() ? 0 : normalized.charAt(0);
    }
}
