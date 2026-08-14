import { describe, expect, it } from 'vitest'
import { locationArtUrl, locationWeather } from './locationMedia'

describe('locationArtUrl', () => {
  it('maps known location codes to compressed banners', () => {
    expect(locationArtUrl('FOREST')).toBe('/locations/forest.webp')
    expect(locationArtUrl('CITY_SQUARE')).toBe('/locations/city_square.webp')
    expect(locationArtUrl('UNKNOWN')).toBe('/locations/city_square.webp')
    expect(locationArtUrl('HARBOUR')).toBe('/locations/north_road.webp')
    expect(locationArtUrl('ANCIENT_RUINS')).toBe('/locations/forest.webp')
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
    expect(locationWeather('UNKNOWN').label).toBe('Cloudy')
  })
})
