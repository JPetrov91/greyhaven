import { useQuery } from '@tanstack/react-query'
import { fetchQuests } from '../api/quests'
import { gameLink } from '../ui/gameNav'
import { Link } from 'react-router-dom'
import { Panel } from '../ui/Panel'

export function QuestTracker() {
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
            return (
              <li key={quest.code} data-testid={`tracked-quest-${quest.code}`}>
                <strong>{quest.name}</strong>
                {ready && quest.turnInNpcName ? (
                  <p data-testid={`tracked-return-${quest.code}`}>Return to {quest.turnInNpcName}</p>
                ) : objective ? (
                  <p>
                    {objective.displayText} {objective.currentAmount}/{objective.requiredAmount}
                  </p>
                ) : null}
              </li>
            )
          })}
        </ul>
      )}
    </Panel>
  )
}
