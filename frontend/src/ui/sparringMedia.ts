export function sparringBotFullUrl(code: string): string {
  return `/sparring/full/${code.toLowerCase()}.webp`
}

export function sparringBotMiniUrl(code: string): string {
  return `/sparring/mini/${code.toLowerCase()}.webp`
}

export function isSparringBotCode(code: string): boolean {
  return code.startsWith('SPARRING_BOT_')
}
