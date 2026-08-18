import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { acceptQuest, turnInQuest } from '../api/quests'
import { fetchNpcs, talkToNpc } from '../api/npcs'
import { ApiError } from '../api/client'
import type { NpcTalkResponse, QuestResponse } from '../api/types'
import { rewardPreview } from './QuestLogPanel'
import { Button } from '../ui/Button'
import { npcPortraitUrl } from '../ui/npcMedia'

type Props = {
  open: boolean
  onClose: () => void
  onOpenMarket?: () => void
  onOpenTravel?: () => void
  initialNpcCode?: string
  variant?: 'overlay' | 'dock'
}

export function questBadgeMark(badge: string): string {
  if (badge === 'AVAILABLE_QUEST') {
    return '!'
  }
  if (badge === 'TURN_IN') {
    return '?'
  }
  if (badge === 'ACTIVE') {
    return '…'
  }
  return ''
}

export function talkParagraphs(text: string): string[] {
  return text
    .split(/\n+/)
    .map((line) => line.trim())
    .filter((line) => line.length > 0)
}

export function QuestCompleteSummary({ quest }: { quest: QuestResponse }) {
  const rewards = rewardPreview(quest)
  return (
    <div className="quest-turn-in-preview" data-testid="quest-complete">
      <p className="type-micro">Quest Complete</p>
      <p>
        <strong>{quest.name}</strong>
      </p>
      {quest.completeText ? <p>{quest.completeText}</p> : null}
      {rewards ? (
        <p className="muted" data-testid="quest-complete-rewards">
          Rewards: {rewards}
        </p>
      ) : null}
      {quest.unlocks.length > 0 ? <p className="muted">Unlocked: {quest.unlocks.join(', ')}</p> : null}
      {quest.nextQuestName ? <p className="muted">Next: {quest.nextQuestName}</p> : null}
    </div>
  )
}

