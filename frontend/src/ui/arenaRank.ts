const DIVISIONS = ['IV', 'III', 'II', 'I'] as const
const TIER_SPAN = 200

const TIERS = [
  { name: 'Iron', min: 0 },
  { name: 'Bronze', min: 1000 },
  { name: 'Silver', min: 1200 },
  { name: 'Gold', min: 1400 },
  { name: 'Platinum', min: 1600 },
  { name: 'Emerald', min: 1800 },
  { name: 'Diamond', min: 2000 },
] as const

export type ArenaRank = {
  name: string
  tier: string
  division: (typeof DIVISIONS)[number] | null
  nextName: string | null
  progress: number
  needed: number
  rating: number
}

export function arenaRankFromRating(rating: number): ArenaRank {
  const safe = Math.max(0, Math.floor(rating))
  let index = 0
  for (let i = 0; i < TIERS.length; i += 1) {
    if (safe >= TIERS[i].min) {
      index = i
    }
  }
  const tier = TIERS[index]
  const next = TIERS[index + 1] ?? null
  const span = next ? next.min - tier.min : TIER_SPAN
  const into = safe - tier.min
  const divisionSize = Math.max(1, Math.floor(span / DIVISIONS.length))
  const divisionIndex = Math.min(DIVISIONS.length - 1, Math.floor(into / divisionSize))
  const division = tier.name === 'Iron' && safe < 1000 ? null : DIVISIONS[divisionIndex]
  const nextDivision = divisionIndex < DIVISIONS.length - 1
    ? `${tier.name} ${DIVISIONS[divisionIndex + 1]}`
    : next
      ? `${next.name} ${DIVISIONS[0]}`
      : null
  const intoDivision = into - divisionIndex * divisionSize
  const needed = nextDivision ? divisionSize : span
  return {
    name: division ? `${tier.name} ${division}` : tier.name,
    tier: tier.name,
    division,
    nextName: nextDivision,
    progress: Math.min(needed, intoDivision),
    needed,
    rating: safe,
  }
}
