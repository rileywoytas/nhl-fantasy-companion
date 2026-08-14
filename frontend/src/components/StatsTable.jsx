import { useMemo, useState } from 'react';
import { PosBadge } from './PosBadge';

function formatValue(col, value) {
  if (value === null || value === undefined) return '—';
  if (col.format) return col.format(value);
  return value;
}

function buildRankTooltip(rankInfo) {
  if (!rankInfo) return undefined;
  const lines = [];
  if (rankInfo.overall) lines.push(`Overall: #${rankInfo.overall.rank} of ${rankInfo.overall.total}`);
  if (rankInfo.category) lines.push(`${rankInfo.category.label}: #${rankInfo.category.rank} of ${rankInfo.category.total}`);
  if (rankInfo.position) lines.push(`${rankInfo.position.label}: #${rankInfo.position.rank} of ${rankInfo.position.total}`);
  return lines.join('\n');
}

export function StatsTable({ players, columns, defaultSortKey, onRowClick, rankById }) {
  const [sortKey, setSortKey] = useState(defaultSortKey);
  const [sortDir, setSortDir] = useState('desc');

  // Reset sort when the column set changes (e.g. switching filters) and the
  // previously-sorted column no longer exists in this table.
  const activeSortKey =
    sortKey === 'lastName' || sortKey === 'teamTriCode' || columns.some((c) => c.key === sortKey)
      ? sortKey
      : defaultSortKey;

  const sortedPlayers = useMemo(() => {
    const copy = [...players];
    copy.sort((a, b) => {
      const aVal = a[activeSortKey];
      const bVal = b[activeSortKey];

      if (aVal === null || aVal === undefined) return 1;
      if (bVal === null || bVal === undefined) return -1;

      if (typeof aVal === 'string') {
        return sortDir === 'asc' ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);
      }
      return sortDir === 'asc' ? aVal - bVal : bVal - aVal;
    });
    return copy;
  }, [players, activeSortKey, sortDir]);

  function handleSort(key) {
    if (key === activeSortKey) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(key);
      setSortDir('desc');
    }
  }

  function SortableHeader({ sortColKey, label, align = 'right' }) {
    return (
      <th
        onClick={() => handleSort(sortColKey)}
        className={`sticky top-0 z-10 cursor-pointer select-none bg-panel px-3 py-2 text-${align} text-xs font-bold text-muted hover:text-amber-light`}
      >
        {label}
        {activeSortKey === sortColKey && (
          <span className="ml-1 text-amber">{sortDir === 'asc' ? '▲' : '▼'}</span>
        )}
      </th>
    );
  }

  return (
    <div className="overflow-x-auto rounded-md border border-rink-border">
      <table className="w-full font-mono text-sm">
        <thead>
          <tr className="bg-panel">
            <SortableHeader sortColKey="lastName" label="Player" align="left" />
            <SortableHeader sortColKey="teamTriCode" label="Team" align="left" />
            {columns.map((col) => (
              <SortableHeader key={col.key} sortColKey={col.key} label={col.label} />
            ))}
          </tr>
        </thead>
        <tbody>
          {sortedPlayers.map((p, i) => (
            <tr
              key={p.playerId}
              onClick={() => onRowClick && onRowClick(p)}
              className={`border-t border-rink-border ${i % 2 === 0 ? 'bg-graphite' : 'bg-panel-alt'} ${
                onRowClick ? 'cursor-pointer hover:bg-panel' : ''
              }`}
            >
              <td className="px-3 py-2 text-left">
                <div className="flex items-center gap-2">
                  <span className="font-sans font-medium text-ink">
                    {p.lastName}, {p.firstName}
                  </span>
                  <PosBadge position={p.position} />
                </div>
              </td>
              <td className="px-3 py-2 text-left text-muted">{p.teamTriCode ?? '—'}</td>
              {columns.map((col) => (
                <td
                  key={col.key}
                  title={col.key === 'fantasyPoints' ? buildRankTooltip(rankById?.get(p.playerId)) : undefined}
                  className={`px-3 py-2 text-right ${col.highlight ? 'font-bold text-amber-light' : ''}`}
                >
                  {formatValue(col, p[col.key])}
                </td>
              ))}
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