export function NpcDialogue({
  open,
  onClose,
  onOpenMarket,
  onOpenTravel,
  initialNpcCode,
  variant = 'overlay',
}: Props) {
  const queryClient = useQueryClient()
  const [completion, setCompletion] = useState<QuestResponse | null>(null)
  const dock = variant === 'dock'
  const npcsQuery = useQuery({
    queryKey: ['npcs'],
    queryFn: fetchNpcs,
    enabled: open && !dock && !initialNpcCode,
    retry: false,
  })
  const talkMutation = useMutation({
    mutationFn: ({
      code,
      questCode,
      action,
      kitFamily,
    }: {
      code: string
      questCode?: string
      action?: string
      kitFamily?: string
    }) => talkToNpc(code, questCode, action, kitFamily),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['quests'] })
      await queryClient.invalidateQueries({ queryKey: ['inventory'] })
      await queryClient.invalidateQueries({ queryKey: ['character'] })
      await queryClient.invalidateQueries({ queryKey: ['npcs'] })
    },
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
    onSuccess: async (quest) => {
      setCompletion(quest)
      await queryClient.invalidateQueries({ queryKey: ['quests'] })
      await queryClient.invalidateQueries({ queryKey: ['activity'] })
      await queryClient.invalidateQueries({ queryKey: ['character'] })
      await queryClient.invalidateQueries({ queryKey: ['inventory'] })
      await queryClient.invalidateQueries({ queryKey: ['npcs'] })
    },
  })

  useEffect(() => {
    if (!open) {
      talkMutation.reset()
      setCompletion(null)
      return
    }
    setCompletion(null)
    if (initialNpcCode) {
      talkMutation.mutate({ code: initialNpcCode })
    }
    // Talk is opened for a specific NPC from the Locations strip or tracker.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, initialNpcCode])

  if (!open) {
    return null
  }

  const talk = talkMutation.data
  const npcs = npcsQuery.data?.npcs ?? []
  const showDirectory = !dock && !initialNpcCode

  async function handleAction(talkView: NpcTalkResponse, action: NpcTalkResponse['actions'][number]) {
    if (action.type === 'CLOSE') {
      onClose()
      return
    }
    if (action.type === 'OPEN_TRAVEL') {
      onOpenTravel?.()
      if (!onOpenTravel) {
        onClose()
      }
      return
    }
    if (action.type === 'SHOP') {
      onOpenMarket?.()
      onClose()
      return
    }
    if (action.type === 'ACCEPT' && action.questCode) {
      await acceptMutation.mutateAsync(action.questCode)
      talkMutation.mutate({ code: talkView.code, questCode: action.questCode })
      return
    }
    if (action.type === 'TURN_IN' && action.questCode) {
      await turnInMutation.mutateAsync(action.questCode)
      talkMutation.mutate({ code: talkView.code, questCode: action.questCode })
      return
    }
    if (action.type === 'DIALOGUE') {
      talkMutation.mutate({
        code: talkView.code,
        questCode: action.questCode ?? undefined,
        action: action.action ?? undefined,
      })
      return
    }
    if (action.type === 'CHOOSE_KIT') {
      talkMutation.mutate({
        code: talkView.code,
        questCode: action.questCode ?? undefined,
        action: 'CHOOSE_KIT',
        kitFamily: action.action ?? undefined,
      })
    }
  }

  return (
    <div
      className={dock ? 'npc-dialogue npc-dialogue-dock' : 'npc-dialogue npc-dialogue-overlay'}
      data-testid="npc-dialogue"
      data-variant={variant}
    >
      {showDirectory ? (
        <>
          <h3>People here</h3>
          {npcs.length === 0 ? <p className="muted">No one to talk to.</p> : null}
          <ul>
            {npcs.map((npc) => {
              const mark = questBadgeMark(npc.questBadges[0] ?? '')
              return (
                <li key={npc.code}>
                  <Button
                    type="button"
                    data-testid={`talk-npc-${npc.code}`}
                    onClick={() => talkMutation.mutate({ code: npc.code })}
                  >
                    {npc.name}
                    {mark ? ` (${mark})` : ''}
                  </Button>
                </li>
              )
            })}
          </ul>
        </>
      ) : null}
      {talk ? (
        <div className="npc-talk-body" data-testid="npc-talk">
          {npcPortraitUrl(talk.portraitCode) ? (
            <img
              className={dock ? 'npc-talk-portrait npc-talk-portrait-dock' : 'npc-talk-portrait'}
              src={npcPortraitUrl(talk.portraitCode)}
              alt=""
              width={dock ? 96 : 72}
              height={dock ? 96 : 72}
            />
          ) : null}
          <p>
            <strong>{talk.name}</strong> · {talk.title}
          </p>
          <div data-testid="npc-talk-text">
            {talkParagraphs(talk.text).map((paragraph) => (
              <p key={paragraph}>{paragraph}</p>
            ))}
          </div>
          {completion ? <QuestCompleteSummary quest={completion} /> : null}
          <div className={dock ? 'npc-talk-actions npc-talk-actions-dock' : 'npc-talk-actions'}>
            {talk.actions.map((action) => (
              <Button
                key={`${action.type}-${action.action ?? action.questCode ?? action.merchantCode ?? action.label}`}
                type="button"
                className={action.hint ? 'npc-talk-reply' : undefined}
                data-testid={`npc-action-${action.type}${action.action ? `-${action.action}` : ''}`}
                onClick={() => void handleAction(talk, action)}
              >
                <span>{action.label}</span>
                {action.hint ? <span className="npc-talk-reply-hint">{action.hint}</span> : null}
              </Button>
            ))}
          </div>
        </div>
      ) : null}
      {talkMutation.error instanceof ApiError ? <p className="form-error">{talkMutation.error.message}</p> : null}
      {dock ? null : (
        <Button type="button" onClick={onClose}>
          Close
        </Button>
      )}
    </div>
  )
}
