import { apiClient } from './client';

export function getSeasonStats(season) {
  return apiClient.get(`/players/stats/${season}`);
}

export function getPlayerSeasonStats(nhlId, season, gameType = 'REGULAR_SEASON') {
  return apiClient.get(`/players/${nhlId}/stats/${season}?gameType=${gameType}`);
}

export function getPlayerGameLog(nhlId, season) {
  return apiClient.get(`/players/${nhlId}/games/${season}`);
}
