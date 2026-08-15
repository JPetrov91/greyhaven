import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchActivity } from '../api/activity'
import type { ActivityEntryResponse } from '../api/types'
import { activityIconUrl, activityMessageParts, formatRelativeTime } from '../ui/activityMedia'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { gameLink } from '../ui/gameNav'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'

type Filter = 'all' | 'events' | 'rewards' | 'alerts'

const FILTERS: { id: Filter; label: string }[] = [
  { id: 'all', label: 'All' },
  { id: 'events', label: 'Events' },
  { id: 'rewards', label: 'Rewards' },
  { id: 'alerts', label: 'Alerts' },
]

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
  const [filter, setFilter] = useState<Filter>('all')
  const [filterOpen, setFilterOpen] = useState(false)
  const [rewardsOpen, setRewardsOpen] = useState(true)
  const [alertsOpen, setAlertsOpen] = useState(true)

  const feed = activityQuery.data ?? []
  const visibleFeed = expanded ? feed : feed.slice(0, 4)
  const canExpand = feed.length > 4
  const hasAlerts = combatActive || encounterActive
  const showEvents = filter === 'all' || filter === 'events'
  const showRewards = (filter === 'all' || filter === 'rewards') && claimableExpedition
  const showAlerts = (filter === 'all' || filter === 'alerts') && hasAlerts

  return (
    <Panel
      as="aside"
      className="game-column game-column-right game-rail activity-rail"
      data-testid="activity-panel"
      aria-label="Activity"
    >
      <div className="activity-head">
        <h2>Activity & Notifications</h2>
        <div className="activity-filter">
          <button
            type="button"
            className="activity-filter-button"
            data-testid="activity-filter"
            aria-expanded={filterOpen}
            aria-haspopup="listbox"
            onClick={() => setFilterOpen((open) => !open)}
          >
            {FILTERS.find((entry) => entry.id === filter)?.label ?? 'All'}
          </button>
          {filterOpen ? (
            <ul className="activity-filter-menu" role="listbox" aria-label="Filter activity">
              {FILTERS.map((entry) => (
                <li key={entry.id}>
                  <button
                    type="button"
                    role="option"
                    aria-selected={filter === entry.id}
                    onClick={() => {
                      setFilter(entry.id)
                      setFilterOpen(false)
                    }}
                  >
                    {entry.label}
                  </button>
                </li>
              ))}
            </ul>
          ) : null}
        </div>
      </div>

      {showEvents ? (
        <section className="activity-section">
          <h3>Recent Events</h3>
          {activityQuery.isLoading ? (
            <LoadingState>Loading activity…</LoadingState>
          ) : activityQuery.error instanceof ApiError ? (
            <ErrorState onRetry={() => void activityQuery.refetch()}>{activityQuery.error.message}</ErrorState>
          ) : feed.length === 0 ? (
            <EmptyState testId="activity-empty">No recent events yet.</EmptyState>
          ) : (
            <ul className="activity-list" data-testid="activity-list">
              {visibleFeed.map((entry) => (
                <ActivityRow key={entry.id} entry={entry} />
              ))}
            </ul>
          )}
        </section>
      ) : null}

      {showRewards ? (
        <section className="activity-section" data-testid="activity-claimable">
          <button
            type="button"
            className="activity-section-toggle"
            aria-expanded={rewardsOpen}
            onClick={() => setRewardsOpen((current) => !current)}
          >
            <h3>
              Claimable Rewards
              <span className="activity-badge">1</span>
            </h3>
            <span className="activity-chevron" aria-hidden="true">
              {rewardsOpen ? '^' : 'v'}
            </span>
          </button>
          {rewardsOpen ? (
            <div className="activity-row">
              <img className="activity-icon" src={activityIconUrl('EXPEDITION_COMPLETED')} alt="" />
              <p>
                Expedition complete. Claim your{' '}
                <span className="activity-hl-blue">rewards</span>.
              </p>
              <Link to={gameLink('expeditions')} className="activity-claim" data-testid="rail-claim-expedition">
                Claim
              </Link>
            </div>
          ) : null}
        </section>
      ) : null}

      {showAlerts ? (
        <section className="activity-section" data-testid="activity-alerts">
          <button
            type="button"
            className="activity-section-toggle"
            aria-expanded={alertsOpen}
            onClick={() => setAlertsOpen((current) => !current)}
          >
            <h3>Alerts</h3>
            <span className="activity-chevron" aria-hidden="true">
              {alertsOpen ? '^' : 'v'}
            </span>
          </button>
          {alertsOpen ? (
            <ul className="activity-list">
              {combatActive ? (
                <li className="activity-row">
                  <img className="activity-icon" src={activityIconUrl('alert')} alt="" />
                  <p>Combat is in progress.</p>
                </li>
              ) : null}
              {encounterActive ? (
                <li className="activity-row">
                  <img className="activity-icon" src={activityIconUrl('alert')} alt="" />
                  <p>An encounter is waiting.</p>
                </li>
              ) : null}
            </ul>
          ) : null}
        </section>
      ) : null}

      {canExpand && showEvents ? (
        <button
          type="button"
          className="activity-view-all"
          data-testid="activity-view-all"
          onClick={() => setExpanded((current) => !current)}
        >
          {expanded ? 'Collapse' : 'View All Notifications'}
        </button>
      ) : null}
    </Panel>
  )
}

function ActivityRow({ entry }: { entry: ActivityEntryResponse }) {
  return (
    <li className="activity-row" data-testid={`activity-${entry.type}`}>
      <img className="activity-icon" src={activityIconUrl(entry.type)} alt="" />
      <p>
        {activityMessageParts(entry.type, entry.message).map((part, index) =>
          part.tone === 'plain' ? (
            part.text
          ) : (
            <span key={`${entry.id}-${index}`} className={`activity-hl-${part.tone}`}>
              {part.text}
            </span>
          ),
        )}
      </p>
      <time dateTime={entry.createdAt}>{formatRelativeTime(entry.createdAt)}</time>
    </li>
  )
}
