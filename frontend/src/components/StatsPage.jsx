import { useEffect, useMemo, useState } from 'react';
import { SearchBar } from './SearchBar';
import { PositionFilter } from './PositionFilter';
import { StatsTable } from './StatsTable';
import { PlayerDetailModal } from './PlayerDetailModal';
import { usePlayerStats } from '../hooks/usePlayerStats';
import { useSeasons } from '../hooks/useSeasons';
import { normalizePosition } from '../utils/position';
import { calculateFantasyPoints } from '../utils/fantasyPoints';
import { formatSeasonLabel } from '../utils/season';
import { SKATER_COLUMNS, GOALIE_COLUMNS, ALL_COLUMNS } from '../config/statColumns';

const FALLBACK_SEASON = '20252026'; // used only if the seasons list hasn't loaded yet

export function StatsPage() {
  const { seasons, status: seasonsStatus } = useSeasons();
  const [season, setSeason] = useState(FALLBACK_SEASON);
  const [searchTerm, setSearchTerm] = useState('');
  const [positionFilter, setPositionFilter] = useState('ALL');
  const [selectedPlayer, setSelectedPlayer] = useState(null);

  // Once the real seasons list loads, default to the most recent one
  // instead of the hardcoded fallback.
  useEffect(() => {
    if (seasonsStatus === 'success' && seasons.length > 0) {
      setSeason(seasons[0]);
    }
  }, [seasonsStatus, seasons]);

  const { players, status, error } = usePlayerStats(season);

  const filteredPlayers = useMemo(() => {
    return players
      .filter((p) => {
        const matchesSearch =
          !searchTerm ||
          `${p.firstName} ${p.lastName}`.toLowerCase().includes(searchTerm.toLowerCase());

        const pos = normalizePosition(p.position);
        let matchesFilter;
        if (positionFilter === 'ALL') matchesFilter = true;
        else if (positionFilter === 'SKATERS') matchesFilter = pos !== 'G';
        else matchesFilter = pos === positionFilter;

        return matchesSearch && matchesFilter;
      })
      .map((p) => ({ ...p, fantasyPoints: calculateFantasyPoints(p) }));
  }, [players, searchTerm, positionFilter]);

  const columns =
    positionFilter === 'G' ? GOALIE_COLUMNS : positionFilter === 'ALL' ? ALL_COLUMNS : SKATER_COLUMNS;
  const defaultSortKey = 'fantasyPoints';

  return (
    <main className="mx-auto max-w-6xl px-6 py-6">
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <SearchBar value={searchTerm} onChange={setSearchTerm} />
          <select
            value={season}
            onChange={(e) => setSeason(e.target.value)}
            className="rounded-md border border-rink-border bg-panel px-2 py-2 font-mono text-xs text-ink focus:border-amber focus:outline-none"
            aria-label="Season"
          >
            {seasons.length === 0 ? (
              <option value={FALLBACK_SEASON}>{formatSeasonLabel(FALLBACK_SEASON)}</option>
            ) : (
              seasons.map((s) => (
                <option key={s} value={s}>
                  {formatSeasonLabel(s)}
                </option>
              ))
            )}
          </select>
        </div>
        <PositionFilter value={positionFilter} onChange={setPositionFilter} />
      </div>

      {status === 'loading' && (
        <p className="py-8 text-center font-mono text-sm text-muted">Loading stats…</p>
      )}

      {status === 'error' && (
        <p className="py-8 text-center font-mono text-sm text-flag-red">
          Couldn't load player stats: {error}
        </p>
      )}

      {status === 'success' && (
        <StatsTable
          players={filteredPlayers}
          columns={columns}
          defaultSortKey={defaultSortKey}
          onRowClick={setSelectedPlayer}
        />
      )}

      {selectedPlayer && (
        <PlayerDetailModal player={selectedPlayer} season={season} onClose={() => setSelectedPlayer(null)} />
      )}
    </main>
  );
}
