package com.rileywoytas.nhl_stats_api.ranking;

import com.rileywoytas.nhl_stats_api.entity.Player;
import com.rileywoytas.nhl_stats_api.ranking.importer.RankingCsvParser;
import com.rileywoytas.nhl_stats_api.ranking.importer.RankingRow;
import com.rileywoytas.nhl_stats_api.ranking.matching.MatchResult;
import com.rileywoytas.nhl_stats_api.ranking.matching.MatchStatus;
import com.rileywoytas.nhl_stats_api.ranking.matching.PlayerIdentity;
import com.rileywoytas.nhl_stats_api.ranking.matching.PlayerMatcher;
import com.rileywoytas.nhl_stats_api.ranking.model.PlayerRanking;
import com.rileywoytas.nhl_stats_api.ranking.repository.PlayerRankingRepository;
import com.rileywoytas.nhl_stats_api.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RankingImportService {

    private static final Pattern SOURCE_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_-]{0,39}");

    private final PlayerRepository playerRepository;
    private final PlayerRankingRepository rankingRepository;
    private final RankingCsvParser parser = new RankingCsvParser();

    public RankingImportService(PlayerRepository playerRepository, PlayerRankingRepository rankingRepository) {
        this.playerRepository = playerRepository;
        this.rankingRepository = rankingRepository;
    }

    /** Replaces every ranking stored under {@code source} with the contents of the CSV. */
    @Transactional
    public RankingImportResult importCsv(String source, Reader csv) throws IOException {
        String normalizedSource = source == null ? "" : source.trim().toLowerCase(Locale.ROOT);
        if (!SOURCE_PATTERN.matcher(normalizedSource).matches()) {
            throw new IllegalArgumentException(
                    "source must be 1-40 characters of a-z, 0-9, '-' or '_', was '" + source + "'");
        }

        List<RankingRow> rows = parser.parse(csv);

        Map<UUID, Player> playersById = playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getId, Function.identity()));
        PlayerMatcher matcher = new PlayerMatcher(
                playersById.values().stream().map(PlayerIdentities::from).toList());

        rankingRepository.deleteBySource(normalizedSource);

        Instant importedAt = Instant.now();
        Set<UUID> alreadyRanked = new HashSet<>();
        List<PlayerRanking> rankings = new ArrayList<>(rows.size());
        List<RankingImportResult.Issue> issues = new ArrayList<>();
        int matched = 0;

        for (RankingRow row : rows) {
            if (row.draftValue() == null) {
                issues.add(RankingImportResult.Issue.of(row, "No rank or ADP value; skipped", List.of()));
                continue;
            }

            MatchResult result = matcher.match(row.name(), row.team(), row.positions());
            Player player = null;

            if (result.isMatched()) {
                UUID playerId = result.player().id();
                if (!alreadyRanked.add(playerId)) {
                    issues.add(RankingImportResult.Issue.of(row,
                            "Matched a player already ranked on an earlier row; skipped",
                            List.of(result.player().describe())));
                    continue;
                }
                player = playersById.get(playerId);
                matched++;
            } else {
                String problem = result.status() == MatchStatus.AMBIGUOUS
                        ? "Several players match; couldn't tell which"
                        : "No matching player found";
                issues.add(RankingImportResult.Issue.of(row, problem,
                        result.candidates().stream().map(PlayerIdentity::describe).toList()));
            }

            rankings.add(new PlayerRanking(normalizedSource, row, player, result.status(), importedAt));
        }

        rankingRepository.saveAll(rankings);
        return new RankingImportResult(normalizedSource, rows.size(), matched, issues);
    }
}
