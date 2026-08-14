import { useState } from 'react';
import { usePlayerGameLog } from '../hooks/usePlayerGameLog';
import { usePlayerPlayoffStats } from '../hooks/usePlayerPlayoffStats';
import { PosBadge } from './PosBadge';
import { normalizePosition } from '../utils/position';
import { calculateSkaterFantasyPoints, calculateGoalieFantasyPoints } from '../utils/fantasyPoints';

function formatDate(dateStr) {
  const d = new Date(dateStr);
  if (Number.isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

// Rounds to the nearest whole second first, then splits into minutes/seconds
// — splitting the unrounded value first could show e.g. "1:60" instead of
// rolling over to "2:00" when the fractional seconds rounded up to 60.
function formatToi(seconds) {
  if (seconds === null || seconds === undefined || Number.isNaN(seconds)) return '—';
  const total = Math.round(seconds);
  const m = Math.floor(total / 60);
  const s = total % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
}

function formatSavePct(v) {
  return v == null ? '—' : v.toFixed(3).replace(/^0/, '');
}

function formatWinPct(wins, starts) {
  if (!starts) return '—';
  return (wins / starts).toFixed(3).replace(/^0/, '');
}

function formatFpts(v) {
  return v.toFixed(1);
}

const SKATER_LOG_COLUMNS = [
  { key: 'goals', label: 'G' },
  { key: 'assists', label: 'A' },
  { key: 'points', label: 'PTS' },
  { key: 'plusMinus', label: '+/-' },
  { key: 'shots', label: 'SOG' },
  { key: 'hits', label: 'HIT' },
  { key: 'blocks', label: 'BLK' },
  { key: 'pim', label: 'PIM' },
];

const GOALIE_LOG_COLUMNS = [
  { key: 'saves', label: 'SV' },
  { key: 'shotsAgainst', label: 'SA' },
  { key: 'goalsAgainst', label: 'GA' },
  { key: 'savePercentage', label: 'SV%', format: formatSavePct },
  { key: 'starter', label: 'GS', format: (v) => (v ? 'Y' : 'N') },
];

function SnapshotStat({ label, value }) {
  return (
    <div className="flex flex-col items-center">
      <span className="font-mono text-[10px] text-muted">{label}</span>
      <span className="font-mono text-sm font-bold text-ink">{value ?? '—'}</span>
    </div>
  );
}

// Builds the ordered list of rank scopes to cycle through when the rank
// badge is clicked. Goalies get two (overall, goalies-only); skaters get
// three (overall, skaters-only, exact position).
function buildRankModes(rankInfo, isGoalie) {
  if (isGoalie) {
    return [
      { label: 'FPTS Rank', data: rankInfo?.overall },
      { label: 'FPTS Rank (Goalies)', data: rankInfo?.category },
    ];
  }
  return [
    { label: 'FPTS Rank', data: rankInfo?.overall },
    { label: 'FPTS Rank (Skaters)', data: rankInfo?.category },
    { label: `FPTS Rank (${rankInfo?.position?.label ?? ''})`, data: rankInfo?.position },
  ];
}

function SeasonSnapshot({ player, isGoalie, rankInfo }) {
  const rankModes = buildRankModes(rankInfo, isGoalie);
  const [rankModeIndex, setRankModeIndex] = useState(0);
  const currentRank = rankModes[rankModeIndex];

  const gpLabel = isGoalie ? `${player.starts ?? 0} GS` : `${player.gamesPlayed ?? 0} GP`;

  return (
    <div className="border-b border-rink-border px-5 py-3">
      <div className="mb-2 font-mono text-[10px] uppercase tracking-wider text-muted">
        Season Stats · {gpLabel}
      </div>
      <div className="flex flex-wrap items-center gap-4">
        {isGoalie ? (
          <>
            <SnapshotStat label="W" value={player.wins} />
            <SnapshotStat label="L" value={player.losses} />
            <SnapshotStat label="OTL" value={player.otLosses} />
            <SnapshotStat label="W%" value={formatWinPct(player.wins, player.starts)} />
            <SnapshotStat label="SV%" value={formatSavePct(player.savePercentage)} />
            <SnapshotStat label="SHO" value={player.shutouts} />
          </>
        ) : (
          <>
            <SnapshotStat label="G" value={player.goals} />
            <SnapshotStat label="A" value={player.assists} />
            <SnapshotStat label="PTS" value={player.points} />
            <SnapshotStat label="PPP" value={player.powerPlayPoints} />
            <SnapshotStat label="SHG" value={player.shorthandedGoals} />
            <SnapshotStat label="GWG" value={player.gameWinningGoals} />
            <SnapshotStat
              label="TOI/G"
              value={player.gamesPlayed ? formatToi(player.timeOnIceSeconds / player.gamesPlayed) : '—'}
            />
          </>
        )}
        <div className="ml-auto flex items-center gap-2">
          {currentRank.data && (
            <button
              onClick={() => setRankModeIndex((i) => (i + 1) % rankModes.length)}
              className="flex flex-col items-center justify-center rounded border border-amber/40 bg-panel-alt px-3 py-1 transition-colors hover:border-amber hover:bg-rink-border/50"
              title="Click to change rank scope"
            >
              <span className="font-mono text-[10px] text-muted">{currentRank.label}</span>
              <span className="font-mono text-base font-bold text-amber-light">{currentRank.data.rank}</span>
            </button>
          )}
          <div className="flex flex-col items-center justify-center rounded bg-panel-alt px-3 py-1">
            <span className="font-mono text-[10px] text-muted">Season FPTS</span>
            <span className="font-mono text-base font-bold text-amber-light">
              {player.fantasyPoints !== undefined ? formatFpts(player.fantasyPoints) : '—'}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

function PlayoffSnapshot({ stats, status, isGoalie }) {
  if (status === 'loading') {
    return (
      <div className="border-b border-rink-border px-5 py-3">
        <div className="font-mono text-[10px] uppercase tracking-wider text-muted">Playoff Stats</div>
        <p className="mt-1 font-mono text-xs text-muted">Loading…</p>
      </div>
    );
  }

  if (status === 'none' || !stats) {
    return (
      <div className="border-b border-rink-border px-5 py-3">
        <div className="font-mono text-[10px] uppercase tracking-wider text-muted">Playoff Stats</div>
        <p className="mt-1 font-mono text-xs text-muted">No playoff games this season.</p>
      </div>
    );
  }

  const gpLabel = isGoalie ? `${stats.starts ?? 0} GS` : `${stats.gamesPlayed ?? 0} GP`;

  return (
    <div className="border-b border-rink-border px-5 py-3">
      <div className="mb-2 font-mono text-[10px] uppercase tracking-wider text-muted">
        Playoff Stats · {gpLabel}
      </div>
      <div className="flex flex-wrap items-center gap-4">
        {isGoalie ? (
          <>
            <SnapshotStat label="W" value={stats.wins} />
            <SnapshotStat label="L" value={stats.losses} />
            <SnapshotStat label="W%" value={formatWinPct(stats.wins, stats.starts)} />
            <SnapshotStat label="SV%" value={formatSavePct(stats.savePercentage)} />
            <SnapshotStat label="SHO" value={stats.shutouts} />
          </>
        ) : (
          <>
            <SnapshotStat label="G" value={stats.goals} />
            <SnapshotStat label="A" value={stats.assists} />
            <SnapshotStat label="PTS" value={stats.points} />
            <SnapshotStat label="PPP" value={stats.powerPlayPoints} />
            <SnapshotStat label="SHG" value={stats.shorthandedGoals} />
            <SnapshotStat label="GWG" value={stats.gameWinningGoals} />
            <SnapshotStat
              label="TOI/G"
              value={stats.gamesPlayed ? formatToi(stats.timeOnIceSeconds / stats.gamesPlayed) : '—'}
            />
          </>
        )}
      </div>
    </div>
  );
}

export function PlayerDetailModal({ player, season, rankInfo, onClose }) {
  const isGoalie = normalizePosition(player.position) === 'G';
  const { games, status, error } = usePlayerGameLog(player.playerId, season);
  const { stats: playoffStats, status: playoffStatus } = usePlayerPlayoffStats(player.playerId, season);
  const columns = isGoalie ? GOALIE_LOG_COLUMNS : SKATER_LOG_COLUMNS;

  return (
    <div
      className="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-graphite/80 px-4 py-8"
      onClick={onClose}
    >
      <div
        className="w-full max-w-4xl rounded-md border border-rink-border bg-panel"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between border-b border-rink-border px-6 py-6">
          <div className="flex items-center gap-5">
            {player.headshot && (
              <img
                src={player.headshot}
                alt=""
                className="h-28 w-28 rounded-full border-2 border-rink-border bg-panel-alt object-cover"
                onError={(e) => {
                  e.currentTarget.style.display = 'none';
                }}
              />
            )}
            <div>
              <div className="flex items-center gap-3">
                <h2 className="font-mono text-3xl font-bold text-ink">
                  {player.firstName} {player.lastName}
                </h2>
                {player.teamLogo && (
                  <img
                    src={player.teamLogo}
                    alt={player.teamTriCode ?? ''}
                    className="h-20 w-20 object-contain"
                  />
                )}
              </div>
              <div className="mt-1 flex items-center gap-2">
                <PosBadge position={player.position} />
                <span className="font-mono text-sm text-muted">{player.teamTriCode ?? '—'}</span>
              </div>
            </div>
          </div>
          <button
            onClick={onClose}
            className="font-mono text-lg text-muted hover:text-ink"
            aria-label="Close"
          >
            ✕
          </button>
        </div>

        <SeasonSnapshot player={player} isGoalie={isGoalie} rankInfo={rankInfo} />
        <PlayoffSnapshot stats={playoffStats} status={playoffStatus} isGoalie={isGoalie} />

        <div className="max-h-[60vh] overflow-y-auto px-5 py-4 themed-scrollbar">
          {status === 'loading' && (
            <p className="py-8 text-center font-mono text-sm text-muted">Loading game log…</p>
          )}
          {status === 'error' && (
            <p className="py-8 text-center font-mono text-sm text-flag-red">
              Couldn't load game log: {error}
            </p>
          )}
          {status === 'success' && games.length === 0 && (
            <p className="py-8 text-center font-mono text-sm text-muted">No games found for this season.</p>
          )}
          {status === 'success' && games.length > 0 && (
            <>
              <table className="w-full font-mono text-sm">
                <thead>
                  <tr className="border-b border-rink-border">
                    <th className="px-2 py-1.5 text-left text-xs font-bold text-muted">Date</th>
                    <th className="px-2 py-1.5 text-left text-xs font-bold text-muted">Opp</th>
                    <th className="px-2 py-1.5 text-xs font-bold text-muted"></th>
                    {columns.map((c) => (
                      <th key={c.key} className="px-2 py-1.5 text-right text-xs font-bold text-muted">
                        {c.label}
                      </th>
                    ))}
                    <th className="px-2 py-1.5 text-right text-xs font-bold text-muted">TOI</th>
                    <th className="px-2 py-1.5 text-right text-xs font-bold text-amber-light">FPTS</th>
                  </tr>
                </thead>
                <tbody>
                  {games.map((g, i) => {
                    const gameFpts = isGoalie ? calculateGoalieFantasyPoints(g) : calculateSkaterFantasyPoints(g);
                    return (
                      <tr
                        key={i}
                        className={`border-t border-rink-border ${i % 2 === 0 ? 'bg-graphite' : 'bg-panel-alt'}`}
                      >
                        <td className="px-2 py-1.5 text-left text-muted">{formatDate(g.gameDate)}</td>
                        <td className="px-2 py-1.5 text-left text-ink">
                          {g.isHome ? 'vs' : '@'} {g.opponent}
                        </td>
                        <td className="px-2 py-1.5 text-center">
                          {g.goalHighlightUrl && (
                            <a
                              href={g.goalHighlightUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              onClick={(e) => e.stopPropagation()}
                              title="Watch goal highlight"
                              className="text-amber hover:text-amber-light"
                            >
                              ▶
                            </a>
                          )}
                        </td>
                        {columns.map((c) => {
                          const value = g[c.key];
                          return (
                            <td key={c.key} className="px-2 py-1.5 text-right text-ink">
                              {value === null || value === undefined ? '—' : c.format ? c.format(value) : value}
                            </td>
                          );
                        })}
                        <td className="px-2 py-1.5 text-right text-muted">{formatToi(g.timeOnIceSeconds)}</td>
                        <td className="px-2 py-1.5 text-right font-bold text-amber-light">
                          {formatFpts(gameFpts)}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
              <p className="mt-2 text-[11px] italic text-muted">
                {isGoalie
                  ? 'Per-game FPTS excludes W/L/SHO — goalie decisions aren\'t tracked per game, only as season totals. See Season FPTS above for the full total.'
                  : 'Per-game FPTS includes real PPP/SHG/GWG once "Import Per-Game Scoring Details" has been run for this season (Data tab). Until then, those categories show as 0 for games not yet processed — Season FPTS above is unaffected either way.'}
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
