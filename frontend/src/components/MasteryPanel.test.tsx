// @vitest-environment jsdom

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fetchMasteries, fetchTechniques, updateTechniqueLoadout } from '../api/mastery'
import type { MasteriesResponse, TechniquesResponse } from '../api/types'
import { MasteryPanel } from './MasteryPanel'

vi.mock('../api/mastery', () => ({
  fetchMasteries: vi.fn(),
  fetchTechniques: vi.fn(),
  updateTechniqueLoadout: vi.fn(),
}))

afterEach(() => {
  cleanup()
  vi.clearAllMocks()
})

function masteriesFixture(): MasteriesResponse {
  return {
    equippedWeaponFamily: 'SWORD',
    masteries: ['SWORD', 'AXE', 'MACE', 'DAGGER', 'BOW'].map((family) => ({
      weaponFamily: family as MasteriesResponse['masteries'][number]['weaponFamily'],
      level: family === 'SWORD' ? 2 : 0,
      totalExperience: family === 'SWORD' ? 200 : 0,
      progress: {
        level: family === 'SWORD' ? 2 : 0,
        totalExperience: family === 'SWORD' ? 200 : 0,
        experienceIntoCurrentLevel: 0,
        experienceRequiredForNextLevel: family === 'SWORD' ? 200 : 80,
        experienceRemaining: family === 'SWORD' ? 200 : 80,
        progressPercent: 0,
        maxLevel: false,
      },
      nextUnlockCodes: family === 'SWORD' ? ['SWORD_DEEP_CUT'] : [],
    })),
  }
}

function techniquesFixture(): TechniquesResponse {
  return {
    equippedWeaponFamily: 'SWORD',
    techniques: [
      {
        code: 'SWORD_RIPOSTE',
        displayName: 'Riposte',
        description: 'A precise counter.',
        weaponFamily: 'SWORD',
        unlockMasteryLevel: 2,
        kind: 'ACTIVE',
        unlocked: true,
        staminaCost: 8,
        accuracyModifier: 8,
        damagePercentModifier: 0,
        appliesStatus: null,
        tags: 'COUNTER',
      },
      {
        code: 'SWORD_DEEP_CUT',
        displayName: 'Deep Cut',
        description: 'A heavier slash.',
        weaponFamily: 'SWORD',
        unlockMasteryLevel: 4,
        kind: 'ACTIVE',
        unlocked: false,
        staminaCost: 12,
        accuracyModifier: 0,
        damagePercentModifier: 15,
        appliesStatus: 'BLEED',
        tags: '',
      },
      {
        code: 'SWORD_MASTERY',
        displayName: 'Sword Mastery',
        description: 'Passive.',
        weaponFamily: 'SWORD',
        unlockMasteryLevel: 10,
        kind: 'PASSIVE',
        unlocked: false,
        staminaCost: 0,
        accuracyModifier: 4,
        damagePercentModifier: 5,
        appliesStatus: null,
        tags: 'MASTERY_PASSIVE',
      },
    ],
    loadout: {
      slots: [null, null, null, null],
      loadoutFamily: null,
      compatibleWithEquippedWeapon: true,
    },
  }
}

function renderPanel(mutationsDisabled = false) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={queryClient}>
      <MasteryPanel mutationsDisabled={mutationsDisabled} />
    </QueryClientProvider>,
  )
}

describe('MasteryPanel', () => {
  it('shows unlocked and locked techniques in the collection', async () => {
    vi.mocked(fetchMasteries).mockResolvedValue(masteriesFixture())
    vi.mocked(fetchTechniques).mockResolvedValue(techniquesFixture())

    renderPanel()

    expect(await screen.findByTestId('technique-SWORD_RIPOSTE')).toBeTruthy()
    expect(screen.getByTestId('technique-SWORD_RIPOSTE').textContent).toContain('Unlocked')
    expect(screen.getByTestId('technique-SWORD_DEEP_CUT').textContent).toContain('Mastery 4')
    expect(screen.getByTestId('technique-SWORD_MASTERY').textContent).toContain('Passive')
  })

  it('saves an unlocked technique into the loadout', async () => {
    vi.mocked(fetchMasteries).mockResolvedValue(masteriesFixture())
    vi.mocked(fetchTechniques).mockResolvedValue(techniquesFixture())
    vi.mocked(updateTechniqueLoadout).mockResolvedValue({
      ...techniquesFixture(),
      loadout: {
        slots: ['SWORD_RIPOSTE', null, null, null],
        loadoutFamily: 'SWORD',
        compatibleWithEquippedWeapon: true,
      },
    })

    renderPanel()
    await screen.findByTestId('save-loadout')

    fireEvent.change(screen.getByTestId('loadout-slot-0'), { target: { value: 'SWORD_RIPOSTE' } })
    fireEvent.click(screen.getByTestId('save-loadout'))

    expect(await screen.findByTestId('save-loadout')).toBeTruthy()
    expect(updateTechniqueLoadout).toHaveBeenCalledWith(['SWORD_RIPOSTE', null, null, null])
  })

  it('warns when the loadout family does not match the equipped weapon', async () => {
    vi.mocked(fetchMasteries).mockResolvedValue(masteriesFixture())
    vi.mocked(fetchTechniques).mockResolvedValue({
      ...techniquesFixture(),
      equippedWeaponFamily: 'BOW',
      loadout: {
        slots: ['SWORD_RIPOSTE', null, null, null],
        loadoutFamily: 'SWORD',
        compatibleWithEquippedWeapon: false,
      },
    })

    renderPanel()

    expect(await screen.findByTestId('mastery-incompatible')).toBeTruthy()
  })

  it('disables loadout edits during combat', async () => {
    vi.mocked(fetchMasteries).mockResolvedValue(masteriesFixture())
    vi.mocked(fetchTechniques).mockResolvedValue(techniquesFixture())

    renderPanel(true)

    expect(await screen.findByTestId('mastery-combat-lock')).toBeTruthy()
    expect((screen.getByTestId('loadout-slot-0') as HTMLSelectElement).disabled).toBe(true)
    expect((screen.getByTestId('save-loadout') as HTMLButtonElement).disabled).toBe(true)
  })
})
