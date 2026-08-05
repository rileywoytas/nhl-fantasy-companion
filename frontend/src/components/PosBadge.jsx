export function PosBadge({ position }) {
  if (!position) return null;
  return (
    <span className="inline-block rounded border border-rink-border bg-panel-alt px-1.5 py-0.5 font-mono text-[11px] font-bold text-amber-light">
      {position}
    </span>
  );
}
