// Converts the raw "20252026" season format from the backend into a
// display-friendly "2025 - 2026".
export function formatSeasonLabel(season) {
  if (!season || season.length !== 8) return season;
  return `${season.slice(0, 4)} - ${season.slice(4, 8)}`;
}
