import { useState } from 'react'
import { ActivityRow } from '../ui/ActivityRow'
import { CompactDataRow } from '../ui/CompactDataRow'
import { Button } from '../ui/Button'
import { CharacterPortrait } from '../ui/CharacterPortrait'
import { ChromeIcon, type ChromeIconName } from '../ui/chromeIcons'
import { CounterBadge } from '../ui/CounterBadge'
import { EquipmentLayout } from '../ui/EquipmentLayout'
import { GenericRow } from '../ui/GenericRow'
import { NotificationRow } from '../ui/NotificationRow'
import { HealthBar } from '../ui/HealthBar'
import { IconButton } from '../ui/IconButton'
import { locationArtUrl } from '../ui/locationMedia'
import { Panel } from '../ui/Panel'
import { MainShellGameTopBar } from './mainShell/MainShellGameTopBar'
import { MainShellLocationPanel } from './mainShell/MainShellLocationPanel'
import { ProgressBar } from '../ui/ProgressBar'
import { Section } from '../ui/Section'
import { StaminaBar } from '../ui/StaminaBar'
import { StatusBadge } from '../ui/StatusBadge'
import { Tabs } from '../ui/Tabs'
import { TextInput } from '../ui/TextInput'
import { UiIcon } from '../ui/UiIcon'
import { XPBar } from '../ui/XPBar'
import { activityIconUrl, activityMessageParts, activityRowVariant, formatRelativeTime } from '../ui/activityMedia'
import { classNames } from '../ui/classNames'
import { persistNavCollapsed, readStoredNavCollapsed } from '../ui/navCollapse'
import { applyUiMode, persistUiMode, readStoredUiMode, type UiMode } from '../ui/uiMode'
import { DEV_UI_MAIN_SHELL_PATH, devUiNavPath } from './devUi'
import {
  MAIN_SHELL_VISUAL_NOW,
  mainShellActivity,
  mainShellAlerts,
  mainShellCharacter,
  mainShellChatChannels,
  mainShellChatMessages,
  mainShellClaimable,
  mainShellEvents,
  mainShellExpeditions,
  mainShellGuild,
  mainShellInventory,
  mainShellNotifications,
  mainShellObjectives,
  type MainShellNoticeVisual,
} from './mainShellVisualFixture'

const LIVE_NAV: { id: string; label: string; icon: ChromeIconName }[] = [
  { id: 'home', label: 'Home', icon: 'home' },
  { id: 'world', label: 'Locations', icon: 'locations' },
  { id: 'quests', label: 'Quests', icon: 'quests' },
  { id: 'character', label: 'Character', icon: 'character' },
  { id: 'inventory', label: 'Inventory', icon: 'pack' },
  { id: 'equipment', label: 'Equipment', icon: 'equipment' },
  { id: 'market', label: 'Market', icon: 'market' },
  { id: 'crafting', label: 'Crafting', icon: 'crafting' },
  { id: 'expeditions', label: 'Expeditions', icon: 'expeditions' },
  { id: 'mastery', label: 'Mastery', icon: 'mastery' },
  { id: 'pvp', label: 'PvP', icon: 'pvp' },
]

const LATER_NAV: { id: string; label: string; icon: ChromeIconName }[] = [
  { id: 'guild', label: 'Guild', icon: 'guild' },
  { id: 'rankings', label: 'Rankings', icon: 'rankings' },
]

function formatAmount(value: number): string {
  return value.toLocaleString('en-US')
}

export { MainShellGameTopBar as MainShellTopBar }

