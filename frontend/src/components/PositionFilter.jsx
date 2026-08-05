const POSITIONS = ['ALL', 'C', 'LW', 'RW', 'D', 'G'];

export function PositionFilter({ value, onChange }) {
  return (
    <div className="flex gap-1">
      {POSITIONS.map((pos) => {
        const active = value === pos;
        return (
          <button
            key={pos}
            onClick={() => onChange(pos)}
            className={`rounded-md px-3 py-1.5 font-mono text-xs font-bold transition-colors ${
              active
                ? 'bg-amber text-graphite'
                : 'border border-rink-border bg-panel text-muted hover:text-ink'
            }`}
          >
            {pos}
          </button>
        );
      })}
    </div>
  );
}
