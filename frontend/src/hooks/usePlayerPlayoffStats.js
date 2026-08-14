import { useEffect, useState } from 'react';
import { getPlayerSeasonStats } from '../api/players';

// The backend doesn't distinguish "this player has no playoff stats" from a
// genuine error (it throws either way) — since most players simply didn't
// play in the playoffs for a given season, we treat any failure here as
// "no playoff appearance" rather than surfacing it as an error.
export function usePlayerPlayoffStats(nhlId, season) {
  const [stats, setStats] = useState(null);
  const [status, setStatus] = useState('idle'); // idle | loading | success | none

  useEffect(() => {
    if (!nhlId || !season) return;

    let cancelled = false;
    setStatus('loading');

    getPlayerSeasonStats(nhlId, season, 'PLAYOFFS')
      .then((data) => {
        if (cancelled) return;
        setStats(data);
        setStatus('success');
      })
      .catch(() => {
        if (cancelled) return;
        setStats(null);
        setStatus('none');
      });

    return () => {
      cancelled = true;
    };
  }, [nhlId, season]);

  return { stats, status };
}