export function MainShellLeftNav({ activeId = 'home' }: { activeId?: string }) {
  const [uiMode, setUiMode] = useState<UiMode>(() => readStoredUiMode())
  const [collapsed, setCollapsed] = useState(() => readStoredNavCollapsed())
  const officeOn = uiMode === 'compact'

  return (
    <nav
      className={classNames('ms-nav', 'surface-base', collapsed && 'is-collapsed')}
      aria-label="Primary"
      data-collapsed={collapsed ? 'true' : undefined}
    >
      <p className="type-section-heading">Navigation</p>
      <ul className="ms-nav-list">
        {LIVE_NAV.map((entry) => (
          <li key={entry.id}>
            <a
              href={devUiNavPath(entry.id)}
              data-testid={`nav-${entry.id}`}
              className={classNames('ms-nav-link', entry.id === activeId && 'ui-nav-selected')}
              aria-current={entry.id === activeId ? 'page' : undefined}
              title={collapsed ? entry.label : undefined}
            >
              <UiIcon size="sm" state={entry.id === activeId ? 'active' : 'default'}>
                <ChromeIcon name={entry.icon} className="" />
              </UiIcon>
              <span className="type-compact">{entry.label}</span>
            </a>
          </li>
        ))}
        {LATER_NAV.map((entry) => (
          <li key={entry.id}>
            <button type="button" className="ms-nav-link" data-testid={`nav-${entry.id}`} disabled title={collapsed ? entry.label : undefined}>
              <UiIcon size="sm" state="disabled">
                <ChromeIcon name={entry.icon} className="" />
              </UiIcon>
              <span className="type-compact">{entry.label}</span>
            </button>
          </li>
        ))}
      </ul>

      <p className="type-section-heading">Quick actions</p>
      <ul className="ms-nav-list">
        <li>
          <a href={DEV_UI_MAIN_SHELL_PATH} className="ms-nav-link" data-testid="quick-tavern">
            <UiIcon size="sm">
              <ChromeIcon name="tavern" className="" />
            </UiIcon>
            <span>
              <span className="type-compact">Visit Tavern</span>
              <span className="type-meta">Social & group</span>
            </span>
          </a>
        </li>
        <li>
          <button type="button" className="ms-nav-link" data-testid="quick-daily" disabled>
            <UiIcon size="sm" state="disabled">
              <ChromeIcon name="daily" className="" />
            </UiIcon>
            <span>
              <span className="type-compact">Daily Rewards</span>
              <span className="type-meta">Available later</span>
            </span>
          </button>
        </li>
        <li>
          <a href={DEV_UI_MAIN_SHELL_PATH} className="ms-nav-link" data-testid="quick-claim-expedition">
            <UiIcon size="sm">
              <ChromeIcon name="expeditions" className="" />
            </UiIcon>
            <span>
              <span className="type-compact">Claim Expedition</span>
              <span className="type-meta">Rewards</span>
            </span>
          </a>
        </li>
        <li>
          <a href={DEV_UI_MAIN_SHELL_PATH} className="ms-nav-link" data-testid="quick-market">
            <UiIcon size="sm">
              <ChromeIcon name="market" className="" />
            </UiIcon>
            <span>
              <span className="type-compact">Market Deals</span>
              <span className="type-meta">Open marketplace</span>
            </span>
          </a>
        </li>
        <li>
          <a href={DEV_UI_MAIN_SHELL_PATH} className="ms-nav-link" data-testid="quick-travel">
            <UiIcon size="sm">
              <ChromeIcon name="travel" className="" />
            </UiIcon>
            <span>
              <span className="type-compact">Travel</span>
              <span className="type-meta">Change location</span>
            </span>
          </a>
        </li>
      </ul>

      <div className="ms-nav-foot">
        <Button
          type="button"
          variant="ghost"
          data-testid="ui-mode-toggle"
          aria-pressed={officeOn}
          onClick={() => {
            const next: UiMode = uiMode === 'compact' ? 'normal' : 'compact'
            setUiMode(next)
            persistUiMode(next)
            applyUiMode(next)
          }}
        >
          Office mode
        </Button>
        <div className="ms-nav-status">
          <span className="type-meta" data-testid="nav-online">
            42 Online
          </span>
          <IconButton
            label={collapsed ? 'Expand navigation' : 'Collapse navigation'}
            data-testid="nav-collapse"
            aria-pressed={collapsed}
            onClick={() => {
              const next = !collapsed
              setCollapsed(next)
              persistNavCollapsed(next)
            }}
          >
            <UiIcon size="sm">
              <ChromeIcon name="collapse" className="" />
            </UiIcon>
          </IconButton>
        </div>
      </div>
    </nav>
  )
}

