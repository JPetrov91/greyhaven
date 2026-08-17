import type { QuestBoardEntryResponse } from '../../api/types'
import { GenericRow } from '../../ui/GenericRow'
import { boardEntryDisabled, formatQuestType, questTypeArtUrl, questTypeTone, rewardChips } from './questBoardUtils'

type Props = {
  quest: QuestBoardEntryResponse
  selected: boolean
  onSelect: (code: string) => void
}

export function QuestListItem({ quest, selected, onSelect }: Props) {
  const disabled = boardEntryDisabled(quest)
  const chips = rewardChips(quest.rewards)
  const tone = questTypeTone(quest.questType)
  return (
    <GenericRow
      as="button"
      className={`notice-board-row notice-board-row--${tone}`}
      testId={`notice-quest-${quest.code}`}
      selected={selected}
      interactive={!disabled}
      tone={quest.listState === 'UNAVAILABLE' ? 'secondary' : 'default'}
      onClick={() => {
        if (!disabled) {
          onSelect(quest.code)
        }
      }}
      icon={
        <img className={`notice-board-crest notice-board-crest--${tone}`} src={questTypeArtUrl(quest.questType)} alt="" />
      }
      primary={
        <span className="notice-board-row-title">
          <span>{quest.name}</span>
          <span className="notice-board-level">Level {quest.recommendedLevel}</span>
        </span>
      }
      secondary={
        <>
          <span className="notice-board-type">{formatQuestType(quest.questType)}</span>
          {quest.shortDescription ? <span className="notice-board-blurb">{quest.shortDescription}</span> : null}
        </>
      }
      metadata={
        <span className="notice-board-rewards" data-testid={`notice-rewards-${quest.code}`}>
          <span className="notice-board-rewards-label">Rewards</span>
          {chips.map((chip) => (
            <span
              key={chip.key}
              className={`notice-board-chip notice-board-chip--${chip.kind.toLowerCase()}`}
              aria-label={`${chip.label} ${chip.kind === 'GOLD' ? 'Gold' : chip.kind}`}
            >
              {chip.iconUrl ? <img src={chip.iconUrl} alt="" /> : null}
              <strong>{chip.label}</strong>
            </span>
          ))}
        </span>
      }
    />
  )
}
