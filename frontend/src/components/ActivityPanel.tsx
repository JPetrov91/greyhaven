import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchActivity } from '../api/activity'

function formatWhen(iso: string): string {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) {
    return iso
  }
  return date.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function ActivityPanel() {
  const activityQuery = useQuery({
    queryKey: ['activity'],
    queryFn: fetchActivity,
    retry: false,
    refetchInterval: 15_000,
    refetchOnWindowFocus: true,
  })

  return (
    <aside className="game-column game-column-right" data-testid="activity-panel" aria-label="Activity">
      <h2>Activity</h2>
      {activityQuery.isLoading ? (
        <p className="muted">Loading activity…</p>
      ) : activityQuery.error instanceof ApiError ? (
        <p className="form-error" role="alert">
          {activityQuery.error.message}
        </p>
      ) : (activityQuery.data?.length ?? 0) === 0 ? (
        <p className="muted" data-testid="activity-empty">
          No recent events yet.
        </p>
      ) : (
        <ul className="activity-list" data-testid="activity-list">
          {activityQuery.data?.map((entry) => (
            <li key={entry.id} data-testid={`activity-${entry.type}`}>
              <p>{entry.message}</p>
              <time className="muted" dateTime={entry.createdAt}>
                {formatWhen(entry.createdAt)}
              </time>
            </li>
          ))}
        </ul>
      )}
    </aside>
  )
}
