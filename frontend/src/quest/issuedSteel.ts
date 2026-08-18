import type { QuestResponse } from '../api/types'

export const ISSUED_STEEL_CODE = 'QST_MILITIA_NOTICE'
export const BREN_NPC_CODE = 'MILITIA_OFFICER'
export const SQUARE_COACH_LINE = 'The watch-sergeant will speak if you talk to him.'
export const SQUARE_TURN_IN_COACH_LINE = 'The watch-sergeant is waiting for your report.'
export const OLD_TOWN_COACH_LINE =
  'Dangerous ground. Search can find steel. You stay in this street.'
export const ISSUED_STEEL_OFFER_BANNER = 'Issued Steel — Talk to Watch-Sergeant Bren'
export const ISSUED_STEEL_TRAVEL_BANNER = 'Issued Steel — Travel to Old Town'
export const ISSUED_STEEL_SEARCH_BANNER = 'Issued Steel — Search the alleys'
export const ISSUED_STEEL_TURN_IN_BANNER = 'Issued Steel — Report to Bren in the Square'
export const FIRST_TRAVEL_RULE = 'Leave this place. Arrive in another. You walk; the city stays.'
export const OLD_TOWN_OFFER_LINE = 'The watch does not own this street.'
export const TRAVEL_LEAD_SUBTITLE = 'Travel — leave this place.'
export const SEARCH_LEAD_SUBTITLE = 'Search — look here.'

export type BrenLeadKind = 'offer' | 'turn-in'
export type IssuedSteelLeadPhase =
  | 'offer'
  | 'postGrantTalk'
  | 'travelOut'
  | 'oldTownSearch'
  | 'return'
  | 'turnIn'
export type IssuedSteelAim = 'bren' | 'travel' | 'search'
export type IssuedSteelStatusPulse = 'SAFE' | 'DANGEROUS'

export type IssuedSteelLead = {
  phase: IssuedSteelLeadPhase
  statusPulse: IssuedSteelStatusPulse | null
  verb: IssuedSteelAim | null
  brenStarter: boolean
  coachLine: string | null
  banner: string | null
  aim: IssuedSteelAim | null
  offeredDestination: 'OLD_TOWN' | 'CITY_SQUARE' | null
  firstTravelSheet: boolean
  travelSubtitle: string | null
  searchSubtitle: string | null
}

export function issuedSteelQuest(quests: QuestResponse[]): QuestResponse | undefined {
  return quests.find((quest) => quest.code === ISSUED_STEEL_CODE)
}

export function visitComplete(quest: QuestResponse): boolean {
  return quest.objectives.some((objective) => objective.type === 'VISIT_LOCATION' && objective.completed)
}

export function searchComplete(quest: QuestResponse): boolean {
  if (quest.status === 'READY_TO_TURN_IN') {
    return true
  }
  return quest.objectives.some((objective) => objective.type === 'SEARCH_LOCATION' && objective.completed)
}

function lead(
  phase: IssuedSteelLeadPhase,
  overrides: Partial<IssuedSteelLead> = {},
): IssuedSteelLead {
  return {
    phase,
    statusPulse: null,
    verb: null,
    brenStarter: false,
    coachLine: null,
    banner: null,
    aim: null,
    offeredDestination: null,
    firstTravelSheet: false,
    travelSubtitle: null,
    searchSubtitle: null,
    ...overrides,
  }
}

export function issuedSteelLead(
  quests: QuestResponse[],
  locationCode: string,
  talkOpen: boolean,
  travelOpen = false,
): IssuedSteelLead | null {
  const quest = issuedSteelQuest(quests)
  if (!quest || quest.status === 'COMPLETED' || quest.status === 'AVAILABLE') {
    return null
  }

  const kit = Boolean(quest.kitFamily)
  const visited = visitComplete(quest)
  const searched = searchComplete(quest)

  if (quest.status === 'READY_TO_TURN_IN') {
    if (locationCode === 'CITY_SQUARE') {
      if (talkOpen) {
        return lead('turnIn')
      }
      return lead('turnIn', {
        verb: 'bren',
        coachLine: SQUARE_TURN_IN_COACH_LINE,
        banner: ISSUED_STEEL_TURN_IN_BANNER,
        aim: 'bren',
      })
    }
    return lead('return', {
      verb: 'travel',
      banner: ISSUED_STEEL_TURN_IN_BANNER,
      aim: 'travel',
      offeredDestination: 'CITY_SQUARE',
    })
  }

  if (!kit) {
    if (talkOpen) {
      return null
    }
    if (locationCode !== 'CITY_SQUARE') {
      return lead('offer', {
        banner: ISSUED_STEEL_OFFER_BANNER,
        aim: 'bren',
      })
    }
    return lead('offer', {
      verb: 'bren',
      brenStarter: true,
      coachLine: SQUARE_COACH_LINE,
      banner: ISSUED_STEEL_OFFER_BANNER,
      aim: 'bren',
    })
  }

  if (talkOpen && locationCode === 'CITY_SQUARE' && !visited && !travelOpen) {
    return lead('postGrantTalk', { statusPulse: 'SAFE' })
  }

  if (!searched && locationCode === 'OLD_TOWN') {
    return lead('oldTownSearch', {
      statusPulse: 'DANGEROUS',
      verb: 'search',
      coachLine: OLD_TOWN_COACH_LINE,
      banner: ISSUED_STEEL_SEARCH_BANNER,
      aim: 'search',
      searchSubtitle: SEARCH_LEAD_SUBTITLE,
    })
  }

  if (!searched) {
    return lead('travelOut', {
      verb: 'travel',
      banner: visited ? ISSUED_STEEL_SEARCH_BANNER : ISSUED_STEEL_TRAVEL_BANNER,
      aim: 'travel',
      offeredDestination: 'OLD_TOWN',
      firstTravelSheet: !visited,
      travelSubtitle: locationCode === 'CITY_SQUARE' && !visited ? TRAVEL_LEAD_SUBTITLE : null,
    })
  }

  if (locationCode !== 'CITY_SQUARE') {
    return lead('return', {
      verb: 'travel',
      banner: ISSUED_STEEL_TURN_IN_BANNER,
      aim: 'travel',
      offeredDestination: 'CITY_SQUARE',
    })
  }

  return null
}

export function brenLeadKind(
  quests: QuestResponse[],
  locationCode: string,
  talkOpen: boolean,
): BrenLeadKind | null {
  const current = issuedSteelLead(quests, locationCode, talkOpen)
  if (current?.phase === 'offer') {
    return 'offer'
  }
  if (current?.phase === 'turnIn' && current.aim === 'bren') {
    return 'turn-in'
  }
  return null
}

export function isBrenLeadActive(quests: QuestResponse[], locationCode: string, talkOpen: boolean): boolean {
  return issuedSteelLead(quests, locationCode, talkOpen)?.brenStarter === true
}

export function aimsAtBren(quest: QuestResponse): boolean {
  if (quest.code !== ISSUED_STEEL_CODE) {
    return quest.actionHint === 'TALK'
  }
  return quest.status === 'ACTIVE' || quest.status === 'READY_TO_TURN_IN'
}
