import type { ActivityType } from '../api/types'

export type ActivityIconName = 'scroll' | 'chest' | 'gold' | 'swords' | 'craft' | 'alert'

export type ActivityTone = 'plain' | 'gold' | 'blue' | 'red'

export type ActivityTextPart = {
  text: string
  tone: ActivityTone
}

const ICON_BY_TYPE: Record<ActivityType, ActivityIconName> = {
  COMBAT_VICTORY: 'swords',
  LEVEL_UP: 'scroll',
  ITEM_FOUND: 'chest',
  EXPEDITION_COMPLETED: 'chest',
  EXPEDITION_CLAIMED: 'chest',
  MARKET_SOLD: 'gold',
  MARKET_BOUGHT: 'gold',
  MARKET_CANCELLED: 'gold',
  MASTERY_UNLOCK: 'scroll',
  TECHNIQUE_UNLOCK: 'scroll',
  ARENA_VICTORY: 'swords',
  ARENA_DEFEAT: 'swords',
  DUEL_RESULT: 'swords',
  CRAFTING_STARTED: 'craft',
  CRAFTING_CLAIMED: 'craft',
  PROFESSION_RANK_UP: 'craft',
  ITEM_SALVAGED: 'craft',
  MARKET_LISTING_FEE: 'gold',
  MARKET_SALE: 'gold',
  BUY_ORDER_CREATED: 'gold',
  BUY_ORDER_FILLED: 'gold',
  BUY_ORDER_CANCELLED: 'gold',
  QUEST_ACCEPTED: 'scroll',
  QUEST_OBJECTIVE: 'scroll',
  QUEST_COMPLETED: 'scroll',
}

export function activityIconUrl(type: ActivityType | 'alert'): string {
  const name = type === 'alert' ? 'alert' : ICON_BY_TYPE[type]
  return `/icons/activity/${name}.webp`
}

export function formatRelativeTime(iso: string, nowMs = Date.now()): string {
  const then = new Date(iso).getTime()
  if (Number.isNaN(then)) {
    return iso
  }
  const minutes = Math.max(0, Math.floor((nowMs - then) / 60_000))
  if (minutes < 1) {
    return 'now'
  }
  if (minutes < 60) {
    return `${minutes}m ago`
  }
  const hours = Math.floor(minutes / 60)
  if (hours < 24) {
    return `${hours}h ago`
  }
  return `${Math.floor(hours / 24)}d ago`
}

function highlight(message: string, fragment: string, tone: ActivityTone): ActivityTextPart[] {
  const start = message.indexOf(fragment)
  if (start < 0) {
    return [{ text: message, tone: 'plain' }]
  }
  const parts: ActivityTextPart[] = [
    { text: message.slice(0, start), tone: 'plain' },
    { text: fragment, tone },
    { text: message.slice(start + fragment.length), tone: 'plain' },
  ]
  return parts.filter((part) => part.text.length > 0)
}

function captured(message: string, pattern: RegExp, tone: ActivityTone): ActivityTextPart[] | null {
  const found = message.match(pattern)
  return found?.[1] ? highlight(message, found[1], tone) : null
}

export function activityMessageParts(type: ActivityType, message: string): ActivityTextPart[] {
  switch (type) {
    case 'COMBAT_VICTORY':
      return captured(message, /You defeated a (.+)\.$/, 'red') ?? [{ text: message, tone: 'plain' }]
    case 'LEVEL_UP':
      return captured(message, /(LEVEL UP)/, 'gold') ?? [{ text: message, tone: 'plain' }]
    case 'ITEM_FOUND':
      return captured(message, /You found (?:\d+x )?(.+)\.$/, 'blue') ?? [{ text: message, tone: 'plain' }]
    case 'EXPEDITION_COMPLETED':
      return captured(message, /Your (.+) returned\.$/, 'gold') ?? [{ text: message, tone: 'plain' }]
    case 'EXPEDITION_CLAIMED':
      return captured(message, /You claimed your (.+) rewards\.$/, 'gold') ?? [{ text: message, tone: 'plain' }]
    case 'MARKET_BOUGHT':
      return captured(message, /You bought (.+?)(?: for |\.)/, 'blue') ?? [{ text: message, tone: 'plain' }]
    case 'MARKET_SOLD':
      return (
        captured(message, /You sold (.+?) for /, 'blue') ??
        captured(message, /(\d+ gold)/i, 'gold') ?? [{ text: message, tone: 'plain' }]
      )
    case 'MASTERY_UNLOCK':
      return captured(message, /Your (.+) mastery/, 'gold') ?? [{ text: message, tone: 'plain' }]
    case 'TECHNIQUE_UNLOCK':
      return captured(message, /You learned (.+)\.$/, 'blue') ?? [{ text: message, tone: 'plain' }]
    case 'CRAFTING_STARTED':
      return captured(message, /You began crafting (.+)\.$/, 'blue') ?? [{ text: message, tone: 'plain' }]
    case 'CRAFTING_CLAIMED':
      return captured(message, /You finished (.+)\.$/, 'blue') ?? [{ text: message, tone: 'plain' }]
    case 'ITEM_SALVAGED':
      return captured(message, /You salvaged (.+)\.$/, 'blue') ?? [{ text: message, tone: 'plain' }]
    case 'ARENA_VICTORY':
    case 'ARENA_DEFEAT':
    case 'DUEL_RESULT':
      return (
        captured(message, / vs (.+?)(?: ended| \(|$)/, 'blue') ??
        captured(message, / defeated (.+?)(?: \(|$)/, 'blue') ??
        captured(message, / lost to (.+?)(?: \(|$)/, 'red') ?? [{ text: message, tone: 'plain' }]
      )
    case 'PROFESSION_RANK_UP':
      return captured(message, /^(.+) reached rank/, 'gold') ?? [{ text: message, tone: 'plain' }]
    default:
      return [{ text: message, tone: 'plain' }]
  }
}
