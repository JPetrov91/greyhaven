import type { QuestBoardEntryResponse } from '../../api/types'
import { GenericRow } from '../../ui/GenericRow'
import { IconWell } from '../../ui/IconWell'
import { ChromeIcon } from '../../ui/chromeIcons'
import { boardEntryDisabled, formatQuestType, rewardChips } from './questBoardUtils'

type Props = {
  quest: QuestBoardEntryResponse
  selected: boolean
  onSelect: (code: string) => void
}

export function QuestListItem({ quest, selected, onSelect }: Props) {
  const disabled = boardEntryDisabled(quest)
  const chips = rewardChips(quest.rewards)
  return (
    <GenericRow
      as="button"
      className="notice-board-row"
      testId={`notice-quest-${quest.code}`}
      selected={selected}
      interactive={!disabled}
      tone={quest.listState === 'UNAVAILABLE' ? 'secondary' : 'default'}
      disabled={disabled}
      onClick={() => {
        if (!disabled) {
          onSelect(quest.code)
        }
      }}
      icon={
        <IconWell active={selected}>
          <ChromeIcon name="quests" />
        </IconWell>
      }
      primary={quest.name}
      secondary={
        <>
          <span>{formatQuestType(quest.questType)}</span>
          <span>Level {quest.recommendedLevel}</span>
          {quest.shortDescription ? <span className="notice-board-blurb">{quest.shortDescription}</span> : null}
        </>
      }
      metadata={
        <span className="notice-board-rewards" data-testid={`notice-rewards-${quest.code}`}>
          {chips.join(' · ')}
        </span>
      }
    />
  )
}
