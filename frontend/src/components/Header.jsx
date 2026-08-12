const PAGES = [
  { key: 'stats', label: 'Stats' },
  { key: 'admin', label: 'Data' },
];

export function Header({ page, onNavigate }) {
  return (
    <header className="border-b-2 border-amber bg-panel px-6 py-4">
      <div className="mx-auto flex max-w-[1600px] items-center justify-between">
        <h1 className="font-mono text-xl font-bold tracking-wider text-amber-light">
          ICE SHEET
        </h1>
        <nav className="flex gap-1">
          {PAGES.map((p) => (
            <button
              key={p.key}
              onClick={() => onNavigate(p.key)}
              className={`rounded px-3 py-1 font-mono text-xs font-bold transition-colors ${
                page === p.key ? 'bg-amber text-graphite' : 'text-muted hover:text-ink'
              }`}
            >
              {p.label}
            </button>
          ))}
        </nav>
      </div>
    </header>
  );
}