export { MainShellLocationPanel as MainShellLocationHero }

export function MainShellCharacterOverview() {
  const character = mainShellCharacter
  return (
    <Panel data-testid="character-summary" title="Character Overview">
      <div className="visually-hidden">
        <h3 data-testid="character-summary-name">{character.name}</h3>
        <p data-testid="character-summary-level">Level {character.level}</p>
      </div>
      <div className="ms-character">
        <CharacterPortrait className="ms-character-art" avatarCode={character.avatarCode} />
        <div className="ms-vitals">
          <div>
            <div className="ms-vital-head">
              <span className="type-micro">Health</span>
              <span className="type-numeric">
                {formatAmount(character.currentHealth)} / {formatAmount(character.maxHealth)}
              </span>
            </div>
            <HealthBar
              max={character.maxHealth}
              value={character.currentHealth}
              label={`Health ${character.currentHealth} of ${character.maxHealth}`}
            />
          </div>
          <div>
            <div className="ms-vital-head">
              <span className="type-micro">Stamina</span>
              <span className="type-numeric">
                {character.currentStamina} / {character.maxStamina}
              </span>
            </div>
            <StaminaBar
              max={character.maxStamina}
              value={character.currentStamina}
              label={`Stamina ${character.currentStamina} of ${character.maxStamina}`}
            />
          </div>
        </div>
      </div>
      <div className="ms-stat-row">
        <div>
          <span className="type-micro">XP</span>
          <strong className="type-numeric type-numeric-gold" data-testid="overview-total-xp">
            {formatAmount(character.progression.totalExperience)}
          </strong>
        </div>
        <div>
          <span className="type-micro">STR</span>
          <strong className="type-numeric" data-testid="character-summary-strength">
            {character.strength}
          </strong>
        </div>
        <div>
          <span className="type-micro">AGI</span>
          <strong className="type-numeric" data-testid="character-summary-agility">
            {character.agility}
          </strong>
        </div>
        <div>
          <span className="type-micro">END</span>
          <strong className="type-numeric" data-testid="character-summary-endurance">
            {character.endurance}
          </strong>
        </div>
        <div>
          <span className="type-micro">PER</span>
          <strong className="type-numeric" data-testid="character-summary-perception">
            {character.perception}
          </strong>
        </div>
      </div>
      <Button type="button" variant="secondary" data-testid="view-character">
        View Character
      </Button>
    </Panel>
  )
}

export function MainShellEquipmentPreview() {
  return (
    <Panel title="Equipment" data-testid="equipment-overview">
      <EquipmentLayout
        testId="home-equipment"
        showStage
        includeFutureSlots
        figureGender="MALE"
        equipment={mainShellInventory.equipment}
        items={mainShellInventory.items}
      />
      <Button type="button" variant="secondary" data-testid="view-full-equipment">
        View Full Equipment
      </Button>
    </Panel>
  )
}

export function MainShellExpeditions() {
  return (
    <Panel data-testid="expedition-overview" title="Active Expeditions" actions={<span className="type-meta">2/3</span>}>
      <ul className="ms-stack">
        {mainShellExpeditions.map((expedition) => (
          <li key={expedition.id} className="ms-expedition" data-testid={`expedition-${expedition.id}`}>
            <div
              className="ms-thumb surface-inset"
              style={{ backgroundImage: `url(${locationArtUrl(expedition.artCode)})` }}
              aria-hidden="true"
            />
            <div className="ms-expedition-copy">
              <p className="type-item">{expedition.name}</p>
              <p className="type-meta">{expedition.remaining}</p>
              <ProgressBar
                value={expedition.progressPercent}
                max={100}
                label={`${expedition.name} ${expedition.progressPercent}% complete`}
              />
              <p className="ms-rewards type-meta">
                <span>
                  <img src="/chrome/currency-silver.webp" alt="" /> {formatAmount(expedition.rewards.silver)} Silver
                </span>
                <span>
                  <img src="/icons/activity/scroll.webp" alt="" /> {formatAmount(expedition.rewards.xp)} XP
                </span>
                <span>
                  <img src="/chrome/currency-honor.webp" alt="" /> {formatAmount(expedition.rewards.marks)} Marks
                </span>
              </p>
              <Button type="button" variant="secondary">
                View
              </Button>
            </div>
          </li>
        ))}
      </ul>
      <Button type="button" variant="secondary" data-testid="view-expeditions">
        Start New Expedition
      </Button>
    </Panel>
  )
}

