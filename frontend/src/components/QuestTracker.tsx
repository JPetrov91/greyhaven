import { useQuery } from '@tanstack/react-query'
import { fetchQuests } from '../api/quests'
import type { QuestResponse } from '../api/types'
import { gameLink } from '../ui/gameNav'
import { Link } from 'react-router-dom'
import { Panel } from '../ui/Panel'

type Props = {
  locationCode?: string
  onOpenTalk?: () => void
}

export function isBrenTalkObjective(quest: QuestResponse): boolean {
  if (quest.code !== 'QST_MILITIA_NOTICE') {
    return false
  }
  if (quest.status === 'READY_TO_TURN_IN') {
    return true
  }
  const objective = quest.objectives.find((item) => !item.completed)
  return objective?.type === 'TALK_TO_NPC'
}

export function QuestTracker({ locationCode, onOpenTalk }: Props) {
  const questsQuery = useQuery({
    queryKey: ['quests'],
    queryFn: fetchQuests,
    retry: false,
  })
  const tracked = (questsQuery.data?.quests ?? [])
    .filter((quest) => quest.tracked && quest.status !== 'COMPLETED')
    .slice(0, 3)

  return (
    <Panel title="Tracked quests" data-testid="quest-tracker">
      {tracked.length === 0 ? (
        <p className="muted">
          Nothing tracked.{' '}
          <Link to={gameLink('quests')} data-testid="open-quest-log">
            Open Quest Log
          </Link>
        </p>
      ) : (
        <ul className="quest-tracker-list">
          {tracked.map((quest) => {
            const objective = quest.objectives.find((item) => !item.completed) ?? quest.objectives[0]
            const ready = quest.status === 'READY_TO_TURN_IN'
            const canTalk = locationCode === 'CITY_SQUARE' && isBrenTalkObjective(quest) && onOpenTalk
            return (
              <li key={quest.code} data-testid={`tracked-quest-${quest.code}`}>
                {canTalk ? (
                  <button type="button" data-testid={`tracked-talk-${quest.code}`} onClick={onOpenTalk}>
                    <strong>{quest.name}</strong>
                    {ready && quest.turnInNpcName ? (
                      <p data-testid={`tracked-return-${quest.code}`}>Return to {quest.turnInNpcName}</p>
                    ) : objective ? (
                      <p>
                        {objective.displayText}
                        {objective.requiredAmount > 1
                          ? ` ${objective.currentAmount}/${objective.requiredAmount}`
                          : ''}
                      </p>
                    ) : null}
                  </button>
                ) : (
                  <>
                    <strong>{quest.name}</strong>
                    {ready && quest.turnInNpcName ? (
                      <p data-testid={`tracked-return-${quest.code}`}>Return to {quest.turnInNpcName}</p>
                    ) : objective ? (
                      <p>
                        {objective.displayText}
                        {objective.requiredAmount > 1
                          ? ` ${objective.currentAmount}/${objective.requiredAmount}`
                          : ''}
                      </p>
                    ) : null}
                  </>
                )}
              </li>
            )
          })}
        </ul>
      )}
    </Panel>
  )
}
