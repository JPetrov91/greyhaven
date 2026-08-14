const ITEM_ART_CODES = new Set([
  'RUSTY_SWORD',
  'IRON_SWORD',
  'MILITIA_SHORTSWORD',
  'ARMING_SWORD',
  'OLD_DAGGER',
  'HUNTING_BOW',
  'IRON_AXE',
  'WOODSMAN_AXE',
  'IRON_MACE',
  'KNOBBED_CLUB',
  'WORN_LEATHER_ARMOR',
  'LEATHER_ARMOR',
  'PADDED_JACK',
  'SPLINT_VEST',
  'MAIL_HAUBERK',
  'IRON_PLATE',
  'WOODEN_BUCKLER',
  'LEATHER_CAP',
  'IRON_HELM',
  'LEATHER_GLOVES',
  'LEATHER_LEGGINGS',
  'LEATHER_BOOTS',
  'COPPER_AMULET',
  'COPPER_RING',
  'HEALING_POTION',
  'WOLF_PELT',
])

export function itemArtUrl(code: string | null | undefined): string | undefined {
  if (!code || !ITEM_ART_CODES.has(code)) {
    return undefined
  }
  return `/items/${code.toLowerCase()}.webp`
}
