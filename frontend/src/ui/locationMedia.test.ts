import { createElement } from 'react'
import { describe, expect, it } from 'vitest'
import { renderToStaticMarkup } from 'react-dom/server'
import { LocationIcon, locationActionArtUrl, locationArtUrl, locationWeather } from './locationMedia'

describe('locationArtUrl', () => {
  it('maps known location codes to compressed banners', () => {
    expect(locationArtUrl('FOREST')).toBe('/locations/forest.webp')
    expect(locationArtUrl('CITY_SQUARE')).toBe('/locations/city_square.webp')
    expect(locationArtUrl('UNKNOWN')).toBe('/locations/city_square.webp')
    expect(locationArtUrl('HARBOUR')).toBe('/locations/harbour.webp')
    expect(locationArtUrl('ANCIENT_RUINS')).toBe('/locations/ancient_ruins.webp')
    expect(locationArtUrl('ARENA')).toBe('/locations/arena.webp')
    expect(locationArtUrl('SPARRING_YARD')).toBe('/locations/sparring_yard.webp')
    expect(locationArtUrl('CRAFTSMEN_WARD')).toBe('/locations/craftsmen_ward.webp')
  })
})

describe('locationWeather', () => {
  it('returns flavor weather for known locations', () => {
    expect(locationWeather('CITY_SQUARE')).toEqual({
      label: 'Cloudy',
      temperature: '13°C',
      icon: 'weather-cloud',
    })
    expect(locationWeather('FOREST').label).toBe('Damp')
    expect(locationWeather('ARENA').label).toBe('Dusty')
    expect(locationWeather('SPARRING_YARD')).toEqual({
      label: 'Overcast',
      temperature: '12°C',
      icon: 'weather-cloud',
    })
    expect(locationWeather('UNKNOWN').label).toBe('Cloudy')
  })
})

describe('LocationIcon', () => {
  it('gives arena a distinct crossed-swords mark from travel', () => {
    const arena = renderToStaticMarkup(createElement(LocationIcon, { name: 'arena' }))
    const travel = renderToStaticMarkup(createElement(LocationIcon, { name: 'compass' }))
    const market = renderToStaticMarkup(createElement(LocationIcon, { name: 'market' }))
    expect(arena).toContain('Arena')
    expect(travel).toContain('Travel')
    expect(arena).not.toBe(travel)
    expect(market).toContain('Market')
  })
})

describe('locationActionArtUrl', () => {
  it('maps action tiles to painted icon assets', () => {
    expect(locationActionArtUrl('compass')).toBe('/icons/actions/travel.webp')
    expect(locationActionArtUrl('arena')).toBe('/icons/actions/arena.webp')
    expect(locationActionArtUrl('market')).toBe('/icons/actions/market.webp')
    expect(locationActionArtUrl('globe')).toBe('/icons/env/world-map.webp')
    expect(locationActionArtUrl('weather-cloud')).toBe('/icons/env/weather-cloud.webp')
    expect(locationActionArtUrl('map')).toBeUndefined()
  })
})
