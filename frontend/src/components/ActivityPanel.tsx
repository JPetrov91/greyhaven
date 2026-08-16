import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchActivity } from '../api/activity'
import type { ActivityEntryResponse } from '../api/types'
import { ActivityRow } from '../ui/ActivityRow'
import { NotificationRow } from '../ui/NotificationRow'
import { activityIconUrl, activityMessageParts, activityRowVariant, formatRelativeTime } from '../ui/activityMedia'
import { Button } from '../ui/Button'
import { CounterBadge } from '../ui/CounterBadge'
import { Dropdown } from '../ui/Dropdown'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { gameLink } from '../ui/gameNav'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { Section } from '../ui/Section'

type Filter = 'all' | 'events' | 'rewards' | 'alerts'

const FILTERS: { value: Filter; label: string }[] = [
  { value: 'all', label: 'All' },
  { value: 'events', label: 'Events' },
  { value: 'rewards', label: 'Rewards' },
  { value: 'alerts', label: 'Alerts' },
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
      title={<span className="type-panel-heading">Activity & Notifications</span>}
      actions={
        <Dropdown
          testId="activity-filter"
          aria-label="Filter activity"
          value={filter}
          onChange={(value) => setFilter(value as Filter)}
          options={FILTERS}
        />
      }
    >
      {showEvents ? (
        <Section title="Recent Events" className="activity-section" divider={false}>
          {activityQuery.isLoading ? (
            <LoadingState>Loading activity…</LoadingState>
          ) : activityQuery.error instanceof ApiError ? (
            <ErrorState onRetry={() => void activityQuery.refetch()}>{activityQuery.error.message}</ErrorState>
          ) : feed.length === 0 ? (
            <EmptyState testId="activity-empty">No recent events yet.</EmptyState>
          ) : (
            <ul className="activity-list ui-row-list" data-testid="activity-list">
              {visibleFeed.map((entry) => (
                <FeedActivityRow key={entry.id} entry={entry} />
              ))}
            </ul>
          )}
        </Section>
      ) : null}

      {showRewards ? (
        <Section className="activity-section" data-testid="activity-claimable" divider={false}>
          <button
            type="button"
            className="activity-section-toggle"
            aria-expanded={rewardsOpen}
            onClick={() => setRewardsOpen((current) => !current)}
          >
            <h3 className="type-section-heading">
              Claimable Rewards
              <CounterBadge count={1} tone="accent" />
            </h3>
            <span className="activity-chevron" aria-hidden="true">
              {rewardsOpen ? '^' : 'v'}
            </span>
          </button>
          {rewardsOpen ? (
            <NotificationRow
              as="div"
              variant="reward"
              unread
              icon={<img src={activityIconUrl('EXPEDITION_COMPLETED')} alt="" />}
              primary={
                <>
                  Expedition complete. Claim your <span className="activity-hl-blue">rewards</span>.
                </>
              }
              action={
                <Link to={gameLink('expeditions')} className="activity-claim" data-testid="rail-claim-expedition">
                  Claim
                </Link>
              }
            />
          ) : null}
        </Section>
      ) : null}

      {showAlerts ? (
        <Section className="activity-section" data-testid="activity-alerts" divider={false}>
          <button
            type="button"
            className="activity-section-toggle"
            aria-expanded={alertsOpen}
            onClick={() => setAlertsOpen((current) => !current)}
          >
            <h3 className="type-section-heading">Alerts</h3>
            <span className="activity-chevron" aria-hidden="true">
              {alertsOpen ? '^' : 'v'}
            </span>
          </button>
          {alertsOpen ? (
            <ul className="activity-list ui-row-list">
              {combatActive ? (
                <NotificationRow
                  variant="warning"
                  unread
                  icon={<img src={activityIconUrl('alert')} alt="" />}
                  primary="Combat is in progress."
                />
              ) : null}
              {encounterActive ? (
                <NotificationRow
                  variant="warning"
                  unread
                  icon={<img src={activityIconUrl('alert')} alt="" />}
                  primary="An encounter is waiting."
                />
              ) : null}
            </ul>
          ) : null}
        </Section>
      ) : null}

      {canExpand && showEvents ? (
        <Button
          type="button"
          variant="ghost"
          className="activity-view-all"
          data-testid="activity-view-all"
          onClick={() => setExpanded((current) => !current)}
        >
          {expanded ? 'Collapse' : 'View All Notifications'}
        </Button>
      ) : null}
    </Panel>
  )
}

function FeedActivityRow({ entry }: { entry: ActivityEntryResponse }) {
  return (
    <ActivityRow
      variant={activityRowVariant(entry.type)}
      testId={`activity-${entry.type}`}
      icon={<img src={activityIconUrl(entry.type)} alt="" />}
      primary={activityMessageParts(entry.type, entry.message).map((part, index) =>
        part.tone === 'plain' ? (
          part.text
        ) : (
          <span key={`${entry.id}-${index}`} className={`activity-hl-${part.tone}`}>
            {part.text}
          </span>
        ),
      )}
      metadata={<time dateTime={entry.createdAt}>{formatRelativeTime(entry.createdAt)}</time>}
    />
  )
}
