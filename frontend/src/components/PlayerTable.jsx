import { useMemo, useState } from 'react';
import { PosBadge } from './PosBadge';

const COLUMNS = [
  { key: 'lastName', label: 'Player', align: 'left' },
  { key: 'teamTriCode', label: 'Team', align: 'left' },
  { key: 'gamesPlayed', label: 'GP', align: 'right' },
  { key: 'goals', label: 'G', align: 'right' },
  { key: 'assists', label: 'A', align: 'right' },
  { key: 'points', label: 'PTS', align: 'right' },
  { key: 'plusMinus', label: '+/-', align: 'right' },
  { key: 'savePercentage', label: 'SV%', align: 'right' },
];

function formatStat(key, value) {
  if (value === null || value === undefined) return '—';
  if (key === 'savePercentage') return value.toFixed(3).replace(/^0/, '');
  return value;
}

export function PlayerTable({ players }) {
  const [sortKey, setSortKey] = useState('points');
  const [sortDir, setSortDir] = useState('desc');

  const sortedPlayers = useMemo(() => {
    const withValues = [...players];
    withValues.sort((a, b) => {
      const aVal = a[sortKey];
      const bVal = b[sortKey];

      // Nulls always sort last, regardless of direction
      if (aVal === null || aVal === undefined) return 1;
      if (bVal === null || bVal === undefined) return -1;

      if (typeof aVal === 'string') {
        return sortDir === 'asc' ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);
      }
      return sortDir === 'asc' ? aVal - bVal : bVal - aVal;
    });
    return withValues;
  }, [players, sortKey, sortDir]);

  function handleSort(key) {
    if (key === sortKey) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(key);
      setSortDir('desc');
    }
  }

  return (
    <div className="overflow-x-auto rounded-md border border-rink-border">
      <table className="w-full font-mono text-sm">
        <thead>
          <tr className="bg-panel">
            {COLUMNS.map((col) => (
              <th
                key={col.key}
                onClick={() => handleSort(col.key)}
                className={`cursor-pointer select-none px-3 py-2 text-${col.align} text-xs font-bold text-muted hover:text-amber-light`}
              >
                {col.label}
                {sortKey === col.key && (
                  <span className="ml-1 text-amber">{sortDir === 'asc' ? '▲' : '▼'}</span>
                )}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {sortedPlayers.map((p, i) => (
            <tr
              key={p.playerId}
              className={`border-t border-rink-border ${i % 2 === 0 ? 'bg-graphite' : 'bg-panel-alt'}`}
            >
              <td className="px-3 py-2 text-left">
                <span className="font-sans font-medium text-ink">
                  {p.lastName}, {p.firstName}
                </span>
                <PosBadge position={p.position} />
              </td>
              <td className="px-3 py-2 text-left text-muted">{p.teamTriCode ?? '—'}</td>
              <td className="px-3 py-2 text-right">{formatStat('gamesPlayed', p.gamesPlayed)}</td>
              <td className="px-3 py-2 text-right">{formatStat('goals', p.goals)}</td>
              <td className="px-3 py-2 text-right">{formatStat('assists', p.assists)}</td>
              <td className="px-3 py-2 text-right font-bold text-amber-light">
                {formatStat('points', p.points)}
              </td>
              <td className="px-3 py-2 text-right">{formatStat('plusMinus', p.plusMinus)}</td>
              <td className="px-3 py-2 text-right">
                {formatStat('savePercentage', p.savePercentage)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {sortedPlayers.length === 0 && (
        <div className="px-3 py-8 text-center text-sm text-muted">No players match your search.</div>
      )}
    </div>
  );
}
