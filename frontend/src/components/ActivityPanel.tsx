import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchActivity } from '../api/activity'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'

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
    <Panel
      as="aside"
      className="game-column game-column-right"
      data-testid="activity-panel"
      aria-label="Activity"
      title="Activity"
    >
      {activityQuery.isLoading ? (
        <LoadingState>Loading activity…</LoadingState>
      ) : activityQuery.error instanceof ApiError ? (
        <ErrorState onRetry={() => void activityQuery.refetch()}>{activityQuery.error.message}</ErrorState>
      ) : (activityQuery.data?.length ?? 0) === 0 ? (
        <EmptyState testId="activity-empty">No recent events yet.</EmptyState>
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
    </Panel>
  )
}
