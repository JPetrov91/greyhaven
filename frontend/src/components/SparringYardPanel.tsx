import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { challengeDuel, fetchPublicCharacter, type PublicCharacterResponse } from '../api/pvp'
import { fetchSparringBots, startSparringDrill } from '../api/sparring'
import { fetchNearbyCharacters } from '../api/world'
import { Button } from '../ui/Button'
import { CompactDataRow } from '../ui/CompactDataRow'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { LoadingState } from '../ui/LoadingState'
import { LocationIcon } from '../ui/locationMedia'
import { Panel } from '../ui/Panel'
import { sparringBotFullUrl, sparringBotMiniUrl } from '../ui/sparringMedia'
import { TextInput } from '../ui/TextInput'

type Props = {
  onMatchStarted?: () => void
}

export function SparringYardPanel({ onMatchStarted }: Props) {
  const queryClient = useQueryClient()
  const [error, setError] = useState<string | null>(null)
  const [selectedLevel, setSelectedLevel] = useState(1)
  const [busy, setBusy] = useState(false)
  const [inspected, setInspected] = useState<PublicCharacterResponse | null>(null)

  const nearbyQuery = useQuery({
    queryKey: ['nearby-characters'],
    queryFn: fetchNearbyCharacters,
  })
  const botsQuery = useQuery({
    queryKey: ['sparring-bots'],
    queryFn: fetchSparringBots,
  })

  const nearby = nearbyQuery.data?.characters ?? []
  const bots = botsQuery.data ?? []
  const selectedBot = bots.find((bot) => bot.level === selectedLevel) ?? bots[0]

  async function challenge(id: string) {
    setError(null)
    setBusy(true)
    try {
      await challengeDuel(id)
      await queryClient.invalidateQueries({ queryKey: ['duel'] })
      onMatchStarted?.()
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : 'Unable to start that duel.')
    } finally {
      setBusy(false)
    }
  }

  async function startDrill(botLevel = selectedBot?.level) {
    if (botLevel == null) {
      return
    }
    setError(null)
    setBusy(true)
    try {
      await startSparringDrill(botLevel)
      await queryClient.invalidateQueries({ queryKey: ['combat'] })
      onMatchStarted?.()
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : 'Unable to start that drill.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div id="sparring" className="ui-stack" data-testid="sparring-yard-panel">
      {error ? <ErrorState>{error}</ErrorState> : null}

      <div className="ui-split">
        <Panel title="Live duels" actions={
          <Button
            type="button"
            variant="secondary"
            disabled={nearbyQuery.isFetching}
            onClick={() => void nearbyQuery.refetch()}
          >
            {nearbyQuery.isFetching ? 'Refreshing…' : 'Refresh'}
          </Button>
        }>
          {nearbyQuery.isLoading ? (
            <LoadingState>Looking around…</LoadingState>
          ) : nearby.length === 0 ? (
            <EmptyState testId="sparring-nearby-empty">No other characters are here.</EmptyState>
          ) : (
            <ul className="ui-row-list" data-testid="sparring-nearby">
              {nearby.map((character) => {
                const eligible = character.level <= 10
                return (
                  <CompactDataRow
                    key={character.id}
                    icon={<LocationIcon name="arena" />}
                    primary={
                      <button type="button" onClick={() => void fetchPublicCharacter(character.id).then(setInspected)}>
                        {character.name}
                      </button>
                    }
                    secondary={`Level ${character.level}`}
                    metadata={eligible ? undefined : 'Too experienced for the yard'}
                    action={
                      eligible ? (
                        <Button
                          type="button"
                          data-testid={`sparring-challenge-${character.id}`}
                          disabled={busy}
                          onClick={() => void challenge(character.id)}
                        >
                          Challenge
                        </Button>
                      ) : null
                    }
                  />
                )
              })}
            </ul>
          )}
          <p className="type-body muted">Duels are friendly. No items are lost. Ranked Arena stays separate.</p>
        </Panel>

        <Panel title="Training bots">
          {botsQuery.isLoading ? (
            <LoadingState>Loading drill partners…</LoadingState>
          ) : (
            <>
              <ul className="ui-row-list">
                {bots.map((bot) => (
                  <CompactDataRow
                    key={bot.code}
                    className="ui-row-has-portrait"
                    selected={bot.level === selectedBot?.level}
                    icon={<img src={sparringBotMiniUrl(bot.code)} alt="" />}
                    primary={bot.name}
                    secondary={`Lv. ${bot.level} generated partner`}
                    action={
                      <Button
                        type="button"
                        variant="secondary"
                        data-testid={`sparring-fight-${bot.level}`}
                        disabled={busy}
                        onClick={() => {
                          setSelectedLevel(bot.level)
                          void startDrill(bot.level)
                        }}
                      >
                        Fight
                      </Button>
                    }
                    onClick={() => setSelectedLevel(bot.level)}
                  />
                ))}
              </ul>
              <Field label="Bot difficulty">
                <TextInput
                  type="range"
                  className="ui-range"
                  data-testid="sparring-bot-level"
                  min={bots[0]?.level ?? 1}
                  max={bots[bots.length - 1]?.level ?? 10}
                  step={1}
                  value={selectedBot?.level ?? selectedLevel}
                  onChange={(event) => setSelectedLevel(Number(event.target.value))}
                />
              </Field>
              {selectedBot ? (
                <div className="character-identity" data-testid="sparring-bot-preview">
                  <div className="portrait-tall">
                    <img src={sparringBotFullUrl(selectedBot.code)} alt="" />
                  </div>
                  <p className="type-body muted">
                    {selectedBot.name} is a generated drill partner, not a player.
                  </p>
                </div>
              ) : null}
              <Button type="button" data-testid="sparring-start-drill" disabled={busy || !selectedBot} onClick={() => void startDrill()}>
                Start drill
              </Button>
            </>
          )}
        </Panel>
      </div>

      <div className="ui-split">
        <Panel title="Training notes" variant="inset">
          <p className="type-body muted">
            Defeat does not take your gear. Live duels use fighters already in the yard. Ranked Arena starts at level
            11.
          </p>
        </Panel>
        <Panel title="Training rewards" variant="inset">
          <p className="type-body muted">
            Drills teach timing, not loot. Wins pay no silver, XP, or Arena marks.
          </p>
        </Panel>
      </div>

      {inspected ? (
        <Panel
          as="aside"
          variant="raised"
          data-testid="sparring-inspect"
          title={inspected.name}
          actions={
            <Button type="button" variant="ghost" onClick={() => setInspected(null)}>
              Close
            </Button>
          }
        >
          <p className="type-body">
            Level {inspected.level} · {inspected.weaponFamily ?? 'Unarmed'}
          </p>
        </Panel>
      ) : null}
    </div>
  )
}
