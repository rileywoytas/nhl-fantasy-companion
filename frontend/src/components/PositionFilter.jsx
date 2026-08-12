const FILTERS = [
  { key: 'SKATERS', label: 'SKATERS' },
  { key: 'G', label: 'GOALIE' },
  { key: 'C', label: 'C' },
  { key: 'LW', label: 'LW' },
  { key: 'RW', label: 'RW' },
  { key: 'D', label: 'D' },
  { key: 'ALL', label: 'ALL' },
];

export function PositionFilter({ value, onChange }) {
  return (
    <div className="flex flex-wrap gap-1">
      {FILTERS.map((filter) => {
        const active = value === filter.key;
        return (
          <button
            key={filter.key}
            onClick={() => onChange(filter.key)}
            className={`rounded-md px-3 py-1.5 font-mono text-xs font-bold transition-colors ${
              active
                ? 'bg-amber text-graphite'
                : 'border border-rink-border bg-panel text-muted hover:text-ink'
            }`}
          >
            {filter.label}
          </button>
        );
      })}
    </div>
  );
}
