const ITEM_ART: Record<string, string> = {
  MILITIA_SHORTSWORD: '/items/militia_shortsword.webp',
  ARMING_SWORD: '/items/arming_sword.webp',
}

export function itemArtUrl(code: string | null | undefined): string | undefined {
  if (!code) {
    return undefined
  }
  return ITEM_ART[code]
}
