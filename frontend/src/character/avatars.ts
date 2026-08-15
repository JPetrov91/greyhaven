export type CharacterGender = 'MALE' | 'FEMALE'

export type AvatarDefinition = {
  code: string
  gender: CharacterGender
  title: string
  imageUrl: string
}

export const AVATARS: readonly AvatarDefinition[] = [
  {
    code: 'male_unyielding',
    gender: 'MALE',
    title: 'The Unyielding',
    imageUrl: '/character/avatars/male-unyielding.png',
  },
  {
    code: 'male_iron_vow',
    gender: 'MALE',
    title: 'The Iron Vow',
    imageUrl: '/character/avatars/male-iron-vow.png',
  },
  {
    code: 'male_ashen_wolf',
    gender: 'MALE',
    title: 'The Ashen Wolf',
    imageUrl: '/character/avatars/male-ashen-wolf.png',
  },
  {
    code: 'male_pale_heir',
    gender: 'MALE',
    title: 'The Pale Heir',
    imageUrl: '/character/avatars/male-pale-heir.png',
  },
  {
    code: 'male_oathbound',
    gender: 'MALE',
    title: 'The Oathbound',
    imageUrl: '/character/avatars/male-oathbound.png',
  },
  {
    code: 'female_veiled',
    gender: 'FEMALE',
    title: 'The Veiled',
    imageUrl: '/character/avatars/female-veiled.png',
  },
  {
    code: 'female_nightbloom',
    gender: 'FEMALE',
    title: 'The Nightbloom',
    imageUrl: '/character/avatars/female-nightbloom.png',
  },
  {
    code: 'female_silver_thorn',
    gender: 'FEMALE',
    title: 'The Silver Thorn',
    imageUrl: '/character/avatars/female-silver-thorn.png',
  },
  {
    code: 'female_ember_queen',
    gender: 'FEMALE',
    title: 'The Ember Queen',
    imageUrl: '/character/avatars/female-ember-queen.png',
  },
  {
    code: 'female_hollow_saint',
    gender: 'FEMALE',
    title: 'The Hollow Saint',
    imageUrl: '/character/avatars/female-hollow-saint.png',
  },
] as const

export function avatarsFor(gender: CharacterGender): AvatarDefinition[] {
  return AVATARS.filter((avatar) => avatar.gender === gender)
}

export function avatarByCode(code: string | null | undefined): AvatarDefinition | undefined {
  if (!code) {
    return undefined
  }
  return AVATARS.find((avatar) => avatar.code === code)
}

export function avatarImageUrl(code: string | null | undefined): string {
  return avatarByCode(code)?.imageUrl ?? '/character/default-avatar.webp'
}
