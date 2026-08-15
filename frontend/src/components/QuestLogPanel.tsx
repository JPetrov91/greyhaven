import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { acceptQuest, fetchQuests, trackQuest, turnInQuest, untrackQuest } from '../api/quests'
import { ApiError } from '../api/client'
import type { QuestResponse } from '../api/types'
import { Button } from '../ui/Button'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { Tabs } from '../ui/Tabs'

type Tab = 'AVAILABLE' | 'ACTIVE' | 'COMPLETED'

function currentObjective(quest: QuestResponse) {
  return quest.objectives.find((objective) => !objective.completed) ?? quest.objectives[quest.objectives.length - 1]
}

function rewardPreview(quest: QuestResponse): string {
  return quest.rewards
    .map((reward) => {
      if (reward.kind === 'XP') {
        return `${reward.amount} XP`
      }
      if (reward.kind === 'GOLD') {
        return `${reward.amount} gold`
      }
      if (reward.kind === 'ITEM') {
        return reward.itemCode
      }
      return reward.unlockCode
    })
    .filter(Boolean)
    .join(' · ')
}

export function QuestLogPanel() {
  const queryClient = useQueryClient()
  const [tab, setTab] = useState<Tab>('AVAILABLE')
  const questsQuery = useQuery({
    queryKey: ['quests'],
    queryFn: fetchQuests,
    retry: false,
  })

  const acceptMutation = useMutation({
    mutationFn: (code: string) => acceptQuest(code),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['quests'] })
      await queryClient.invalidateQueries({ queryKey: ['activity'] })
      await queryClient.invalidateQueries({ queryKey: ['character'] })
    },
  })
  const turnInMutation = useMutation({
    mutationFn: (code: string) => turnInQuest(code),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['quests'] })
      await queryClient.invalidateQueries({ queryKey: ['activity'] })
      await queryClient.invalidateQueries({ queryKey: ['character'] })
      await queryClient.invalidateQueries({ queryKey: ['inventory'] })
    },
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
  const visible = quests.filter((quest) => {
    if (tab === 'ACTIVE') {
      return quest.status === 'ACTIVE' || quest.status === 'READY_TO_TURN_IN'
    }
    return quest.status === tab
  })
  const actionError =
    (acceptMutation.error instanceof ApiError && acceptMutation.error.message) ||
    (turnInMutation.error instanceof ApiError && turnInMutation.error.message) ||
    (trackMutation.error instanceof ApiError && trackMutation.error.message) ||
    null

  return (
    <Panel title="Quest Log" data-testid="quest-log">
      <Tabs<Tab>
        label="Quest status"
        testId="quest-log-tabs"
        value={tab}
        onChange={setTab}
        tabs={[
          { id: 'AVAILABLE', label: 'Available' },
          { id: 'ACTIVE', label: 'Active' },
          { id: 'COMPLETED', label: 'Completed' },
        ]}
      />
      {visible.length === 0 ? (
        <EmptyState>No quests in this list.</EmptyState>
      ) : (
        <ul className="quest-log-list" data-testid="quest-log-list">
          {visible.map((quest) => {
            const objective = currentObjective(quest)
            return (
              <li key={quest.code} data-testid={`quest-${quest.code}`}>
                <div>
                  <strong>{quest.name}</strong>
                  <p className="muted">
                    Lv {quest.recommendedLevel}
                    {quest.startNpcCode ? ` · ${quest.startNpcCode}` : ''}
                  </p>
                  {objective ? (
                    <p data-testid={`quest-objective-${quest.code}`}>
                      {objective.displayText} {objective.currentAmount}/{objective.requiredAmount}
                    </p>
                  ) : null}
                  <p className="muted">{rewardPreview(quest)}</p>
                </div>
                <div className="quest-log-actions">
                  {quest.status === 'AVAILABLE' ? (
                    <Button
                      type="button"
                      data-testid={`accept-quest-${quest.code}`}
                      onClick={() => acceptMutation.mutate(quest.code)}
                    >
                      Accept
                    </Button>
                  ) : null}
                  {quest.status === 'READY_TO_TURN_IN' ? (
                    <Button
                      type="button"
                      data-testid={`turn-in-quest-${quest.code}`}
                      onClick={() => turnInMutation.mutate(quest.code)}
                    >
                      Turn in
                    </Button>
                  ) : null}
                  {quest.status === 'ACTIVE' || quest.status === 'READY_TO_TURN_IN' ? (
                    <Button
                      type="button"
                      data-testid={`track-quest-${quest.code}`}
                      onClick={() => trackMutation.mutate({ code: quest.code, tracked: quest.tracked })}
                    >
                      {quest.tracked ? 'Untrack' : 'Track'}
                    </Button>
                  ) : null}
                </div>
              </li>
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
