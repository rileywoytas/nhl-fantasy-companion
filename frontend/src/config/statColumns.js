function formatSavePct(v) {
  return v.toFixed(3).replace(/^0/, '');
}

function formatWinPct(v) {
  return v.toFixed(3).replace(/^0/, '');
}

function formatFantasyPoints(v) {
  return v.toFixed(1);
}

// Rounds to the nearest whole second first, then splits into minutes/seconds
// — splitting the unrounded value first could show e.g. "1:60" instead of
// rolling over to "2:00" when the fractional seconds rounded up to 60.
function formatToi(seconds) {
  if (seconds === null || seconds === undefined || Number.isNaN(seconds)) return '—';
  const total = Math.round(seconds);
  const m = Math.floor(total / 60);
  const s = total % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
}

export const SKATER_COLUMNS = [
  { key: 'gamesPlayed', label: 'GP' },
  { key: 'goals', label: 'G' },
  { key: 'assists', label: 'A' },
  { key: 'points', label: 'PTS' },
  { key: 'plusMinus', label: '+/-' },
  { key: 'pim', label: 'PIM' },
  { key: 'shots', label: 'SOG' },
  { key: 'hits', label: 'HIT' },
  { key: 'blocks', label: 'BLK' },
  { key: 'powerPlayPoints', label: 'PPP' },
  { key: 'shorthandedGoals', label: 'SHG' },
  { key: 'gameWinningGoals', label: 'GWG' },
  { key: 'avgToiSeconds', label: 'TOI/G', format: formatToi },
  { key: 'fantasyPoints', label: 'FPTS', highlight: true, format: formatFantasyPoints },
];

// All goalie stat fields currently captured by the backend.
export const GOALIE_COLUMNS = [
  { key: 'gamesPlayed', label: 'GP' },
  { key: 'starts', label: 'GS' },
  { key: 'wins', label: 'W' },
  { key: 'losses', label: 'L' },
  { key: 'otLosses', label: 'OTL' },
  { key: 'winPct', label: 'W%', format: formatWinPct },
  { key: 'saves', label: 'SV' },
  { key: 'shotsAgainst', label: 'SA' },
  { key: 'goalsAgainst', label: 'GA' },
  { key: 'savePercentage', label: 'SV%', format: formatSavePct },
  { key: 'shutouts', label: 'SHO' },
  { key: 'fantasyPoints', label: 'FPTS', highlight: true, format: formatFantasyPoints },
];

// Combined view for the "ALL" filter — skaters and goalies in one table.
// Goalie rows will show blank skater stats and vice versa.
export const ALL_COLUMNS = [
  { key: 'gamesPlayed', label: 'GP' },
  ...SKATER_COLUMNS.filter((c) => c.key !== 'gamesPlayed' && c.key !== 'fantasyPoints'),
  ...GOALIE_COLUMNS.filter((c) => c.key !== 'gamesPlayed' && c.key !== 'fantasyPoints'),
  { key: 'fantasyPoints', label: 'FPTS', highlight: true, format: formatFantasyPoints },
];
