import { apiClient } from './client';

export function getSeasonStats(season) {
  return apiClient.get(`/players/stats/${season}`);
}

export function getPlayerSeasonStats(nhlId, season) {
  return apiClient.get(`/players/${nhlId}/stats/${season}`);
}

export function getPlayerGameLog(nhlId, season) {
  return apiClient.get(`/players/${nhlId}/games/${season}`);
}
