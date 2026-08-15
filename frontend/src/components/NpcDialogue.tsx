import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
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

export function QuestCompleteSummary({ quest }: { quest: QuestResponse }) {
  const rewards = rewardPreview(quest)
  return (
    <div data-testid="quest-complete">
      <p>
        <strong>Quest Complete</strong> — {quest.name}
      </p>
      {rewards ? <p className="muted">{rewards}</p> : null}
      {quest.unlocks.length > 0 ? <p className="muted">Unlocked: {quest.unlocks.join(', ')}</p> : null}
      {quest.nextQuestName ? <p className="muted">Next: {quest.nextQuestName}</p> : null}
    </div>
  )
}

export function NpcDialogue({ open, onClose, onOpenMarket }: Props) {
  const queryClient = useQueryClient()
  const [completion, setCompletion] = useState<QuestResponse | null>(null)
  const npcsQuery = useQuery({
    queryKey: ['npcs'],
    queryFn: fetchNpcs,
    enabled: open,
    retry: false,
  })
  const talkMutation = useMutation({
    mutationFn: ({ code, questCode }: { code: string; questCode?: string }) => talkToNpc(code, questCode),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['quests'] })
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
    },
  })

  if (!open) {
    return null
  }

  const talk = talkMutation.data
  const npcs = npcsQuery.data?.npcs ?? []

  async function handleAction(talkView: NpcTalkResponse, type: string, questCode: string | null) {
    if (type === 'CLOSE') {
      onClose()
      return
    }
    if (type === 'SHOP') {
      onOpenMarket?.()
      onClose()
      return
    }
    if (type === 'ACCEPT' && questCode) {
      await acceptMutation.mutateAsync(questCode)
      talkMutation.mutate({ code: talkView.code, questCode })
      return
    }
    if (type === 'TURN_IN' && questCode) {
      await turnInMutation.mutateAsync(questCode)
      talkMutation.mutate({ code: talkView.code, questCode })
    }
  }

  return (
    <div className="npc-dialogue" data-testid="npc-dialogue">
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
      {talk ? (
        <div data-testid="npc-talk">
          {npcPortraitUrl(talk.portraitCode) ? (
            <img src={npcPortraitUrl(talk.portraitCode)} alt="" width={72} height={72} />
          ) : null}
          <p>
            <strong>{talk.name}</strong> · {talk.title}
          </p>
          <p data-testid="npc-talk-text">{talk.text}</p>
          {completion ? <QuestCompleteSummary quest={completion} /> : null}
          <div className="npc-talk-actions">
            {talk.actions.map((action) => (
              <Button
                key={`${action.type}-${action.questCode ?? action.merchantCode ?? 'x'}`}
                type="button"
                data-testid={`npc-action-${action.type}`}
                onClick={() => void handleAction(talk, action.type, action.questCode)}
              >
                {action.label}
              </Button>
            ))}
          </div>
        </div>
      ) : null}
      {talkMutation.error instanceof ApiError ? <p className="form-error">{talkMutation.error.message}</p> : null}
      <Button type="button" onClick={onClose}>
        Close
      </Button>
    </div>
  )
}
