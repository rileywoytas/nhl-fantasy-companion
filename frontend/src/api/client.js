const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  const contentType = res.headers.get('content-type') || '';
  const isJson = contentType.includes('application/json');
  const body = isJson ? await res.json().catch(() => null) : await res.text();

  if (!res.ok) {
    const message = isJson ? JSON.stringify(body) : body;
    throw new Error(`${res.status} ${res.statusText}${message ? ` — ${message}` : ''}`);
  }

  return body;
}

export const apiClient = {
  get: (path) => request(path),
  post: (path) => request(path, { method: 'POST' }),
};
