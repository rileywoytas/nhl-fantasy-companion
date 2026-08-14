import { normalizePosition } from './position';

export const SKATER_SCORING = {
  goals: 2,
  assists: 1,
  plusMinus: 1,
  powerPlayPoints: 1,
  shorthandedGoals: 4,
  gameWinningGoals: 2,
  blocks: 0.2,
};

export const GOALIE_SCORING = {
  wins: 2,
  losses: -1,
  goalsAgainst: -1,
  saves: 0.15,
  shutouts: 4,
};

// Treats missing/not-yet-imported stats as 0 rather than making the whole
// total NaN — a player with no advanced stats imported yet will just score
// 0 for those categories instead of breaking the column.
function n(value) {
  return value ?? 0;
}

export function calculateSkaterFantasyPoints(p) {
  return (
    n(p.goals) * SKATER_SCORING.goals +
    n(p.assists) * SKATER_SCORING.assists +
    n(p.plusMinus) * SKATER_SCORING.plusMinus +
    (n(p.powerPlayGoals) + n(p.powerPlayAssists)) * SKATER_SCORING.powerPlayPoints +
    n(p.shorthandedGoals) * SKATER_SCORING.shorthandedGoals +
    n(p.gameWinningGoals) * SKATER_SCORING.gameWinningGoals +
    n(p.blocks) * SKATER_SCORING.blocks
  );
}

export function calculateGoalieFantasyPoints(p) {
  return (
    n(p.wins) * GOALIE_SCORING.wins +
    n(p.losses) * GOALIE_SCORING.losses +
    n(p.goalsAgainst) * GOALIE_SCORING.goalsAgainst +
    n(p.saves) * GOALIE_SCORING.saves +
    n(p.shutouts) * GOALIE_SCORING.shutouts
  );
}

export function calculateFantasyPoints(player) {
  return normalizePosition(player.position) === 'G'
    ? calculateGoalieFantasyPoints(player)
    : calculateSkaterFantasyPoints(player);
}
