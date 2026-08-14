const MERCHANT_ART: Record<string, string> = {
  'edric-varn': '/merchants/edric-varn.webp',
  'mara-helden': '/merchants/mara-helden.webp',
  'sister-calia': '/merchants/sister-calia.webp',
  'tomas-reed': '/merchants/tomas-reed.webp',
}

export function merchantPortraitUrl(code: string | null | undefined): string | undefined {
  if (!code) {
    return undefined
  }
  return MERCHANT_ART[code]
}
