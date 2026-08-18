import type { NpcResponse } from '../api/types'
import { questBadgeMark } from './NpcDialogue'
import { npcPortraitUrl } from '../ui/npcMedia'

type Props = {
  npcs: NpcResponse[]
  onTalk: (npcCode: string) => void
  selectedCode?: string
  onCloseTalk?: () => void
  leadNpcCode?: string
  pulseNpcCode?: string
  pulseLead?: boolean
}

export function NpcStrip({
  npcs,
  onTalk,
  selectedCode,
  onCloseTalk,
  leadNpcCode,
  pulseNpcCode,
  pulseLead,
}: Props) {
  if (npcs.length === 0) {
    return null
  }

  return (
    <section className="npc-strip" aria-label="People here" data-testid="npc-strip">
      <div className="npc-strip-head">
        <h3 className="npc-strip-heading">People here</h3>
        {onCloseTalk ? (
          <button
            type="button"
            className="npc-strip-close"
            data-testid="npc-strip-close"
            aria-label="Close talk"
            onClick={(event) => {
              event.stopPropagation()
              onCloseTalk()
            }}
          >
            ×
          </button>
        ) : null}
      </div>
      <div className="npc-strip-track">
        {npcs.map((npc) => {
          const mark = questBadgeMark(npc.questBadges[0] ?? '')
          const portrait = npcPortraitUrl(npc.portraitCode)
          const selected = npc.code === selectedCode
          const lead = npc.code === leadNpcCode
          const pulse = pulseLead && npc.code === (pulseNpcCode ?? leadNpcCode)
          const classes = [
            'npc-strip-card',
            selected || lead ? 'npc-strip-card-selected' : '',
            lead ? 'npc-strip-card-lead' : '',
            pulse ? 'npc-strip-card-pulse' : '',
            leadNpcCode && !lead ? 'npc-strip-card-dim' : '',
          ]
            .filter(Boolean)
            .join(' ')
          return (
            <button
              key={npc.code}
              type="button"
              className={classes}
              data-testid={`npc-strip-${npc.code}`}
              aria-pressed={selected}
              onClick={() => onTalk(npc.code)}
            >
              <span className="npc-strip-portrait" aria-hidden="true">
                {portrait ? <img src={portrait} alt="" /> : null}
                {mark ? (
                  <span className="npc-strip-mark" data-testid={`npc-strip-mark-${npc.code}`}>
                    {mark}
                  </span>
                ) : null}
              </span>
              <span className="npc-strip-copy">
                <strong>{npc.name}</strong>
                <span>{npc.title}</span>
              </span>
              {lead ? (
                <span className="npc-strip-talk" data-testid={`npc-strip-talk-${npc.code}`}>
                  Talk
                </span>
              ) : null}
            </button>
          )
        })}
      </div>
    </section>
  )
}
