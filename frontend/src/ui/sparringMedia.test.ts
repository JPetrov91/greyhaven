import { describe, expect, it } from 'vitest'
import { isSparringBotCode, sparringBotFullUrl, sparringBotMiniUrl } from './sparringMedia'

describe('sparringMedia', () => {
  it('maps catalog codes to full-body and mini plates', () => {
    expect(sparringBotFullUrl('SPARRING_BOT_L04')).toBe('/sparring/full/sparring_bot_l04.webp')
    expect(sparringBotMiniUrl('SPARRING_BOT_L04')).toBe('/sparring/mini/sparring_bot_l04.webp')
    expect(isSparringBotCode('SPARRING_BOT_L01')).toBe(true)
    expect(isSparringBotCode('STREET_THUG')).toBe(false)
  })
})
