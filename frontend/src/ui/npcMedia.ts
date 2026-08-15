import { merchantPortraitUrl } from './merchantMedia'

const NPC_PORTRAITS: Record<string, string> = {
  'militia-officer': '/merchants/edric-varn.webp',
  'patrol-sergeant': '/merchants/tomas-reed.webp',
  'drill-instructor': '/merchants/edric-varn.webp',
}

export function npcPortraitUrl(code: string | null | undefined): string | undefined {
  if (!code) {
    return undefined
  }
  return NPC_PORTRAITS[code] ?? merchantPortraitUrl(code)
}
