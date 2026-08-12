const FILTERS = ['ALL', 'SKATERS', 'C', 'LW', 'RW', 'D', 'G'];

export function PositionFilter({ value, onChange }) {
  return (
    <div className="flex flex-wrap gap-1">
      {FILTERS.map((filter) => {
        const active = value === filter;
        return (
          <button
            key={filter}
            onClick={() => onChange(filter)}
            className={`rounded-md px-3 py-1.5 font-mono text-xs font-bold transition-colors ${
              active
                ? 'bg-amber text-graphite'
                : 'border border-rink-border bg-panel text-muted hover:text-ink'
            }`}
          >
            {filter}
          </button>
        );
      })}
    </div>
  );
}
