import { useEffect, useRef, useState } from 'react';
import { apiClient } from '../api/client';

function buildEndpoint(action, values) {
  return typeof action.endpoint === 'function' ? action.endpoint(values) : action.endpoint;
}

function summarize(result) {
  // Backend actions return either a plain count (number) or a message
  // (string, sometimes multi-line with a breakdown). Render both cleanly
  // without assuming a particular shape.
  if (typeof result === 'number') {
    return { headline: `${result} record${result === 1 ? '' : 's'}`, detail: null };
  }
  if (typeof result === 'string') {
    const lines = result.split('\n').filter(Boolean);
    return { headline: lines[0] ?? result, detail: lines.slice(1) };
  }
  return { headline: String(result), detail: null };
}

export function ActionCard({ action }) {
  const initialValues = Object.fromEntries(action.fields.map((f) => [f.key, f.default ?? '']));
  const [values, setValues] = useState(initialValues);
  const [status, setStatus] = useState('idle'); // idle | running | success | error
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [ranAt, setRanAt] = useState(null);
  const [progress, setProgress] = useState(null);
  const pollRef = useRef(null);

  // While running, poll the action's progress endpoint (if it declares one)
  // so long imports show live status instead of a blank "Running…" for
  // minutes at a time.
  useEffect(() => {
    if (status === 'running' && action.progressEndpoint) {
      const poll = () => {
        apiClient
          .get(action.progressEndpoint)
          .then(setProgress)
          .catch(() => {}); // progress polling failures are non-critical — just skip this tick
      };
      poll();
      pollRef.current = setInterval(poll, 1200);
      return () => clearInterval(pollRef.current);
    }
    clearInterval(pollRef.current);
  }, [status, action.progressEndpoint]);

  async function handleRun() {
    setStatus('running');
    setError(null);
    setProgress(null);
    try {
      const response = await apiClient.post(buildEndpoint(action, values));
      setResult(response);
      setStatus('success');
      setRanAt(new Date());
    } catch (err) {
      setError(err.message);
      setStatus('error');
      setRanAt(new Date());
    }
  }

  const summary = status === 'success' ? summarize(result) : null;

  return (
    <div className="rounded-md border border-rink-border bg-panel p-4">
      <div className="mb-1 flex items-center justify-between gap-2">
        <h3 className="font-mono text-sm font-bold text-ink">{action.label}</h3>
        {ranAt && (
          <span className="whitespace-nowrap font-mono text-[10px] text-muted">
            {ranAt.toLocaleTimeString()}
          </span>
        )}
      </div>
      <p className="mb-3 text-xs text-muted">{action.description}</p>

      {action.fields.length > 0 && (
        <div className="mb-3 flex flex-wrap gap-2">
          {action.fields.map((field) => (
            <label key={field.key} className="flex flex-col gap-1">
              <span className="font-mono text-[10px] text-muted">{field.label}</span>
              {field.type === 'select' ? (
                <select
                  value={values[field.key]}
                  onChange={(e) => setValues((v) => ({ ...v, [field.key]: e.target.value }))}
                  className="rounded border border-rink-border bg-panel-alt px-2 py-1 font-mono text-xs text-ink focus:border-amber focus:outline-none"
                >
                  {field.options.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              ) : (
                <input
                  type={field.type}
                  value={values[field.key]}
                  onChange={(e) => setValues((v) => ({ ...v, [field.key]: e.target.value }))}
                  className="w-28 rounded border border-rink-border bg-panel-alt px-2 py-1 font-mono text-xs text-ink focus:border-amber focus:outline-none"
                />
              )}
            </label>
          ))}
        </div>
      )}

      <button
        onClick={handleRun}
        disabled={status === 'running'}
        className="rounded-md bg-amber px-3 py-1.5 font-mono text-xs font-bold text-graphite transition-opacity hover:opacity-90 disabled:opacity-50"
      >
        {status === 'running' ? 'Running…' : 'Run'}
      </button>

      {status === 'running' && progress && (
        <div className="mt-3 rounded border border-amber/30 bg-panel-alt px-3 py-2">
          <div className="flex items-center justify-between font-mono text-xs font-bold text-amber-light">
            <span>
              {progress.processedGames} / {progress.totalGames} games
            </span>
            <span className="text-muted">{progress.elapsedSeconds}s</span>
          </div>
          {progress.lastGameId && (
            <p className="mt-1 font-mono text-[11px] text-muted">
              Last: game {progress.lastGameId}
              {progress.lastGameDate ? ` (${progress.lastGameDate})` : ''} — {progress.lastEventSummary}
            </p>
          )}
          {progress.failedGames > 0 && (
            <p className="mt-1 font-mono text-[11px] text-flag-red">{progress.failedGames} failed so far</p>
          )}
        </div>
      )}

      {status === 'success' && summary && (
        <div className="mt-3 rounded border border-flag-green/40 bg-panel-alt px-3 py-2">
          <p className="font-mono text-xs font-bold text-flag-green">{summary.headline}</p>
          {summary.detail && summary.detail.length > 0 && (
            <ul className="mt-1 space-y-0.5">
              {summary.detail.map((line, i) => (
                <li key={i} className="font-mono text-[11px] text-muted">
                  {line}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {status === 'error' && (
        <div className="mt-3 rounded border border-flag-red/40 bg-panel-alt px-3 py-2">
          <p className="font-mono text-xs font-bold text-flag-red">Failed</p>
          <p className="mt-1 font-mono text-[11px] text-muted">{error}</p>
        </div>
      )}
    </div>
  );
}
