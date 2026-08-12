import { useState } from 'react';
import { Header } from './components/Header';
import { StatsPage } from './components/StatsPage';
import { AdminPanel } from './components/AdminPanel';

function App() {
  const [page, setPage] = useState('stats');

  return (
    <div className="min-h-screen bg-graphite">
      <Header page={page} onNavigate={setPage} />
      {page === 'stats' ? <StatsPage /> : <AdminPanel />}
    </div>
  );
}

export default App;
