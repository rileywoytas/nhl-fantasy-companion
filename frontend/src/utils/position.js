// Normalizes any raw position value (single-letter NHL API codes, or already-
// expanded values) into one of: C, LW, RW, D, G. Falls back to the trimmed,
// uppercased raw value if nothing matches, rather than silently showing
// something unexpected.
const NORMALIZE_MAP = {
  C: 'C',
  L: 'LW',
  LW: 'LW',
  R: 'RW',
  RW: 'RW',
  D: 'D',
  G: 'G',
};

export function normalizePosition(code) {
  if (!code) return null;
  const key = code.trim().toUpperCase();
  return NORMALIZE_MAP[key] ?? key;
}

export function displayPosition(code) {
  return normalizePosition(code) ?? '—';
}
