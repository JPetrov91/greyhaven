import type { CharacterGender } from './avatars'

const MALE_GIVEN = [
  'Ragnar',
  'Kael',
  'Aldric',
  'Torin',
  'Draven',
  'Ulric',
  'Bram',
  'Cedric',
  'Garrick',
  'Bjorn',
] as const

const FEMALE_GIVEN = [
  'Morwen',
  'Seren',
  'Veyra',
  'Isolde',
  'Lyra',
  'Maelis',
  'Nyx',
  'Elara',
  'Rowena',
  'Ysabel',
] as const

const MALE_EPITHET = [
  'Ironfist',
  'Shadowbane',
  'Ashwalker',
  'Grimward',
  'Frostborn',
  'Blackthorn',
  'Wolfheart',
] as const

const FEMALE_EPITHET = [
  'Nightveil',
  'Duskhar',
  'Stormvale',
  'Silverthorn',
  'Nightbloom',
  'Emberfall',
  'Moonward',
] as const

export const CHARACTER_NAME_PATTERN = /^[\p{L}\p{N}]+(?: [\p{L}\p{N}]+)*$/u

export function randomCharacterName(
  gender: CharacterGender = 'MALE',
  random: () => number = Math.random,
): string {
  const givenPool = gender === 'FEMALE' ? FEMALE_GIVEN : MALE_GIVEN
  const epithetPool = gender === 'FEMALE' ? FEMALE_EPITHET : MALE_EPITHET
  const given = givenPool[Math.floor(random() * givenPool.length)] ?? givenPool[0]
  const epithet = epithetPool[Math.floor(random() * epithetPool.length)] ?? epithetPool[0]
  const name = `${given} ${epithet}`
  if (name.length < 3 || name.length > 24 || !CHARACTER_NAME_PATTERN.test(name)) {
    return gender === 'FEMALE' ? 'Seren Nightveil' : 'Ragnar Ironfist'
  }
  return name
}
