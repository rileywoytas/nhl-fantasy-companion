import { useEffect, useState } from 'react';
import { getSeasons } from '../api/games';

export function useSeasons() {
  const [seasons, setSeasons] = useState([]);
  const [status, setStatus] = useState('idle');

  useEffect(() => {
    let cancelled = false;
    setStatus('loading');

    getSeasons()
      .then((data) => {
        if (cancelled) return;
        setSeasons(data);
        setStatus('success');
      })
      .catch(() => {
        if (!cancelled) setStatus('error');
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return { seasons, status };
}
