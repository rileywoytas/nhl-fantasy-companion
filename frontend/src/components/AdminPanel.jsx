import { ADMIN_ACTIONS } from '../config/adminActions';
import { ActionCard } from './ActionCard';

export function AdminPanel() {
  const groups = [...new Set(ADMIN_ACTIONS.map((a) => a.group))];

  return (
    <div className="mx-auto max-w-3xl px-6 py-6">
      <p className="mb-6 font-mono text-xs text-muted">
        Run data imports and see results here instead of guessing from server logs.
      </p>
      {groups.map((group) => (
        <div key={group} className="mb-6">
          <h2 className="mb-2 font-mono text-xs font-bold uppercase tracking-wider text-amber-light">
            {group}
          </h2>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {ADMIN_ACTIONS.filter((a) => a.group === group).map((action) => (
              <ActionCard key={action.id} action={action} />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
