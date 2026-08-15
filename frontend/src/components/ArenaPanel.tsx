import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../api/client'
import { fetchCharacter } from '../api/character'
import { fetchInventory } from '../api/inventory'
import {
  challengeArena,
  challengeDuel,
  fetchArenaOpponents,
  fetchArenaProfile,
  fetchPvpHistory,
  fetchPublicCharacter,
  updateArenaDefense,
  type ArenaDefense,
  type PublicCharacterResponse,
} from '../api/pvp'
import type { CombatAction } from '../api/types'
import { arenaRankFromRating } from '../ui/arenaRank'
import { Button } from '../ui/Button'
import { CharacterPortrait } from '../ui/CharacterPortrait'
import { ComingLaterButton } from '../ui/ComingLater'
import { EmptyState } from '../ui/EmptyState'
import { ErrorState } from '../ui/ErrorState'
import { Field } from '../ui/Field'
import { gameLink } from '../ui/gameNav'
import { ItemIcon } from '../ui/itemIcons'
import { LoadingState } from '../ui/LoadingState'
import { locationArtUrl } from '../ui/locationMedia'
import { Panel } from '../ui/Panel'

type Props = {
  onMatchStarted: () => void
}

type ArenaTab = 'pvp' | 'pve' | 'defense' | 'history' | 'rewards'

const LOADOUT_SLOTS = ['MAIN_HAND', 'OFF_HAND', 'CHEST', 'RING', 'FEET'] as const

const PVE_PREVIEWS = [
  {
    title: 'Pit of the Damned',
    location: 'SEWERS',
    difficulty: 'Hard',
    waves: 'Waves 1–8',
  },
  {
    title: 'Coliseum of Trials',
    location: 'ARENA',
    difficulty: 'Nightmare',
    waves: 'Waves 1–12',
  },
  {
    title: 'Abyssal Gauntlet',
    location: 'ANCIENT_RUINS',
    difficulty: 'Hell',
    waves: 'Waves 1–16',
  },
] as const

