package com.rileywoytas.nhl_stats_api.ranking.matching;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerMatcherTest {

    private static final PlayerIdentity AHO_CAR = new PlayerIdentity(new UUID(0, 1), "Sebastian Aho", "CAR", "C");
    private static final PlayerIdentity AHO_NYI = new PlayerIdentity(new UUID(0, 2), "Sebastian Aho", "NYI", "D");
    private static final PlayerIdentity PETTERSSON_F = new PlayerIdentity(new UUID(0, 3), "Elias Pettersson", "VAN", "C");
    private static final PlayerIdentity PETTERSSON_D = new PlayerIdentity(new UUID(0, 4), "Elias Pettersson", "VAN", "D");
    private static final PlayerIdentity STUTZLE = new PlayerIdentity(new UUID(0, 5), "Tim Stützle", "OTT", "C");
    private static final PlayerIdentity MARNER = new PlayerIdentity(new UUID(0, 6), "Mitchell Marner", "VGK", "R");
    private static final PlayerIdentity PAUL = new PlayerIdentity(new UUID(0, 7), "Nicholas Paul", "TBL", "C");
    private static final PlayerIdentity B_TKACHUK = new PlayerIdentity(new UUID(0, 8), "Brady Tkachuk", "OTT", "L");

    private final PlayerMatcher matcher = new PlayerMatcher(List.of(
            AHO_CAR, AHO_NYI, PETTERSSON_F, PETTERSSON_D, STUTZLE, MARNER, PAUL, B_TKACHUK));

    @Test
    void matchesRegardlessOfAccents() {
        assertThat(matcher.match("Tim Stutzle", "OTT", "C").player()).isEqualTo(STUTZLE);
    }

    @Test
    void separatesNamesakesByTeam() {
        assertThat(matcher.match("Sebastian Aho", "NYI", "D").player()).isEqualTo(AHO_NYI);
        assertThat(matcher.match("Sebastian Aho", "CAR", "C,LW").player()).isEqualTo(AHO_CAR);
    }

    @Test
    void separatesNamesakesOnTheSameTeamByPosition() {
        assertThat(matcher.match("Elias Pettersson", "VAN", "D").player()).isEqualTo(PETTERSSON_D);
        assertThat(matcher.match("Elias Pettersson", "VAN", "C,LW").player()).isEqualTo(PETTERSSON_F);
    }

    @Test
    void reportsNamesakesItCannotSeparate() {
        MatchResult result = matcher.match("Elias Pettersson", null, null);

        assertThat(result.status()).isEqualTo(MatchStatus.AMBIGUOUS);
        assertThat(result.candidates()).containsExactlyInAnyOrder(PETTERSSON_F, PETTERSSON_D);
    }

    @Test
    void handlesNicknamesViaSurnameAndTeam() {
        assertThat(matcher.match("Mitch Marner", "VGK", "RW").player()).isEqualTo(MARNER);
    }

    @Test
    void understandsFantasyPlatformTeamAbbreviations() {
        assertThat(matcher.match("Nick Paul", "TB", "C").player()).isEqualTo(PAUL);
    }

    @Test
    void surnameFallbackRequiresTheSameFirstInitial() {
        assertThat(matcher.match("Matthew Tkachuk", "OTT", "LW").status()).isEqualTo(MatchStatus.NOT_FOUND);
    }

    @Test
    void reportsUnknownPlayers() {
        assertThat(matcher.match("Wayne Gretzky", "EDM", "C").status()).isEqualTo(MatchStatus.NOT_FOUND);
    }
}