export function MainShellObjectives() {
  return (
    <Panel title="Daily Objectives" data-testid="daily-objectives">
      <ul className="ms-stack ui-row-list">
        {mainShellObjectives.map((objective) => {
          const complete = objective.current >= objective.required
          return (
            <GenericRow
              as="li"
              key={objective.id}
              testId={`objective-${objective.id}`}
              primary={objective.name}
              secondary={`${objective.current}/${objective.required}`}
              metadata={complete ? <StatusBadge tone="safe">Complete</StatusBadge> : null}
              action={
                <XPBar
                  max={objective.required}
                  value={objective.current}
                  label={`${objective.name} ${objective.current} of ${objective.required}`}
                  density="compact"
                />
              }
            />
          )
        })}
      </ul>
    </Panel>
  )
}

export function MainShellEvents() {
  return (
    <Panel title="World Events" data-testid="world-events">
      <ul className="ms-stack ui-row-list">
        {mainShellEvents.map((event) => (
          <GenericRow
            as="li"
            key={event.id}
            testId={`event-${event.id}`}
            primary={event.name}
            metadata={<StatusBadge tone={event.tone}>{event.timing}</StatusBadge>}
          />
        ))}
      </ul>
    </Panel>
  )
}

export function MainShellGuild() {
  const guild = mainShellGuild
  return (
    <Panel data-testid="guild-placeholder" title="My Guild">
      <div className="ms-guild">
        <img className="ms-guild-crest" src={guild.crestUrl} alt="" />
        <div>
          <p className="type-item">{guild.name}</p>
          <p className="type-meta">Level {guild.level}</p>
        </div>
      </div>
      <XPBar
        max={guild.xpMax}
        value={guild.xp}
        label={`Guild XP ${guild.xp} of ${guild.xpMax}`}
        showValue
        valueText={`${formatAmount(guild.xp)} / ${formatAmount(guild.xpMax)} XP`}
      />
      <dl className="ms-ledger">
        <div>
          <dt className="type-meta">Members</dt>
          <dd className="type-numeric">
            {guild.members} / {guild.memberCap}
          </dd>
        </div>
        <div>
          <dt className="type-meta">Guild Power</dt>
          <dd className="type-numeric">{formatAmount(guild.power)}</dd>
        </div>
        <div>
          <dt className="type-meta">Territories</dt>
          <dd className="type-numeric">{guild.territories}</dd>
        </div>
      </dl>
      <Button type="button" variant="secondary" data-testid="guild-overview" disabled>
        Guild Overview
      </Button>
    </Panel>
  )
}

function NoticeRow({ notice }: { notice: MainShellNoticeVisual }) {
  return (
    <NotificationRow
      as="li"
      variant={notice.variant}
      unread={Boolean(notice.action) || notice.variant === 'warning'}
      testId={notice.id}
      icon={<img src={activityIconUrl(notice.iconType)} alt="" />}
      primary={notice.primary}
      secondary={notice.secondary}
      action={
        notice.action ? (
          <Button type="button" variant="primary">
            {notice.action}
          </Button>
        ) : null
      }
    />
  )
}

