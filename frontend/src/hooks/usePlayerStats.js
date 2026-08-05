import { useEffect, useState } from 'react';
import { getSeasonStats } from '../api/players';

export function usePlayerStats(season) {
  const [players, setPlayers] = useState([]);
  const [status, setStatus] = useState('idle'); // idle | loading | success | error
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!season) return;

    let cancelled = false;
    setStatus('loading');
    setError(null);

    getSeasonStats(season)
      .then((data) => {
        if (cancelled) return;
        setPlayers(data);
        setStatus('success');
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err.message);
        setStatus('error');
      });

    return () => {
      cancelled = true;
    };
  }, [season]);

  return { players, status, error };
}
