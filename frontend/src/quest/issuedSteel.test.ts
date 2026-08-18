import { describe, expect, it } from 'vitest'
import type { QuestObjectiveResponse, QuestResponse } from '../api/types'
import { brenLeadKind, issuedSteelLead } from './issuedSteel'

const base: QuestResponse = {
  code: 'QST_MILITIA_NOTICE',
  name: 'Issued Steel',
  description: 'The watch is thin.',
  category: 'MAIN',
  status: 'ACTIVE',
  recommendedLevel: 1,
  startNpcCode: 'MILITIA_OFFICER',
  startNpcName: 'Watch-Sergeant Bren',
  turnInNpcCode: 'MILITIA_OFFICER',
  turnInNpcName: 'Watch-Sergeant Bren',
  nextQuestCode: null,
  nextQuestName: null,
  repeatable: false,
  tracked: true,
  objectives: [],
  rewards: [],
  unlocks: [],
}

function objective(
  type: string,
  targetCode: string,
  completed: boolean,
  displayText: string,
): QuestObjectiveResponse {
  return {
    type,
    targetCode,
    requiredAmount: 1,
    currentAmount: completed ? 1 : 0,
    completed,
    displayText,
    consumeOnTurnIn: false,
  }
}

function quest(overrides: Partial<QuestResponse>): QuestResponse {
  return { ...base, ...overrides }
}

const afterKit = quest({
  kitFamily: 'SWORD',
  objectives: [
    objective('TALK_TO_NPC', 'MILITIA_OFFICER', true, 'Talk to Watch-Sergeant Bren'),
    objective('VISIT_LOCATION', 'OLD_TOWN', false, 'Reach Old Town'),
    objective('SEARCH_LOCATION', 'OLD_TOWN', false, 'Search the alleys'),
  ],
})

describe('brenLeadKind', () => {
  it('offers Bren before the kit on the Square', () => {
    expect(brenLeadKind([quest({ status: 'ACTIVE', kitFamily: null })], 'CITY_SQUARE', false)).toBe('offer')
  })

  it('does not use starter Bren chrome when ready to turn in', () => {
    expect(brenLeadKind([quest({ status: 'READY_TO_TURN_IN', kitFamily: 'SWORD' })], 'CITY_SQUARE', false)).toBe(
      'turn-in',
    )
  })

  it('does not lead Bren after the kit while Old Town is still open', () => {
    expect(brenLeadKind([afterKit], 'CITY_SQUARE', false)).toBeNull()
  })
})

describe('issuedSteelLead', () => {
  it('offers Bren with Talk verb before the kit', () => {
    const current = issuedSteelLead([quest({ kitFamily: null })], 'CITY_SQUARE', false)
    expect(current?.phase).toBe('offer')
    expect(current?.brenStarter).toBe(true)
    expect(current?.aim).toBe('bren')
  })

  it('pulses SAFE during post-grant Talk and does not glow Travel', () => {
    const current = issuedSteelLead([afterKit], 'CITY_SQUARE', true)
    expect(current?.phase).toBe('postGrantTalk')
    expect(current?.statusPulse).toBe('SAFE')
    expect(current?.verb).toBeNull()
  })

  it('aims Travel after kit on the Square', () => {
    const current = issuedSteelLead([afterKit], 'CITY_SQUARE', false)
    expect(current?.phase).toBe('travelOut')
    expect(current?.aim).toBe('travel')
    expect(current?.firstTravelSheet).toBe(true)
    expect(current?.offeredDestination).toBe('OLD_TOWN')
    expect(current?.brenStarter).toBe(false)
  })

  it('aims Search on first Old Town', () => {
    const inTown = quest({
      ...afterKit,
      objectives: [
        objective('TALK_TO_NPC', 'MILITIA_OFFICER', true, 'Talk to Watch-Sergeant Bren'),
        objective('VISIT_LOCATION', 'OLD_TOWN', true, 'Reach Old Town'),
        objective('SEARCH_LOCATION', 'OLD_TOWN', false, 'Search the alleys'),
      ],
    })
    const current = issuedSteelLead([inTown], 'OLD_TOWN', false)
    expect(current?.phase).toBe('oldTownSearch')
    expect(current?.statusPulse).toBe('DANGEROUS')
    expect(current?.aim).toBe('search')
  })

  it('aims Travel back after Search', () => {
    const searched = quest({
      kitFamily: 'SWORD',
      status: 'READY_TO_TURN_IN',
      objectives: [
        objective('TALK_TO_NPC', 'MILITIA_OFFICER', true, 'Talk to Watch-Sergeant Bren'),
        objective('VISIT_LOCATION', 'OLD_TOWN', true, 'Reach Old Town'),
        objective('SEARCH_LOCATION', 'OLD_TOWN', true, 'Search the alleys'),
      ],
    })
    expect(issuedSteelLead([searched], 'OLD_TOWN', false)?.aim).toBe('travel')
    expect(issuedSteelLead([searched], 'CITY_SQUARE', false)?.aim).toBe('bren')
    expect(issuedSteelLead([searched], 'CITY_SQUARE', false)?.brenStarter).toBe(false)
  })
})