export function ArenaPanel({ onMatchStarted }: Props) {
  const queryClient = useQueryClient()
  const [tab, setTab] = useState<ArenaTab>('pvp')
  const [error, setError] = useState<string | null>(null)
  const [selectedOpponentId, setSelectedOpponentId] = useState<string | null>(null)
  const [inspected, setInspected] = useState<PublicCharacterResponse | null>(null)
  const profileQuery = useQuery({ queryKey: ['arena-profile'], queryFn: fetchArenaProfile })
  const opponentsQuery = useQuery({ queryKey: ['arena-opponents'], queryFn: () => fetchArenaOpponents(0) })
  const historyQuery = useQuery({ queryKey: ['pvp-history'], queryFn: () => fetchPvpHistory(0) })
  const characterQuery = useQuery({ queryKey: ['character'], queryFn: fetchCharacter })
  const inventoryQuery = useQuery({ queryKey: ['inventory'], queryFn: fetchInventory, retry: false })

  const profile = profileQuery.data
  const opponents = opponentsQuery.data?.opponents ?? []
  const history = historyQuery.data?.entries ?? []
  const selectedId = selectedOpponentId ?? opponents[0]?.id ?? null
  const rank = arenaRankFromRating(profile?.rating ?? 0)
  const record = useMemo(() => summarizeHistory(history), [history])
  const loadout = useMemo(() => {
    const inventory = inventoryQuery.data
    if (!inventory) {
      return []
    }
    return LOADOUT_SLOTS.map((slot) => {
      const itemId = inventory.equipment.slots[slot]
      const item = itemId ? inventory.items.find((entry) => entry.id === itemId) : undefined
      return { slot, item }
    })
  }, [inventoryQuery.data])

  async function saveDefense(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!profile) {
      return
    }
    const form = new FormData(event.currentTarget)
    const defense: ArenaDefense = {
      preferredAction: String(form.get('preferredAction')) as CombatAction,
      preferredTechniqueCode: String(form.get('preferredTechniqueCode') || '') || null,
      healWhenHpPercentBelow: Number(form.get('healWhenHpPercentBelow')),
      defendWhenStaminaPercentBelow: Number(form.get('defendWhenStaminaPercentBelow')),
      finisherWhenEnemyHpPercentBelow: Number(form.get('finisherWhenEnemyHpPercentBelow')),
      finisherTechniqueCode: String(form.get('finisherTechniqueCode') || '') || null,
    }
    setError(null)
    try {
      await updateArenaDefense(defense)
      await queryClient.invalidateQueries({ queryKey: ['arena-profile'] })
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to save defense.')
    }
  }

  async function startArena(defenderId: string) {
    setError(null)
    try {
      await challengeArena(defenderId)
      onMatchStarted()
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to start the challenge.')
    }
  }

  async function startDuel(defenderId: string) {
    setError(null)
    try {
      await challengeDuel(defenderId)
      await queryClient.invalidateQueries({ queryKey: ['duel'] })
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to send the duel challenge.')
    }
  }

  async function inspect(id: string) {
    setError(null)
    setSelectedOpponentId(id)
    try {
      setInspected(await fetchPublicCharacter(id))
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'Unable to inspect that character.')
    }
  }

  if (profileQuery.isPending) {
    return <LoadingState>Opening the Arena…</LoadingState>
  }
  if (profileQuery.isError || !profile) {
    return <ErrorState onRetry={() => void profileQuery.refetch()}>Arena profile could not be loaded.</ErrorState>
  }

  const defenseForm = (
    <DefenseForm profile={profile} onSubmit={(event) => void saveDefense(event)} compact={tab === 'pvp'} />
  )

  return (
    <Panel
      id="pvp"
      className="game-column pvp-panel arena-dashboard"
      data-testid="arena-panel"
      title="Arena"
      actions={
        <ComingLaterButton className="arena-guide">
          Arena Guide
        </ComingLaterButton>
      }
    >
      <div className="arena-tabs" role="tablist" aria-label="Arena views">
        <TabButton id="pvp" current={tab} onSelect={setTab}>
          PvP Arena
        </TabButton>
        <TabButton id="pve" current={tab} onSelect={setTab}>
          PvE Arena
        </TabButton>
        <TabButton id="defense" current={tab} onSelect={setTab}>
          Defense Setup
        </TabButton>
        <TabButton id="history" current={tab} onSelect={setTab}>
          Battle History
        </TabButton>
        <TabButton id="rewards" current={tab} onSelect={setTab}>
          Rewards
        </TabButton>
      </div>

      <p className="visually-hidden" data-testid="arena-rating">
        Rating {profile.rating} · Marks {profile.marks}
      </p>
      {error ? <ErrorState>{error}</ErrorState> : null}

      {tab === 'pvp' ? (
        <div className="arena-pvp">
          <section className="arena-status-row" aria-label="Arena standing">
            <article className="arena-card arena-rank-card">
              <RankCrest tier={rank.tier} />
              <div>
                <p className="arena-kicker">Current Rank</p>
                <h3>{rank.name}</h3>
                <p className="muted">Rating {profile.rating.toLocaleString('en-US')}</p>
                {rank.nextName ? (
                  <>
                    <div
                      className="arena-meter"
                      role="progressbar"
                      aria-valuemin={0}
                      aria-valuemax={rank.needed}
                      aria-valuenow={rank.progress}
                      aria-label={`Progress to ${rank.nextName}`}
                    >
                      <span style={{ width: `${(rank.progress / rank.needed) * 100}%` }} />
                    </div>
                    <p className="muted">
                      {rank.progress}/{rank.needed} to {rank.nextName}
                    </p>
                  </>
                ) : (
                  <p className="muted">Highest displayed rank</p>
                )}
              </div>
            </article>

            <article className="arena-card arena-record-card">
              <StatChip label="Wins" value={record.wins} />
              <StatChip label="Losses" value={record.losses} />
              <StatChip label="Win Rate" value={record.winRate == null ? '—' : `${record.winRate}%`} />
              <StatChip label="Best Streak" value={record.bestStreak} />
              <div className="arena-streak">
                <span className="arena-streak-flame" aria-hidden="true" />
                <div>
                  <p className="arena-kicker">Current Streak</p>
                  <strong>
                    {record.currentStreak} {record.currentStreak === 1 ? 'Win' : 'Wins'}
                  </strong>
                </div>
              </div>
              <p className="muted arena-record-note">From recent battles on this page.</p>
            </article>

            <article className="arena-card arena-marks-card">
              <p className="arena-kicker">Arena Marks</p>
              <strong data-testid="arena-marks">{profile.marks.toLocaleString('en-US')}</strong>
              <p className="muted">Earned from ranked Arena fights. Weekly chests come later.</p>
            </article>

            <article className="arena-card arena-loadout-card">
              <div className="arena-card-head">
                <div>
                  <p className="arena-kicker">Loadout</p>
                  <h3>{characterQuery.data?.name ?? 'Your character'}</h3>
                </div>
                <Link to={gameLink('equipment')} className="btn btn-secondary">
                  View Loadout
                </Link>
              </div>
              <ul className="arena-loadout">
                {loadout.map(({ slot, item }) => (
                  <li key={slot} title={item?.displayName ?? slot.replace('_', ' ')}>
                    {item ? (
                      <ItemIcon item={item} className="item-icon" />
                    ) : (
                      <span className="arena-loadout-empty">{slotLabel(slot)}</span>
                    )}
                  </li>
                ))}
              </ul>
            </article>
          </section>

          <div className="arena-mid-row">
            <section className="arena-card arena-opponents-card" aria-labelledby="arena-opponents-heading">
              <div className="arena-card-head">
                <h3 id="arena-opponents-heading">Choose an Opponent</h3>
                <Button
                  type="button"
                  variant="secondary"
                  disabled={opponentsQuery.isFetching}
                  onClick={() => void opponentsQuery.refetch()}
                >
                  {opponentsQuery.isFetching ? 'Refreshing…' : 'Refresh Opponents'}
                </Button>
              </div>
              {opponentsQuery.isError ? (
                <ErrorState onRetry={() => void opponentsQuery.refetch()}>
                  Travel to the Arena to see available opponents.
                </ErrorState>
              ) : opponents.length ? (
                <ul className="arena-opponent-grid" data-testid="arena-opponents">
                  {opponents.map((opponent) => (
                    <li
                      key={opponent.id}
                      className={opponent.id === selectedId ? 'arena-opponent is-selected' : 'arena-opponent'}
                    >
                      <button
                        type="button"
                        className="arena-opponent-select"
                        onClick={() => setSelectedOpponentId(opponent.id)}
                      >
                        <CharacterPortrait className="arena-opponent-portrait" />
                        <strong>{opponent.name}</strong>
                        <span className="muted">Lv. {opponent.level}</span>
                        <span className="arena-opponent-rating">{opponent.rating.toLocaleString('en-US')} rating</span>
                      </button>
                      <p className="arena-opponent-rewards muted">Marks & rating on a ranked win</p>
                      <div className="arena-opponent-actions">
                        <Button type="button" variant="secondary" onClick={() => void inspect(opponent.id)}>
                          Inspect
                        </Button>
                        <Button type="button" onClick={() => void startArena(opponent.id)}>
                          Challenge
                        </Button>
                        <Button type="button" variant="ghost" onClick={() => void startDuel(opponent.id)}>
                          Duel
                        </Button>
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <EmptyState testId="arena-opponents-empty">No opponents are listed yet.</EmptyState>
              )}
            </section>

            <PvePreviewColumn onOpen={() => setTab('pve')} />
          </div>

          <div className="arena-bottom-row">
            <section className="arena-card" aria-labelledby="arena-defense-heading">
              <div className="arena-card-head">
                <div>
                  <h3 id="arena-defense-heading">Arena Defense</h3>
                  <p className="muted">
                    {characterQuery.data
                      ? `Armor ${characterQuery.data.derivedStats.armor} · Damage ${characterQuery.data.derivedStats.physicalDamage}`
                      : 'Offline defenders use this strategy.'}
                  </p>
                </div>
                <Button type="button" variant="secondary" onClick={() => setTab('defense')}>
                  Edit Defense
                </Button>
              </div>
              {defenseForm}
            </section>

            <HistoryCard
              entries={history}
              loading={historyQuery.isPending}
              onOpen={() => setTab('history')}
            />

            <section className="arena-card" aria-labelledby="arena-ladder-heading">
              <h3 id="arena-ladder-heading">Rating Ladder</h3>
              <p className="arena-your-rank">
                Your rating <strong>{profile.rating.toLocaleString('en-US')}</strong>
              </p>
              <p className="muted">Server placements come later.</p>
              <ComingLaterButton className="btn btn-secondary">Open Rankings</ComingLaterButton>
            </section>
          </div>
        </div>
      ) : null}

      {tab === 'pve' ? (
        <section className="arena-card arena-pve-page">
          <h3>PvE Arena</h3>
          <p className="muted">
            Training Grounds will use generated NPC opponents on the live combat engine. These boards are a preview.
          </p>
          <div className="arena-pve-grid arena-pve-grid-wide">
            {PVE_PREVIEWS.map((entry) => (
              <PveCard key={entry.title} {...entry} />
            ))}
          </div>
        </section>
      ) : null}

      {tab === 'defense' ? (
        <section className="arena-card arena-defense-page">
          <h3>Defense Setup</h3>
          <p className="muted">Attackers fight this scripted defense while you are away.</p>
          {defenseForm}
        </section>
      ) : null}

      {tab === 'history' ? (
        <section className="arena-card">
          <h3>Battle History</h3>
          <HistoryList entries={history} empty />
        </section>
      ) : null}

      {tab === 'rewards' ? (
        <section className="arena-card arena-rewards-page">
          <h3>Rewards</h3>
          <p>
            Ranked Arena pays <strong>marks</strong> and rating. You currently hold{' '}
            <strong>{profile.marks.toLocaleString('en-US')}</strong> marks.
          </p>
          <p className="muted">Season tracks and weekly chests are not live yet.</p>
        </section>
      ) : null}

      {inspected ? (
        <aside className="arena-inspect" data-testid="public-inspect">
          <div className="arena-card-head">
            <h3>{inspected.name}</h3>
            <Button type="button" variant="ghost" onClick={() => setInspected(null)}>
              Close
            </Button>
          </div>
          <p>
            Level {inspected.level} · Rating {inspected.arenaRating}
          </p>
          <p>
            STR {inspected.strength} AGI {inspected.agility} END {inspected.endurance} PER {inspected.perception}
          </p>
          <p>
            {inspected.weaponFamily ?? 'Unarmed'} mastery {inspected.weaponMasteryLevel ?? 0}
          </p>
          <ul>
            {inspected.equipment.map((item) => (
              <li key={item.slot}>
                {item.slot}: {item.displayName}
              </li>
            ))}
          </ul>
        </aside>
      ) : null}
    </Panel>
  )
}

function TabButton({
  id,
  current,
  onSelect,
  children,
}: {
  id: ArenaTab
  current: ArenaTab
  onSelect: (tab: ArenaTab) => void
  children: string
}) {
  return (
    <button
      type="button"
      role="tab"
      aria-selected={current === id}
      className={current === id ? 'tab tab-active' : 'tab'}
      data-testid={`arena-tab-${id}`}
      onClick={() => onSelect(id)}
    >
      {children}
    </button>
  )
}

function StatChip({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="arena-stat">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  )
}

function RankCrest({ tier }: { tier: string }) {
  return (
    <svg className={`arena-crest arena-crest-${tier.toLowerCase()}`} viewBox="0 0 64 72" aria-hidden="true">
      <path
        d="M32 4 56 14v22c0 16-10.4 26.8-24 32C18.4 62.8 8 52 8 36V14Z"
        fill="currentColor"
        fillOpacity="0.22"
        stroke="currentColor"
        strokeWidth="2.4"
      />
      <path d="M32 16 44 22v12c0 8-5.2 13.4-12 16-6.8-2.6-12-8-12-16V22Z" fill="currentColor" fillOpacity="0.55" />
    </svg>
  )
}

function PvePreviewColumn({ onOpen }: { onOpen: () => void }) {
  return (
    <section className="arena-card arena-pve-column" aria-labelledby="arena-pve-heading">
      <div className="arena-card-head">
        <h3 id="arena-pve-heading">PvE Arena</h3>
        <Button type="button" variant="ghost" onClick={onOpen}>
          View
        </Button>
      </div>
      <div className="arena-pve-stack">
        {PVE_PREVIEWS.map((entry) => (
          <PveCard key={entry.title} {...entry} compact />
        ))}
      </div>
    </section>
  )
}

function PveCard({
  title,
  location,
  difficulty,
  waves,
  compact = false,
}: {
  title: string
  location: string
  difficulty: string
  waves: string
  compact?: boolean
}) {
  return (
    <article className={compact ? 'arena-pve-card is-compact' : 'arena-pve-card'}>
      <div className="arena-pve-art" style={{ backgroundImage: `url(${locationArtUrl(location)})` }} />
      <div className="arena-pve-copy">
        <h4>{title}</h4>
        <p className="muted">
          {waves} · {difficulty}
        </p>
        <ComingLaterButton className="btn btn-secondary">Enter PvE Arena</ComingLaterButton>
      </div>
    </article>
  )
}

function HistoryCard({
  entries,
  loading,
  onOpen,
}: {
  entries: HistoryEntry[]
  loading: boolean
  onOpen: () => void
}) {
  return (
    <section className="arena-card" aria-labelledby="arena-history-heading">
      <div className="arena-card-head">
        <h3 id="arena-history-heading">Recent Battles</h3>
        <Button type="button" variant="ghost" onClick={onOpen}>
          View all
        </Button>
      </div>
      {loading ? <LoadingState>Loading battles…</LoadingState> : <HistoryList entries={entries.slice(0, 5)} />}
    </section>
  )
}

type HistoryEntry = {
  matchId: string
  opponentName: string
  result: string
  ratingDelta: number
  createdAt: string
}

function HistoryList({ entries, empty = false }: { entries: HistoryEntry[]; empty?: boolean }) {
  if (!entries.length) {
    return empty ? <EmptyState>No battles recorded yet.</EmptyState> : <p className="muted">No battles recorded yet.</p>
  }
  return (
    <ul className="arena-history" data-testid="pvp-history">
      {entries.map((entry) => (
        <li key={`${entry.matchId}-${entry.createdAt}`}>
          <span className={entry.result === 'WIN' ? 'arena-result-win' : 'arena-result-loss'}>{entry.result}</span>
          <span>vs {entry.opponentName}</span>
          <span className="muted">
            {entry.ratingDelta ? `Rating ${entry.ratingDelta > 0 ? '+' : ''}${entry.ratingDelta}` : 'Unranked'}
          </span>
          <time className="muted" dateTime={entry.createdAt}>
            {formatWhen(entry.createdAt)}
          </time>
          <ComingLaterButton className="btn btn-ghost">Watch</ComingLaterButton>
        </li>
      ))}
    </ul>
  )
}

function DefenseForm({
  profile,
  onSubmit,
  compact,
}: {
  profile: {
    defense: ArenaDefense
    preferredActionOptions: CombatAction[]
  }
  onSubmit: (event: React.FormEvent<HTMLFormElement>) => void
  compact: boolean
}) {
  return (
    <form
      onSubmit={onSubmit}
      data-testid="arena-defense-form"
      className={compact ? 'arena-defense-form is-compact' : 'arena-defense-form'}
    >
      {compact ? null : <h3>Defense strategy</h3>}
      <Field label="Preferred action">
        <select name="preferredAction" defaultValue={profile.defense.preferredAction}>
          {profile.preferredActionOptions.map((action) => (
            <option key={action} value={action}>
              {action}
            </option>
          ))}
        </select>
      </Field>
      <Field label="Preferred technique">
        <input name="preferredTechniqueCode" defaultValue={profile.defense.preferredTechniqueCode ?? ''} />
      </Field>
      <Field label="Heal when HP below %">
        <input
          name="healWhenHpPercentBelow"
          type="number"
          min={0}
          max={100}
          defaultValue={profile.defense.healWhenHpPercentBelow}
        />
      </Field>
      <Field label="Defend when stamina below %">
        <input
          name="defendWhenStaminaPercentBelow"
          type="number"
          min={0}
          max={100}
          defaultValue={profile.defense.defendWhenStaminaPercentBelow}
        />
      </Field>
      <Field label="Finisher when enemy HP below %">
        <input
          name="finisherWhenEnemyHpPercentBelow"
          type="number"
          min={0}
          max={100}
          defaultValue={profile.defense.finisherWhenEnemyHpPercentBelow}
        />
      </Field>
      <Field label="Finisher technique">
        <input name="finisherTechniqueCode" defaultValue={profile.defense.finisherTechniqueCode ?? ''} />
      </Field>
      <Button type="submit">Save defense</Button>
    </form>
  )
}

function summarizeHistory(entries: HistoryEntry[]) {
  const decided = entries.filter((entry) => entry.result === 'WIN' || entry.result === 'LOSS')
  const wins = decided.filter((entry) => entry.result === 'WIN').length
  const losses = decided.filter((entry) => entry.result === 'LOSS').length
  let currentStreak = 0
  for (const entry of decided) {
    if (entry.result !== 'WIN') {
      break
    }
    currentStreak += 1
  }
  let bestStreak = 0
  let running = 0
  for (const entry of decided) {
    if (entry.result === 'WIN') {
      running += 1
      bestStreak = Math.max(bestStreak, running)
    } else {
      running = 0
    }
  }
  return {
    wins,
    losses,
    winRate: decided.length ? Math.round((wins / decided.length) * 100) : null,
    currentStreak,
    bestStreak,
  }
}

function slotLabel(slot: string) {
  return slot
    .toLowerCase()
    .split('_')
    .map((part) => part[0]?.toUpperCase() + part.slice(1))
    .join(' ')
}

function formatWhen(iso: string): string {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) {
    return iso
  }
  return date.toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

