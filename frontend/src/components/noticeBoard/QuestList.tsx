import type { QuestBoardEntryResponse } from '../../api/types'
import { EmptyState } from '../../ui/EmptyState'
import { QuestListItem } from './QuestListItem'

type Props = {
  quests: QuestBoardEntryResponse[]
  selectedQuestId: string | null
  onSelect: (code: string) => void
}

export function QuestList({ quests, selectedQuestId, onSelect }: Props) {
  if (quests.length === 0) {
    return (
      <EmptyState testId="notice-board-empty">
        No new notices have been posted. Check back later.
      </EmptyState>
    )
  }
  return (
    <ul className="ui-row-list notice-board-list" data-testid="notice-board-list">
      {quests.map((quest) => (
        <li key={quest.code}>
          <QuestListItem quest={quest} selected={quest.code === selectedQuestId} onSelect={onSelect} />
        </li>
      ))}
    </ul>
  )
}
