import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchQuests, trackQuest, untrackQuest } from '../api/quests'
import { ApiError } from '../api/client'
import type { QuestResponse } from '../api/types'
import { Button } from '../ui/Button'
import { GenericRow } from '../ui/GenericRow'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { Tabs } from '../ui/Tabs'
import { ISSUED_STEEL_CODE } from '../quest/issuedSteel'

type Tab = 'AVAILABLE' | 'ACTIVE' | 'COMPLETED'

function currentObjective(quest: QuestResponse) {
  return quest.objectives.find((objective) => !objective.completed) ?? quest.objectives[quest.objectives.length - 1]
}

export function rewardPreview(quest: QuestResponse): string {
  return quest.rewards
    .map((reward) => {
      if (reward.kind === 'XP') {
        return `${reward.amount} XP`
      }
      if (reward.kind === 'GOLD') {
        return `${reward.amount} gold`
      }
      if (reward.kind === 'ITEM') {
        return reward.itemName ?? reward.itemCode
      }
      return reward.unlockCode
    })
    .filter(Boolean)
    .join(' · ')
}

export function recommendedLocation(quest: QuestResponse): string | null {
  if (quest.status === 'COMPLETED' || quest.status === 'AVAILABLE') {
    return null
  }
  if (quest.locationName) {
    return quest.locationName
  }
  if (quest.status === 'READY_TO_TURN_IN') {
    return 'City Square'
  }
  const objective = currentObjective(quest)
  if (objective?.targetCode === 'OLD_TOWN') {
    return 'Old Town'
  }
  if (objective?.type === 'TALK_TO_NPC' || objective?.targetCode === 'MILITIA_OFFICER') {
    return 'City Square'
  }
  return quest.actionLocationCode ?? null
}

function objectiveLine(quest: QuestResponse): string | null {
  const objective = currentObjective(quest)
  if (!objective || quest.status === 'AVAILABLE') {
    return null
  }
  if (quest.status === 'COMPLETED') {
    return null
  }
  if (objective.requiredAmount <= 1) {
    return objective.displayText
  }
  return `${objective.displayText} ${objective.currentAmount}/${objective.requiredAmount}`
}

type Props = {
  onAimBren?: () => void
}

export function QuestLogPanel({ onAimBren }: Props) {
  const queryClient = useQueryClient()
  const [tabOverride, setTabOverride] = useState<Tab | null>(null)
  const questsQuery = useQuery({
    queryKey: ['quests'],
    queryFn: fetchQuests,
    retry: false,
  })

  const trackMutation = useMutation({
    mutationFn: async ({ code, tracked }: { code: string; tracked: boolean }) =>
      tracked ? untrackQuest(code) : trackQuest(code),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['quests'] })
    },
  })

  if (questsQuery.isLoading) {
    return (
      <Panel title="Quest Log" data-testid="quest-log">
        <LoadingState>Loading quests…</LoadingState>
      </Panel>
    )
  }
  if (questsQuery.error instanceof ApiError) {
    return (
      <Panel title="Quest Log" data-testid="quest-log">
        <ErrorState onRetry={() => void questsQuery.refetch()}>{questsQuery.error.message}</ErrorState>
      </Panel>
    )
  }

  const quests = questsQuery.data?.quests ?? []
  const hasActive = quests.some((quest) => quest.status === 'ACTIVE' || quest.status === 'READY_TO_TURN_IN')
  const tab = tabOverride ?? (hasActive ? 'ACTIVE' : 'AVAILABLE')
  const visible = quests.filter((quest) => {
    if (tab === 'ACTIVE') {
      return quest.status === 'ACTIVE' || quest.status === 'READY_TO_TURN_IN'
    }
    return quest.status === tab
  })
  const actionError = trackMutation.error instanceof ApiError ? trackMutation.error.message : null

  return (
    <Panel title="Quest Log" data-testid="quest-log">
      <Tabs<Tab>
        label="Quest status"
        testId="quest-log-tabs"
        value={tab}
        onChange={setTabOverride}
        tabs={[
          { id: 'AVAILABLE', label: 'Available' },
          { id: 'ACTIVE', label: 'Active' },
          { id: 'COMPLETED', label: 'Completed' },
        ]}
      />
      {visible.length === 0 ? (
        <EmptyState>No quests in this list.</EmptyState>
      ) : (
        <ul className="ui-row-list quest-log-list" data-testid="quest-log-list">
          {visible.map((quest) => {
            const place = recommendedLocation(quest)
            const line = objectiveLine(quest)
            const canTrack = quest.status === 'ACTIVE' || quest.status === 'READY_TO_TURN_IN'
            return (
              <GenericRow
                key={quest.code}
                as="li"
                testId={`quest-${quest.code}`}
                primary={quest.name}
                action={
                  canTrack ? (
                    <Button
                      type="button"
                      variant="ghost"
                      data-testid={`track-quest-${quest.code}`}
                      onClick={() => trackMutation.mutate({ code: quest.code, tracked: quest.tracked })}
                    >
                      {quest.tracked ? 'Untrack' : 'Track'}
                    </Button>
                  ) : null
                }
                secondary={
                  <>
                    <span>
                      Lv {quest.recommendedLevel}
                      {quest.startNpcName ? ` · ${quest.startNpcName}` : ''}
                    </span>
                    {quest.status === 'AVAILABLE' && quest.startNpcName ? (
                      <span data-testid={`quest-hint-${quest.code}`}>Talk to {quest.startNpcName} to accept</span>
                    ) : null}
                    {quest.status === 'READY_TO_TURN_IN' && quest.turnInNpcName ? (
                      <span data-testid={`quest-hint-${quest.code}`}>Return to {quest.turnInNpcName}</span>
                    ) : null}
                    {quest.status === 'ACTIVE' || quest.status === 'READY_TO_TURN_IN' ? (
                      <span data-testid={`quest-description-${quest.code}`}>{quest.description}</span>
                    ) : null}
                    {quest.status === 'COMPLETED' && quest.completeText ? (
                      <span data-testid={`quest-complete-text-${quest.code}`}>{quest.completeText}</span>
                    ) : null}
                    {line ? <span data-testid={`quest-objective-${quest.code}`}>{line}</span> : null}
                    {place ? (
                      quest.code === ISSUED_STEEL_CODE && onAimBren ? (
                        <button
                          type="button"
                          className="quest-log-aim"
                          data-testid={`quest-recommended-${quest.code}`}
                          onClick={() => onAimBren()}
                        >
                          Recommended: {place}
                        </button>
                      ) : (
                        <span data-testid={`quest-recommended-${quest.code}`}>Recommended: {place}</span>
                      )
                    ) : null}
                    <span>{rewardPreview(quest)}</span>
                  </>
                }
              />
            )
          })}
        </ul>
      )}
      {actionError ? (
        <p className="form-error" role="alert">
          {actionError}
        </p>
      ) : null}
    </Panel>
  )
}
