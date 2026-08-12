import { apiClient } from './client';

export function getSeasons() {
  return apiClient.get('/games/seasons');
}
