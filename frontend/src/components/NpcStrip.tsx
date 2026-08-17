import type { NpcResponse } from '../api/types'
import { questBadgeMark } from './NpcDialogue'
import { npcPortraitUrl } from '../ui/npcMedia'

type Props = {
  npcs: NpcResponse[]
  onTalk: (npcCode: string) => void
}

export function NpcStrip({ npcs, onTalk }: Props) {
  if (npcs.length === 0) {
    return null
  }

  return (
    <section className="npc-strip" aria-label="People here" data-testid="npc-strip">
      <div className="npc-strip-track">
        {npcs.map((npc) => {
          const mark = questBadgeMark(npc.questBadges[0] ?? '')
          const portrait = npcPortraitUrl(npc.portraitCode)
          return (
            <button
              key={npc.code}
              type="button"
              className="npc-strip-card"
              data-testid={`npc-strip-${npc.code}`}
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
            </button>
          )
        })}
      </div>
    </section>
  )
}
