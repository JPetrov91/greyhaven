import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { acceptQuest, fetchQuest } from '../../api/quests'
import { fetchQuestBoard } from '../../api/world'
import type { QuestBoardEntryResponse } from '../../api/types'
import { ErrorState } from '../../ui/ErrorState'
import { LoadingState } from '../../ui/LoadingState'
import { useToast } from '../../ui/ToastRegion'
import { QuestList } from './QuestList'
import { QuestPreview } from './QuestPreview'

type Mode = 'LIST' | 'PREVIEW'

type Props = {
  locationCode: string
  open: boolean
  onClose: () => void
  onOpenTalk?: () => void
}

export function NoticeBoard({ locationCode, open, onClose }: Props) {
  const queryClient = useQueryClient()
  const toast = useToast()
  const [mode, setMode] = useState<Mode>('LIST')
  const [selectedQuestId, setSelectedQuestId] = useState<string | null>(null)
  const [filter, setFilter] = useState('ALL')
  const [acceptError, setAcceptError] = useState<string | null>(null)

  const boardQuery = useQuery({
    queryKey: ['quest-board', locationCode],
    queryFn: () => fetchQuestBoard(locationCode),
    enabled: open,
    retry: false,
  })

  const previewQuery = useQuery({
    queryKey: ['quest', selectedQuestId],
    queryFn: () => fetchQuest(selectedQuestId!),
    enabled: open && mode === 'PREVIEW' && selectedQuestId != null,
    retry: false,
  })

  const acceptMutation = useMutation({
    mutationFn: (code: string) => acceptQuest(code),
    onSuccess: async (quest) => {
      toast.notify(`Quest Accepted — ${quest.name}`)
      await queryClient.invalidateQueries({ queryKey: ['quests'] })
      await queryClient.invalidateQueries({ queryKey: ['quest-board'] })
      await queryClient.invalidateQueries({ queryKey: ['activity'] })
      await queryClient.invalidateQueries({ queryKey: ['character'] })
      setMode('LIST')
      setSelectedQuestId(null)
      onClose()
    },
    onError: (error) => {
      if (error instanceof ApiError) {
        if (error.code === 'QUEST_ALREADY_ACCEPTED') {
          void queryClient.invalidateQueries({ queryKey: ['quests'] })
          setAcceptError('You already accepted that quest.')
          return
        }
        setAcceptError(error.message)
        if (error.code === 'QUEST_NOT_AVAILABLE') {
          void boardQuery.refetch()
        }
        return
      }
      setAcceptError('Unable to accept this quest.')
    },
  })

  const quests = boardQuery.data?.quests ?? []
  const types = useMemo(() => {
    const unique = new Set(quests.map((quest) => quest.questType))
    return ['ALL', ...unique]
  }, [quests])
  const visible = quests.filter((quest) => filter === 'ALL' || quest.questType === filter)

  if (!open) {
    return null
  }

  function selectQuest(code: string) {
    setAcceptError(null)
    setSelectedQuestId(code)
    setMode('PREVIEW')
  }

  function closePreview() {
    setAcceptError(null)
    setSelectedQuestId(null)
    setMode('LIST')
  }

  return (
    <aside
      className={`notice-board notice-board--${mode.toLowerCase()}`}
      data-testid="notice-board"
      data-mode={mode}
    >
      <span className="notice-board-corner notice-board-corner-bl" aria-hidden="true" />
      <span className="notice-board-corner notice-board-corner-br" aria-hidden="true" />
      <header className="notice-board-header">
        <div className="notice-board-titleblock">
          <h3>Notice Board</h3>
          <p>Available Quests</p>
        </div>
        <div className="notice-board-toolbar">
          <label className="notice-board-filter">
            <span>Filter</span>
            <select
              data-testid="notice-board-filter"
              value={filter}
              onChange={(event) => setFilter(event.target.value)}
            >
              {types.map((type) => (
                <option key={type} value={type}>
                  {type === 'ALL' ? 'All' : type}
                </option>
              ))}
            </select>
          </label>
          <button type="button" className="notice-board-close" data-testid="notice-board-close" onClick={onClose}>
            Close
          </button>
        </div>
      </header>
      <div className="notice-board-workspace">
        {mode === 'PREVIEW' ? (
          <QuestPreview
            quest={previewQuery.data}
            loading={previewQuery.isPending}
            accepting={acceptMutation.isPending}
            acceptError={acceptError}
            onAccept={() => {
              if (selectedQuestId && !acceptMutation.isPending) {
                setAcceptError(null)
                acceptMutation.mutate(selectedQuestId)
              }
            }}
            onClose={closePreview}
          />
        ) : null}
        <div className="notice-board-list-pane">
          {boardQuery.isPending ? (
            <LoadingState>Checking the board…</LoadingState>
          ) : boardQuery.isError ? (
            <ErrorState onRetry={() => void boardQuery.refetch()}>
              The notice board could not be loaded.
            </ErrorState>
          ) : (
            <QuestList quests={visible as QuestBoardEntryResponse[]} selectedQuestId={selectedQuestId} onSelect={selectQuest} />
          )}
        </div>
      </div>
    </aside>
  )
}
