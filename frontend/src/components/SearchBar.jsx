export function SearchBar({ value, onChange }) {
  return (
    <input
      type="text"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder="Search players…"
      className="w-full rounded-md border border-rink-border bg-panel px-3 py-2 text-sm text-ink placeholder:text-muted focus:border-amber focus:outline-none sm:w-64"
    />
  );
}