export function MainShellActivity() {
  const nowMs = Date.parse(MAIN_SHELL_VISUAL_NOW)
  return (
    <Panel as="aside" className="ms-rail" data-testid="activity-panel" aria-label="Activity" title="Activity & Notifications">
      <Section title="Recent Events">
        <ul className="ms-stack ui-row-list" data-testid="activity-list">
          {mainShellActivity.map((entry) => (
            <ActivityRow
              key={entry.id}
              variant={activityRowVariant(entry.type)}
              testId={`activity-${entry.type}`}
              icon={<img src={activityIconUrl(entry.type)} alt="" />}
              primary={activityMessageParts(entry.type, entry.message).map((part, index) =>
                part.tone === 'plain' ? (
                  part.text
                ) : (
                  <span key={`${entry.id}-${index}`} className={`activity-hl-${part.tone}`}>
                    {part.text}
                  </span>
                ),
              )}
              metadata={<time dateTime={entry.createdAt}>{formatRelativeTime(entry.createdAt, nowMs)}</time>}
            />
          ))}
        </ul>
      </Section>
      <Section
        title={
          <>
            Claimable Rewards
            <CounterBadge count={mainShellClaimable.length} tone="danger" />
          </>
        }
      >
        <ul className="ms-stack ui-row-list" data-testid="activity-claimable">
          {mainShellClaimable.map((notice) => (
            <NoticeRow key={notice.id} notice={notice} />
          ))}
        </ul>
      </Section>
      <Section
        title={
          <>
            Notifications
            <CounterBadge count={mainShellNotifications.length} tone="danger" />
          </>
        }
      >
        <ul className="ms-stack ui-row-list">
          {mainShellNotifications.map((notice) => (
            <NoticeRow key={notice.id} notice={notice} />
          ))}
        </ul>
      </Section>
      <Section title="Alerts">
        <ul className="ms-stack ui-row-list" data-testid="activity-alerts">
          {mainShellAlerts.map((notice) => (
            <NoticeRow key={notice.id} notice={notice} />
          ))}
        </ul>
      </Section>
      <Button type="button" variant="secondary" data-testid="activity-view-all">
        View All Notifications
      </Button>
    </Panel>
  )
}

function formatWhen(iso: string): string {
  return new Date(iso).toLocaleTimeString('en-GB', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
    timeZone: 'UTC',
  })
}

function nameClass(name: string): string {
  if (name.toLowerCase() === 'system') {
    return 'type-numeric type-numeric-gold'
  }
  return 'type-item'
}

function renderBody(body: string) {
  return body.split(/(\[[^\]]+\])/).map((part, index) =>
    part.startsWith('[') && part.endsWith(']') ? (
      <span key={index} className="type-numeric type-numeric-gold">
        {part}
      </span>
    ) : (
      part
    ),
  )
}

type ChatChannelId = (typeof mainShellChatChannels)[number]['id']

export function MainShellChat() {
  const [channel, setChannel] = useState<ChatChannelId>('global')
  return (
    <Panel className="ms-chat" data-testid="chat-panel" aria-label="Global chat" title="Global chat">
      <Tabs<ChatChannelId>
        label="Chat channels"
        value={channel}
        onChange={setChannel}
        tabs={mainShellChatChannels.map((entry) => ({
          id: entry.id,
          label: `${entry.label} ${entry.unread}`,
        }))}
      />
      <div className="surface-inset ms-chat-log">
        <ul className="ms-stack ui-row-list" data-testid="chat-list">
          {mainShellChatMessages.map((message) => (
            <CompactDataRow
              key={message.id}
              testId={`chat-message-${message.id}`}
              tone={message.characterName.toLowerCase() === 'system' ? 'secondary' : 'default'}
              interactive={false}
              primary={
                <>
                  <time className="type-meta" dateTime={message.createdAt}>
                    {formatWhen(message.createdAt)}
                  </time>{' '}
                  <strong className={nameClass(message.characterName)}>[{message.characterName}]</strong>{' '}
                  {renderBody(message.body)}
                </>
              }
            />
          ))}
        </ul>
      </div>
      <form className="ms-chat-form" onSubmit={(event) => event.preventDefault()}>
        <TextInput name="body" placeholder="Type your message..." data-testid="chat-input" autoComplete="off" />
        <Button type="submit" variant="primary" data-testid="chat-send" disabled>
          Send
        </Button>
      </form>
    </Panel>
  )
}
