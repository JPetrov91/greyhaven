import type { QuestResponse } from '../../api/types'
import { Button } from '../../ui/Button'
import { LoadingState } from '../../ui/LoadingState'
import { npcPortraitUrl } from '../../ui/npcMedia'
import { formatDifficulty, formatQuestType, questArtworkUrl, rewardChips } from './questBoardUtils'

type Props = {
  quest: QuestResponse | undefined
  loading: boolean
  accepting: boolean
  acceptError: string | null
  onAccept: () => void
  onClose: () => void
}

export function QuestPreview({ quest, loading, accepting, acceptError, onAccept, onClose }: Props) {
  if (loading || !quest) {
    return (
      <div className="notice-preview-body" data-testid="notice-preview-loading">
        <LoadingState>Loading notice…</LoadingState>
      </div>
    )
  }

  const artwork = questArtworkUrl(quest)
  const available = quest.status === 'AVAILABLE'
  const rewards = rewardChips(quest.rewards)

  return (
    <div className="notice-preview-body" data-testid={`notice-preview-${quest.code}`}>
      <header className="notice-preview-header">
        <h3>{quest.name}</h3>
        <p className="muted">
          {formatQuestType(quest.questType ?? quest.category)}
          {' · '}
          Recommended Level: {quest.recommendedLevel}
        </p>
      </header>
      {artwork ? (
        <div
          className="notice-preview-art"
          data-testid="notice-preview-art"
          style={{ backgroundImage: `url(${artwork})` }}
        />
      ) : null}
      {quest.startNpcName ? (
        <div className="notice-preview-giver">
          {quest.startNpcCode ? (
            <img
              src={npcPortraitUrl(quest.startNpcCode.toLowerCase().replaceAll('_', '-'))}
              alt=""
              width={48}
              height={48}
            />
          ) : null}
          <div>
            <p className="type-micro">Quest giver</p>
            <p>{quest.startNpcName}</p>
          </div>
        </div>
      ) : null}
      <p>{quest.description}</p>
      <section>
        <h4>Objectives</h4>
        <ul className="notice-preview-objectives">
          {quest.objectives.map((objective) => (
            <li key={`${objective.type}-${objective.targetCode}-${objective.displayText}`}>
              ◇ {objective.displayText} 0/{objective.requiredAmount}
            </li>
          ))}
        </ul>
      </section>
      <div className="notice-preview-meta">
        <div>
          <p className="type-micro">Location</p>
          <p>
            {quest.locationName ?? 'Unknown'}
            {quest.regionName ? `, ${quest.regionName}` : ''}
          </p>
        </div>
        <div>
          <p className="type-micro">Difficulty</p>
          <p data-testid="notice-preview-difficulty">{formatDifficulty(quest.difficulty)}</p>
        </div>
      </div>
      <section>
        <h4>Rewards</h4>
        <ul className="notice-preview-rewards">
          {rewards.map((reward) => (
            <li key={reward}>{reward}</li>
          ))}
        </ul>
      </section>
      {acceptError ? (
        <p className="notice-preview-error" role="alert">
          {acceptError}
        </p>
      ) : null}
      <div className="notice-preview-actions">
        <Button
          data-testid="notice-accept"
          disabled={!available || accepting}
          onClick={onAccept}
        >
          {accepting ? 'Accepting…' : available ? 'Accept Quest' : quest.status === 'ACTIVE' ? 'Already accepted' : 'Unavailable'}
        </Button>
        <Button data-testid="notice-close-preview" variant="ghost" onClick={onClose}>
          Close
        </Button>
      </div>
    </div>
  )
}
