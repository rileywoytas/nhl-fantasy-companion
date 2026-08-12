function formatSavePct(v) {
  return v.toFixed(3).replace(/^0/, '');
}

export const SKATER_COLUMNS = [
  { key: 'gamesPlayed', label: 'GP' },
  { key: 'goals', label: 'G' },
  { key: 'assists', label: 'A' },
  { key: 'points', label: 'PTS', highlight: true },
  { key: 'plusMinus', label: '+/-' },
  { key: 'pim', label: 'PIM' },
  { key: 'shots', label: 'SOG' },
  { key: 'hits', label: 'HIT' },
  { key: 'blocks', label: 'BLK' },
];

// All goalie stat fields currently captured by the backend. Wins/Losses/
// Shutouts aren't tracked yet (Group B work) — once they land, add them here.
export const GOALIE_COLUMNS = [
  { key: 'gamesPlayed', label: 'GP' },
  { key: 'starts', label: 'GS' },
  { key: 'saves', label: 'SV' },
  { key: 'shotsAgainst', label: 'SA' },
  { key: 'goalsAgainst', label: 'GA' },
  { key: 'savePercentage', label: 'SV%', highlight: true, format: formatSavePct },
];

// Combined view for the "ALL" filter — skaters and goalies in one table.
// Goalie rows will show blank skater stats and vice versa.
export const ALL_COLUMNS = [
  { key: 'gamesPlayed', label: 'GP' },
  ...SKATER_COLUMNS.filter((c) => c.key !== 'gamesPlayed'),
  ...GOALIE_COLUMNS.filter((c) => c.key !== 'gamesPlayed'),
];
