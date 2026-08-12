import { useMemo, useState } from 'react';
import { Header } from './components/Header';
import { SearchBar } from './components/SearchBar';
import { PositionFilter } from './components/PositionFilter';
import { StatsTable } from './components/StatsTable';
import { usePlayerStats } from './hooks/usePlayerStats';
import { normalizePosition } from './utils/position';
import { SKATER_COLUMNS, GOALIE_COLUMNS, ALL_COLUMNS } from './config/statColumns';

const DEFAULT_SEASON = '20252026'; // adjust to match your `games.season` format

function App() {
  const [season, setSeason] = useState(DEFAULT_SEASON);
  const [searchTerm, setSearchTerm] = useState('');
  const [positionFilter, setPositionFilter] = useState('ALL');

  const { players, status, error } = usePlayerStats(season);

  const filteredPlayers = useMemo(() => {
    return players.filter((p) => {
      const matchesSearch =
        !searchTerm ||
        `${p.firstName} ${p.lastName}`.toLowerCase().includes(searchTerm.toLowerCase());

      const pos = normalizePosition(p.position);
      let matchesFilter;
      if (positionFilter === 'ALL') matchesFilter = true;
      else if (positionFilter === 'SKATERS') matchesFilter = pos !== 'G';
      else matchesFilter = pos === positionFilter;

      return matchesSearch && matchesFilter;
    });
  }, [players, searchTerm, positionFilter]);

  const columns =
    positionFilter === 'G' ? GOALIE_COLUMNS : positionFilter === 'ALL' ? ALL_COLUMNS : SKATER_COLUMNS;
  const defaultSortKey = positionFilter === 'G' ? 'savePercentage' : 'points';

  return (
    <div className="min-h-screen bg-graphite">
      <Header />

      <main className="mx-auto max-w-6xl px-6 py-6">
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <SearchBar value={searchTerm} onChange={setSearchTerm} />
            <input
              type="text"
              value={season}
              onChange={(e) => setSeason(e.target.value)}
              className="w-28 rounded-md border border-rink-border bg-panel px-2 py-2 font-mono text-xs text-ink focus:border-amber focus:outline-none"
              aria-label="Season"
            />
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
          <StatsTable players={filteredPlayers} columns={columns} defaultSortKey={defaultSortKey} />
        )}
      </main>
    </div>
  );
}

export default App;
