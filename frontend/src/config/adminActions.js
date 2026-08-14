// Each action maps to one backend import endpoint. To add a new one later
// (e.g. a new advanced-stats column source), just add an entry here — the
// panel renders and runs it automatically, no new UI needed.
//
// `endpoint` can be a plain string, or a function of the field values for
// actions that take parameters.

export const ADMIN_ACTIONS = [
  {
    id: 'import-teams',
    group: 'Teams',
    label: 'Import Teams',
    description: 'Pulls the full list of NHL teams.',
    endpoint: '/teams/import',
    fields: [],
  },
  {
    id: 'import-skaters',
    group: 'Players',
    label: 'Import Skaters',
    description: 'Pulls current skaters with time on ice > 0.',
    endpoint: '/players/import/skaters',
    fields: [],
  },
  {
    id: 'import-goalies',
    group: 'Players',
    label: 'Import Goalies',
    description: 'Pulls current goalies.',
    endpoint: '/players/import/goalies',
    fields: [],
  },
  {
    id: 'backfill-players',
    group: 'Players',
    label: 'Backfill Missing Players',
    description: 'Fetches player info for anyone with box score stats but no player record (e.g. retired players).',
    endpoint: '/players/backfill',
    fields: [],
  },
  {
    id: 'import-games',
    group: 'Games',
    label: 'Import Games',
    description: 'Pulls the game schedule for every season from the starting year through the current one.',
    endpoint: (v) => `/games/import/${v.startingYear}`,
    fields: [{ key: 'startingYear', label: 'Starting Year', type: 'number', default: 2015 }],
  },
  {
    id: 'import-box-scores',
    group: 'Games',
    label: 'Import Season Box Scores',
    description: 'Pulls per-game player stats for every game in a season. Can take a while for a full season.',
    endpoint: (v) => `/import/boxscore/season/${v.season}`,
    fields: [{ key: 'season', label: 'Season', type: 'text', default: '20252026' }],
  },
  {
    id: 'import-advanced-stats',
    group: 'Advanced Stats',
    label: 'Import Advanced Stats',
    description: 'Pulls season-total PPG, PPA, SHG, GWG for skaters and W/L/OTL/SHO for goalies.',
    endpoint: (v) => `/players/import/advanced-stats?season=${v.season}&gameType=${v.gameType}`,
    fields: [
      { key: 'season', label: 'Season', type: 'text', default: '20252026' },
      {
        key: 'gameType',
        label: 'Game Type',
        type: 'select',
        options: ['REGULAR_SEASON', 'PLAYOFFS'],
        default: 'REGULAR_SEASON',
      },
    ],
  },
  {
    id: 'import-scoring-details',
    group: 'Advanced Stats',
    label: 'Import Per-Game Scoring Details',
    description:
      'Fills in real per-game PPG/PPA/SHG/GWG (used for per-game FPTS in the player modal). Run this after Import Season Box Scores for the same season — it only updates games that are already imported. Resumable: re-running only processes games not yet done.',
    endpoint: (v) => `/import/scoring-details/season/${v.season}`,
    progressEndpoint: '/import/scoring-details/progress',
    fields: [{ key: 'season', label: 'Season', type: 'text', default: '20252026' }],
  },
];
