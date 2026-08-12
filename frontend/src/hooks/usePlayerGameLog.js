import { useEffect, useState } from 'react';
import { getPlayerGameLog } from '../api/players';

export function usePlayerGameLog(nhlId, season) {
  const [games, setGames] = useState([]);
  const [status, setStatus] = useState('idle');
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!nhlId || !season) return;

    let cancelled = false;
    setStatus('loading');
    setError(null);

    getPlayerGameLog(nhlId, season)
      .then((data) => {
        if (cancelled) return;
        setGames(data);
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
  }, [nhlId, season]);

  return { games, status, error };
}
