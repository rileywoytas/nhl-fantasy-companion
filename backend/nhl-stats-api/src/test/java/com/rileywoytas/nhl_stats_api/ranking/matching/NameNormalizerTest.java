package com.rileywoytas.nhl_stats_api.ranking.matching;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NameNormalizerTest {

    @Test
    void stripsAccents() {
        assertThat(NameNormalizer.normalize("Tim Stützle")).isEqualTo("tim stutzle");
        assertThat(NameNormalizer.normalize("Marc-André Fleury")).isEqualTo("marc andre fleury");
        assertThat(NameNormalizer.normalize("Søren")).isEqualTo("soren");
    }

    @Test
    void ignoresPeriodsAndApostrophes() {
        assertThat(NameNormalizer.normalize("J.T. Miller")).isEqualTo(NameNormalizer.normalize("JT Miller"));
        assertThat(NameNormalizer.normalize("Ryan O'Reilly")).isEqualTo("ryan oreilly");
    }

    @Test
    void flipsLastCommaFirst() {
        assertThat(NameNormalizer.normalize("Draisaitl, Leon")).isEqualTo("leon draisaitl");
    }

    @Test
    void dropsGenerationalSuffixes() {
        assertThat(NameNormalizer.normalize("Paul Smith Jr.")).isEqualTo("paul smith");
    }

    @Test
    void treatsBlankAsEmpty() {
        assertThat(NameNormalizer.normalize(null)).isEmpty();
        assertThat(NameNormalizer.normalize("   ")).isEmpty();
    }
}
