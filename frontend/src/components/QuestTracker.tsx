import { useEffect, useRef } from 'react'
import { useQuery } from '@tanstack/react-query'
import { fetchQuests } from '../api/quests'
import type { QuestResponse } from '../api/types'
import { ChromeIcon } from '../ui/chromeIcons'
import { gameLink } from '../ui/gameNav'
import { GenericRow } from '../ui/GenericRow'
import { IconWell } from '../ui/IconWell'
import { useToast } from '../ui/ToastRegion'
import { Link } from 'react-router-dom'
import { Panel } from '../ui/Panel'

const TRACK_SLOTS = 3

type Props = {
  locationCode?: string
  onOpenTalk?: () => void
}

export function isContextualTalk(quest: QuestResponse, locationCode?: string): boolean {
  if (!locationCode || quest.actionLocationCode !== locationCode) {
    return false
  }
  return quest.status === 'READY_TO_TURN_IN' || quest.actionHint === 'TALK'
}

function trackerObjective(quest: QuestResponse) {
  const objective = quest.objectives.find((item) => !item.completed) ?? quest.objectives[0]
  const ready = quest.status === 'READY_TO_TURN_IN'
  if (ready && quest.turnInNpcName) {
    return (
      <span data-testid={`tracked-return-${quest.code}`}>
        ✓ Objectives Complete · Return to {quest.turnInNpcName}
      </span>
    )
  }
  if (!objective) {
    return null
  }
  return (
    <>
      {objective.displayText}
      {objective.requiredAmount > 1 ? ` ${objective.currentAmount}/${objective.requiredAmount}` : ''}
    </>
  )
}

function trackerMeta(quest: QuestResponse, canTalk: boolean) {
  if (canTalk) {
    return 'Talk'
  }
  if (quest.status === 'READY_TO_TURN_IN') {
    return 'Report'
  }
  return 'Active'
}

function progressKey(quest: QuestResponse): string {
  const completed = quest.objectives.filter((objective) => objective.completed).length
  return `${quest.status}:${completed}`
}

export function QuestTracker({ locationCode, onOpenTalk }: Props) {
  const toast = useToast()
  const previous = useRef<Map<string, string>>(new Map())
  const questsQuery = useQuery({
    queryKey: ['quests'],
    queryFn: fetchQuests,
    retry: false,
  })
  const tracked = (questsQuery.data?.quests ?? [])
    .filter((quest) => quest.tracked && quest.status !== 'COMPLETED')
    .slice(0, TRACK_SLOTS)
  const emptySlots = Math.max(0, TRACK_SLOTS - tracked.length)

  useEffect(() => {
    const next = new Map<string, string>()
    for (const quest of tracked) {
      const key = progressKey(quest)
      next.set(quest.code, key)
      const prior = previous.current.get(quest.code)
      if (prior && prior !== key) {
        const objective = quest.objectives.find((item) => !item.completed)
        if (quest.status === 'READY_TO_TURN_IN') {
          toast.notify(`Quest Ready to Turn In — ${quest.name}`)
        } else if (objective) {
          toast.notify(`Quest Updated — ${quest.name}: ${objective.displayText}`)
        }
      }
    }
    previous.current = next
  }, [tracked, toast])

  return (
    <Panel title="Tracked quests" className="quest-tracker-card" data-testid="quest-tracker">
      {tracked.length === 0 ? (
        <p className="muted">
          Nothing tracked.{' '}
          <Link to={gameLink('quests')} data-testid="open-quest-log">
            Open Quest Log
          </Link>
        </p>
      ) : (
        <>
          <ul className="quest-tracker-list">
            {tracked.map((quest) => {
              const canTalk = Boolean(isContextualTalk(quest, locationCode) && onOpenTalk)
              return (
                <li key={quest.code} data-testid={`tracked-quest-${quest.code}`}>
                  <GenericRow
                    as={canTalk ? 'button' : 'div'}
                    className="quest-tracker-entry surface-interactive"
                    testId={canTalk ? `tracked-talk-${quest.code}` : undefined}
                    interactive={canTalk}
                    icon={
                      <IconWell active={canTalk || quest.status === 'READY_TO_TURN_IN'}>
                        <ChromeIcon name="quests" />
                      </IconWell>
                    }
                    primary={quest.name}
                    secondary={trackerObjective(quest)}
                    metadata={trackerMeta(quest, canTalk)}
                    onClick={canTalk ? onOpenTalk : undefined}
                  />
                </li>
              )
            })}
            {Array.from({ length: emptySlots }, (_, index) => (
              <li key={`empty-${index}`} className="quest-tracker-empty" data-testid="tracked-quest-empty">
                <span className="type-micro">Empty track</span>
              </li>
            ))}
          </ul>
          <Link to={gameLink('quests')} className="quest-tracker-log" data-testid="open-quest-log">
            Open Quest Log
          </Link>
        </>
      )}
    </Panel>
  )
}
