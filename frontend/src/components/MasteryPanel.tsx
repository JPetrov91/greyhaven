import { useEffect, useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchMasteries, fetchTechniques, updateTechniqueLoadout } from '../api/mastery'
import type { TechniqueDefinitionResponse, WeaponFamily } from '../api/types'
import { Button } from '../ui/Button'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { ProgressBar } from '../ui/ProgressBar'
import { StatusBadge } from '../ui/StatusBadge'
import { Tabs } from '../ui/Tabs'

const FAMILIES: { id: WeaponFamily; label: string }[] = [
  { id: 'SWORD', label: 'Sword' },
  { id: 'AXE', label: 'Axe' },
  { id: 'MACE', label: 'Mace' },
  { id: 'DAGGER', label: 'Dagger' },
  { id: 'BOW', label: 'Bow' },
]

type Props = {
  mutationsDisabled?: boolean
}

export function MasteryPanel({ mutationsDisabled = false }: Props) {
  const queryClient = useQueryClient()
  const [family, setFamily] = useState<WeaponFamily>('SWORD')
  const [slots, setSlots] = useState<Array<string | null>>([null, null, null, null])
  const [saveError, setSaveError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const masteriesQuery = useQuery({
    queryKey: ['masteries'],
    queryFn: fetchMasteries,
    retry: false,
  })

  const techniquesQuery = useQuery({
    queryKey: ['techniques'],
    queryFn: fetchTechniques,
    retry: false,
  })

  const techniques = techniquesQuery.data
  const masteries = masteriesQuery.data

  useEffect(() => {
    if (!techniques) {
      return
    }
    setSlots(padSlots(techniques.loadout.slots))
    const loadoutFamily = techniques.loadout.loadoutFamily
    const equipped = techniques.equippedWeaponFamily
    if (loadoutFamily) {
      setFamily(loadoutFamily)
    } else if (equipped) {
      setFamily(equipped)
    }
  }, [techniques])

  const familyTechniques = useMemo(
    () => (techniques?.techniques ?? []).filter((entry) => entry.weaponFamily === family),
    [techniques, family],
  )

  const unlockedActives = familyTechniques.filter(
    (entry) => entry.unlocked && entry.kind === 'ACTIVE',
  )

  const mastery = masteries?.masteries.find((entry) => entry.weaponFamily === family) ?? null

  const panelProps = {
    className: 'game-column mastery-panel',
    'aria-label': 'Mastery',
    id: 'mastery',
    'data-testid': 'mastery-panel',
    title: 'Mastery',
  } as const

  if (masteriesQuery.isLoading || techniquesQuery.isLoading) {
    return (
      <Panel {...panelProps}>
        <LoadingState>Loading mastery…</LoadingState>
      </Panel>
    )
  }

  const loadError =
    (masteriesQuery.error instanceof ApiError && masteriesQuery.error.message) ||
    (techniquesQuery.error instanceof ApiError && techniquesQuery.error.message) ||
    null
  if (loadError) {
    return (
      <Panel {...panelProps}>
        <ErrorState
          onRetry={() => {
            void masteriesQuery.refetch()
            void techniquesQuery.refetch()
          }}
        >
          {loadError}
        </ErrorState>
      </Panel>
    )
  }

  if (!masteries || !techniques || !mastery) {
    return null
  }

  const progress = mastery.progress

  async function handleSave() {
    setSaveError(null)
    setSaving(true)
    try {
      const updated = await updateTechniqueLoadout(slots)
      queryClient.setQueryData(['techniques'], updated)
    } catch (error) {
      setSaveError(error instanceof ApiError ? error.message : 'Unable to save loadout.')
    } finally {
      setSaving(false)
    }
  }

  function setSlot(index: number, code: string) {
    setSlots((current) => {
      const next = [...current]
      next[index] = code === '' ? null : code
      return next
    })
  }

  return (
    <Panel {...panelProps}>
      <Tabs
        label="Weapon family"
        testId="mastery-family-tabs"
        value={family}
        onChange={(id) => setFamily(id as WeaponFamily)}
        tabs={FAMILIES}
      />

      <p data-testid="mastery-level">
        {progress.maxLevel ? `${labelOf(family)} mastery ${mastery.level} — MAX` : `${labelOf(family)} mastery ${mastery.level}`}
      </p>
      <div className="xp-progress" data-testid="mastery-xp">
        {progress.maxLevel ? (
          <span>MAX</span>
        ) : (
          <span>
            {progress.experienceIntoCurrentLevel} / {progress.experienceRequiredForNextLevel} XP
          </span>
        )}
        <ProgressBar
          value={progress.maxLevel ? 100 : progress.progressPercent}
          label={`${labelOf(family)} mastery progress`}
          testId="mastery-xp-bar"
        />
      </div>

      {masteries.equippedWeaponFamily ? (
        <p className="muted" data-testid="mastery-equipped-family">
          Equipped: {labelOf(masteries.equippedWeaponFamily)}
        </p>
      ) : (
        <p className="muted" data-testid="mastery-equipped-family">
          No weapon equipped
        </p>
      )}

      {!techniques.loadout.compatibleWithEquippedWeapon ? (
        <p className="form-error" role="status" data-testid="mastery-incompatible">
          Loadout family does not match the equipped weapon. It can still be saved for later.
        </p>
      ) : null}

      <h3>Techniques</h3>
      {familyTechniques.length === 0 ? (
        <EmptyState>No techniques for this family.</EmptyState>
      ) : (
        <ul className="mastery-technique-list" data-testid="mastery-collection">
          {familyTechniques.map((technique) => (
            <li key={technique.code} data-testid={`technique-${technique.code}`}>
              <div className="mastery-technique-heading">
                <strong>{technique.displayName}</strong>
                {technique.unlocked ? (
                  <StatusBadge tone="upgrade">Unlocked</StatusBadge>
                ) : (
                  <StatusBadge>Mastery {technique.unlockMasteryLevel}</StatusBadge>
                )}
                {technique.kind === 'PASSIVE' ? <StatusBadge tone="neutral">Passive</StatusBadge> : null}
              </div>
              <p className="muted">{technique.description}</p>
            </li>
          ))}
        </ul>
      )}

      <h3>Loadout</h3>
      {mutationsDisabled ? (
        <p className="muted" data-testid="mastery-combat-lock">
          Technique loadout cannot be changed during combat.
        </p>
      ) : null}
      <div className="mastery-loadout" data-testid="mastery-loadout">
        {slots.map((code, index) => (
          <Field key={index} label={`Slot ${index + 1}`}>
            <select
              data-testid={`loadout-slot-${index}`}
              value={code ?? ''}
              disabled={mutationsDisabled || saving}
              onChange={(event) => setSlot(index, event.target.value)}
            >
              <option value="">Empty</option>
              {optionsForSlot(unlockedActives, slots, index).map((technique) => (
                <option key={technique.code} value={technique.code}>
                  {technique.displayName}
                </option>
              ))}
            </select>
          </Field>
        ))}
      </div>
      <Button
        type="button"
        data-testid="save-loadout"
        disabled={mutationsDisabled || saving}
        onClick={() => void handleSave()}
      >
        Save loadout
      </Button>
      {saveError ? (
        <p className="form-error" role="alert" data-testid="loadout-error">
          {saveError}
        </p>
      ) : null}
    </Panel>
  )
}

function padSlots(slots: Array<string | null>): Array<string | null> {
  const next = [...slots]
  while (next.length < 4) {
    next.push(null)
  }
  return next.slice(0, 4)
}

function optionsForSlot(
  unlockedActives: TechniqueDefinitionResponse[],
  slots: Array<string | null>,
  index: number,
): TechniqueDefinitionResponse[] {
  const used = new Set(slots.filter((code, slotIndex) => code && slotIndex !== index))
  return unlockedActives.filter((technique) => !used.has(technique.code) || technique.code === slots[index])
}

function labelOf(family: WeaponFamily): string {
  return FAMILIES.find((entry) => entry.id === family)?.label ?? family
}
