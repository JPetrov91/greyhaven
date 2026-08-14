import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { claimCraftingJob, fetchCurrentCraftingJob, fetchProfessions, fetchRecipes, startCraftingJob } from '../api/crafting'
import { fetchCurrentLocation } from '../api/world'
import type { CraftingJobResponse, LocationAction, Profession, RecipeResponse } from '../api/types'
import { Button } from '../ui/Button'
import { ErrorState } from '../ui/ErrorState'
import { LoadingState } from '../ui/LoadingState'
import { Panel } from '../ui/Panel'
import { gameLink } from '../ui/gameNav'

const PROFESSION_LABELS: Record<Profession, string> = {
  BLACKSMITH: 'Blacksmith',
  ALCHEMIST: 'Alchemist',
  HUNTER: 'Hunter',
}

function formatRemaining(completesAt: string, nowMs: number): string {
  const remainingMs = Math.max(0, Date.parse(completesAt) - nowMs)
  const totalSeconds = Math.ceil(remainingMs / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

type Props = {
  onClose?: () => void
}

export function CraftingPanel({ onClose }: Props) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [nowMs, setNowMs] = useState(() => Date.now())
  const [completionRefreshRequested, setCompletionRefreshRequested] = useState(false)

  const locationQuery = useQuery({
    queryKey: ['location'],
    queryFn: fetchCurrentLocation,
    retry: false,
  })
  const professionsQuery = useQuery({
    queryKey: ['crafting-professions'],
    queryFn: fetchProfessions,
    retry: false,
  })
  const recipesQuery = useQuery({
    queryKey: ['crafting-recipes'],
    queryFn: fetchRecipes,
    retry: false,
  })
  const jobQuery = useQuery({
    queryKey: ['crafting-job'],
    queryFn: fetchCurrentCraftingJob,
    retry: false,
    refetchInterval: (query) => (query.state.data?.status === 'ACTIVE' ? 5_000 : false),
  })

  const atWard = (locationQuery.data?.actions ?? []).includes('CRAFT' satisfies LocationAction)

  useEffect(() => {
    const id = window.setInterval(() => setNowMs(Date.now()), 1_000)
    return () => window.clearInterval(id)
  }, [])

  useEffect(() => {
    setCompletionRefreshRequested(false)
  }, [jobQuery.data?.id, jobQuery.data?.status])

  useEffect(() => {
    const job = jobQuery.data
    if (!job || job.status !== 'ACTIVE' || completionRefreshRequested) {
      return
    }
    if (Date.parse(job.completesAt) <= nowMs) {
      setCompletionRefreshRequested(true)
      void queryClient.invalidateQueries({ queryKey: ['crafting-job'] })
    }
  }, [completionRefreshRequested, jobQuery.data, nowMs, queryClient])

  async function refreshRelated() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['crafting-job'] }),
      queryClient.invalidateQueries({ queryKey: ['crafting-professions'] }),
      queryClient.invalidateQueries({ queryKey: ['crafting-recipes'] }),
      queryClient.invalidateQueries({ queryKey: ['activity'] }),
      queryClient.invalidateQueries({ queryKey: ['character'] }),
      queryClient.invalidateQueries({ queryKey: ['inventory'] }),
    ])
  }

  async function handleStart(recipe: RecipeResponse) {
    setError(null)
    setBusy(true)
    try {
      const started = await startCraftingJob(recipe.code)
      queryClient.setQueryData(['crafting-job'], started)
      await refreshRelated()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Unable to start crafting.')
    } finally {
      setBusy(false)
    }
  }

  async function handleClaim(job: CraftingJobResponse) {
    setError(null)
    setBusy(true)
    try {
      const claimed = await claimCraftingJob(job.id)
      queryClient.setQueryData(['crafting-job'], claimed)
      await refreshRelated()
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
        await queryClient.invalidateQueries({ queryKey: ['crafting-job'] })
      } else {
        setError('Unable to claim the finished craft.')
      }
    } finally {
      setBusy(false)
    }
  }

  if (professionsQuery.isLoading || recipesQuery.isLoading || jobQuery.isLoading) {
    return (
      <Panel className="expedition-panel" data-testid="crafting-panel" title="Craftsmen Ward">
        <LoadingState>Loading professions…</LoadingState>
      </Panel>
    )
  }

  const loadError =
    (professionsQuery.error instanceof ApiError && professionsQuery.error.message) ||
    (recipesQuery.error instanceof ApiError && recipesQuery.error.message) ||
    (jobQuery.error instanceof ApiError && jobQuery.error.message)
  if (loadError) {
    return (
      <Panel className="expedition-panel" data-testid="crafting-panel" title="Craftsmen Ward">
        <ErrorState
          onRetry={() => {
            void professionsQuery.refetch()
            void recipesQuery.refetch()
            void jobQuery.refetch()
          }}
        >
          {loadError}
        </ErrorState>
      </Panel>
    )
  }

  const job = jobQuery.data ?? null
  const visuallyComplete = job?.status === 'ACTIVE' && Date.parse(job.completesAt) <= nowMs
  const openJob = job && job.status !== 'CLAIMED'

  return (
    <Panel
      id="crafting"
      className="expedition-panel"
      data-testid="crafting-panel"
      aria-label="Crafting"
      title="Craftsmen Ward"
      actions={
        onClose ? (
          <Button type="button" variant="ghost" onClick={onClose} data-testid="crafting-close">
            Close
          </Button>
        ) : null
      }
    >
      {!atWard ? (
        <div data-testid="crafting-travel-hint">
          <p className="muted">Travel to the Craftsmen Ward to start, claim, or salvage work.</p>
          <Link className="btn btn-secondary" to={gameLink('world')} data-testid="crafting-travel-cta">
            Open Locations
          </Link>
        </div>
      ) : null}

      <section aria-labelledby="profession-ranks-heading">
        <h3 id="profession-ranks-heading">Professions</h3>
        <ul data-testid="profession-list">
          {(professionsQuery.data ?? []).map((profession) => (
            <li key={profession.profession} data-testid={`profession-${profession.profession}`}>
              <strong>{PROFESSION_LABELS[profession.profession]}</strong> · Rank {profession.rank}
              {profession.maxRank ? ' (max)' : ` · ${profession.xp} XP (${profession.xpToNextRank} to next)`}
            </li>
          ))}
        </ul>
      </section>

      {openJob ? (
        <section data-testid="crafting-job">
          <p>
            {job.recipeName} · <strong>{job.status}</strong>
            {job.rarity ? ` · ${job.rarity}` : ''}
          </p>
          {job.status === 'ACTIVE' && !visuallyComplete ? (
            <p data-testid="crafting-countdown">
              Remaining: <strong>{formatRemaining(job.completesAt, nowMs)}</strong>
            </p>
          ) : null}
          {visuallyComplete ? (
            <p className="muted">Time is up. Refreshing completion from the server…</p>
          ) : null}
          {job.resultReady ? (
            <p>
              Ready: {job.outputItemName} × {job.outputQuantity}
              {job.rarity ? ` (${job.rarity})` : ''} · +{job.professionXp} profession XP
            </p>
          ) : null}
          {job.status === 'COMPLETED' ? (
            <Button
              type="button"
              data-testid="claim-craft-button"
              disabled={busy || !atWard}
              onClick={() => void handleClaim(job)}
            >
              {busy ? 'Claiming…' : 'Claim craft'}
            </Button>
          ) : null}
        </section>
      ) : (
        <p className="muted">No crafting job in progress. Claim finished work before starting another.</p>
      )}

      <section aria-labelledby="recipe-list-heading">
        <h3 id="recipe-list-heading">Recipes</h3>
        <ul className="strategy-list" data-testid="recipe-list">
          {(recipesQuery.data ?? []).map((recipe) => (
            <li key={recipe.code} data-testid={`recipe-${recipe.code}`}>
              <div>
                <strong>{recipe.name}</strong>
                <span className="muted">
                  {' '}
                  — {PROFESSION_LABELS[recipe.profession]} rank {recipe.requiredProfessionRank} · {recipe.durationSeconds}s
                  {recipe.goldCost > 0 ? ` · ${recipe.goldCost}g` : ''}
                </span>
                <p className="muted">
                  {recipe.inputs.map((input) => `${input.itemName} × ${input.quantity} (${input.availableQuantity})`).join(', ')}
                  {` → ${recipe.outputItemName} × ${recipe.outputQuantity}`}
                </p>
                {recipe.unavailableReason ? <p className="muted">{recipe.unavailableReason}</p> : null}
              </div>
              <Button
                type="button"
                data-testid={`start-recipe-${recipe.code}`}
                disabled={busy || !atWard || Boolean(openJob) || !recipe.available}
                onClick={() => void handleStart(recipe)}
              >
                Craft
              </Button>
            </li>
          ))}
        </ul>
      </section>

      {error ? (
        <p className="form-error" role="alert" data-testid="crafting-error">
          {error}
        </p>
      ) : null}
    </Panel>
  )
}
