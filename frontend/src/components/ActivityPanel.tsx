import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchActivity } from '../api/activity'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { gameLink } from '../ui/gameNav'
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

type Props = {
  claimableExpedition?: boolean
  combatActive?: boolean
  encounterActive?: boolean
}

export function ActivityPanel({
  claimableExpedition = false,
  combatActive = false,
  encounterActive = false,
}: Props) {
  const activityQuery = useQuery({
    queryKey: ['activity'],
    queryFn: fetchActivity,
    retry: false,
    refetchInterval: 15_000,
    refetchOnWindowFocus: true,
  })
  const [expanded, setExpanded] = useState(false)

  const hasAlerts = combatActive || encounterActive

  return (
    <Panel
      as="aside"
      className="game-column game-column-right game-rail"
      data-testid="activity-panel"
      aria-label="Activity"
      title="Activity"
    >
      {claimableExpedition ? (
        <section className="rail-section" data-testid="activity-claimable">
          <h3>Claimable Rewards</h3>
          <p>Expedition complete</p>
          <NavLink to={gameLink('expeditions')} className="btn btn-primary" data-testid="rail-claim-expedition">
            Claim
          </NavLink>
        </section>
      ) : null}

      {hasAlerts ? (
        <section className="rail-section" data-testid="activity-alerts">
          <h3>Alerts</h3>
          {combatActive ? <p>Combat is in progress.</p> : null}
          {encounterActive ? <p>An encounter is waiting.</p> : null}
        </section>
      ) : null}

      <section className="rail-section">
        <h3>Recent Events</h3>
        {activityQuery.isLoading ? (
          <LoadingState>Loading activity…</LoadingState>
        ) : activityQuery.error instanceof ApiError ? (
          <ErrorState onRetry={() => void activityQuery.refetch()}>{activityQuery.error.message}</ErrorState>
        ) : (activityQuery.data?.length ?? 0) === 0 ? (
          <EmptyState testId="activity-empty">No recent events yet.</EmptyState>
        ) : (
          <ul className="activity-list" data-testid="activity-list" style={expanded ? { maxHeight: 'none' } : undefined}>
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
      </section>

      <button
        type="button"
        className="btn btn-secondary"
        data-testid="activity-view-all"
        onClick={() => setExpanded((current) => !current)}
      >
        {expanded ? 'Collapse' : 'View All Notifications'}
      </button>
    </Panel>
  )
}
