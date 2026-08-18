import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchQuests, turnInQuest } from '../api/quests'
import type { QuestResponse } from '../api/types'
import { Button } from '../ui/Button'
import { useToast } from '../ui/ToastRegion'
import { aimsAtBren } from '../quest/issuedSteel'

type Props = {
  locationCode?: string
  onAimBren?: () => void
  onSearchEncounter?: () => void
  onOpenWorld?: () => void
}

export function contextualQuest(quests: QuestResponse[], locationCode?: string): QuestResponse | null {
  if (!locationCode) {
    return null
  }
  return (
    quests.find(
      (quest) =>
        quest.tracked &&
        quest.status !== 'COMPLETED' &&
        quest.actionLocationCode === locationCode,
    ) ?? null
  )
}

export function actionLabel(quest: QuestResponse): string {
  if (quest.status === 'READY_TO_TURN_IN') {
    return 'Claim Rewards'
  }
  if (quest.actionHint === 'TALK') {
    return 'Talk'
  }
  if (quest.actionHint === 'SEARCH') {
    return 'Investigate'
  }
  if (quest.actionHint === 'FIGHT') {
    return 'Fight'
  }
  return 'Continue'
}

export function LocationQuestAction({ locationCode, onAimBren, onSearchEncounter, onOpenWorld }: Props) {
  const queryClient = useQueryClient()
  const toast = useToast()
  const questsQuery = useQuery({
    queryKey: ['quests'],
    queryFn: fetchQuests,
    retry: false,
  })
  const quest = contextualQuest(questsQuery.data?.quests ?? [], locationCode)
  const turnInMutation = useMutation({
    mutationFn: (code: string) => turnInQuest(code),
    onSuccess: async (completed) => {
      toast.notify(`Quest Complete — ${completed.name}`)
      await queryClient.invalidateQueries({ queryKey: ['quests'] })
      await queryClient.invalidateQueries({ queryKey: ['activity'] })
      await queryClient.invalidateQueries({ queryKey: ['character'] })
      await queryClient.invalidateQueries({ queryKey: ['inventory'] })
    },
  })

  if (!quest) {
    return null
  }

  const objective = quest.objectives.find((item) => !item.completed)
  const title =
    quest.status === 'READY_TO_TURN_IN'
      ? `Return to ${quest.turnInNpcName ?? 'the quest giver'}`
      : (objective?.displayText ?? quest.name)

  async function handleAction() {
    if (quest.status === 'READY_TO_TURN_IN') {
      try {
        await turnInMutation.mutateAsync(quest.code)
      } catch (error) {
        if (error instanceof ApiError && error.code === 'QUEST_WRONG_LOCATION') {
          onAimBren?.()
        }
      }
      return
    }
    if (quest.actionHint === 'TALK' || aimsAtBren(quest)) {
      onAimBren?.()
      return
    }
    if (quest.actionHint === 'SEARCH' || quest.actionHint === 'FIGHT') {
      onSearchEncounter?.()
      return
    }
    onOpenWorld?.()
  }

  return (
    <div className="location-quest-action" data-testid="location-quest-action">
      <p className="type-micro">Quest objective</p>
      <p data-testid="location-quest-objective">{title}</p>
      <Button
        data-testid="location-quest-cta"
        loading={turnInMutation.isPending}
        onClick={() => void handleAction()}
      >
        {actionLabel(quest)}
      </Button>
    </div>
  )
}
