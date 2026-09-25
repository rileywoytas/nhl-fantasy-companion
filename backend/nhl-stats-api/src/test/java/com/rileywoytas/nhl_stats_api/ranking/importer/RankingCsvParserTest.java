package com.rileywoytas.nhl_stats_api.ranking.importer;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RankingCsvParserTest {

    private final RankingCsvParser parser = new RankingCsvParser();

    @Test
    void readsATypicalExport() throws IOException {
        String csv = """
                \uFEFFPlayer,Team,Position,RkOv,ADP
                Connor McDavid,EDM,C,1,1.2
                "Draisaitl, Leon",EDM,"C,LW",2,-
                ,,,,
                Cale Makar,COL,D,3,3.4
                """;

        List<RankingRow> rows = parser.parse(new StringReader(csv));

        assertThat(rows).extracting(RankingRow::name)
                .containsExactly("Connor McDavid", "Draisaitl, Leon", "Cale Makar");
        assertThat(rows.get(0)).isEqualTo(new RankingRow(2, "Connor McDavid", "EDM", "C", 1, 1.2));
        assertThat(rows.get(1).positions()).isEqualTo("C,LW");
        assertThat(rows.get(1).adp()).isNull();
        assertThat(rows.get(1).draftValue()).isEqualTo(2.0);
        assertThat(rows.get(2).sourceRow()).isEqualTo(5);
    }

    @Test
    void joinsSeparateFirstAndLastNameColumns() throws IOException {
        String csv = """
                First Name,Last Name,Avg. Pick
                Connor,McDavid,1.1
                """;

        assertThat(parser.parse(new StringReader(csv)).get(0).name()).isEqualTo("Connor McDavid");
    }

    @Test
    void rejectsAFileWithNoRankOrAdp() {
        String csv = """
                Name,Team
                Connor McDavid,EDM
                """;

        assertThatThrownBy(() -> parser.parse(new StringReader(csv)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rank or ADP");
    }
}
