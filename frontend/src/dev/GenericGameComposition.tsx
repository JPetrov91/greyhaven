import { useState } from 'react'
import { ActivityRow } from '../ui/ActivityRow'
import { Button } from '../ui/Button'
import { CharacterPortrait } from '../ui/CharacterPortrait'
import { ChromeHint } from '../ui/ChromeHint'
import { ChromeIcon, type ChromeIconName } from '../ui/chromeIcons'
import { CompactDataRow } from '../ui/CompactDataRow'
import { CounterBadge } from '../ui/CounterBadge'
import { IconButton } from '../ui/IconButton'
import { NotificationRow } from '../ui/NotificationRow'
import { Ornament } from '../ui/Ornament'
import { Panel } from '../ui/Panel'
import { SearchInput } from '../ui/SearchInput'
import { Section } from '../ui/Section'
import { Select } from '../ui/Select'
import { Tabs } from '../ui/Tabs'
import { TextInput } from '../ui/TextInput'
import { Tooltip } from '../ui/Tooltip'
import { UiIcon } from '../ui/UiIcon'
import { XPBar } from '../ui/XPBar'
import { classNames } from '../ui/classNames'
import { applyUiMode, persistUiMode, readStoredUiMode, type UiMode } from '../ui/uiMode'

type RecordTab = 'open' | 'sealed' | 'archive'
type ChatTab = 'global' | 'local' | 'watch'

type LedgerEntry = {
  id: string
  tab: RecordTab
  name: string
  kind: string
  status: string
  weight: string
  when: string
}

const NAV: { id: string; label: string; icon: ChromeIconName; active?: boolean }[] = [
  { id: 'home', label: 'Home', icon: 'home' },
  { id: 'locations', label: 'Locations', icon: 'locations' },
  { id: 'character', label: 'Character', icon: 'character' },
  { id: 'records', label: 'Records', icon: 'quests', active: true },
  { id: 'notices', label: 'Notices', icon: 'mail' },
  { id: 'hall', label: 'Hall', icon: 'guild' },
]

const QUICK: { id: string; label: string; meta: string; icon: ChromeIconName }[] = [
  { id: 'lamp', label: 'Light lamps', icon: 'daily', meta: 'Harbour duty' },
  { id: 'roster', label: 'Open roster', icon: 'friends', meta: 'Watch list' },
  { id: 'seal', label: 'Seal notice', icon: 'crafting', meta: 'Clerk desk' },
]

const LEDGER: LedgerEntry[] = [
  { id: 'r1', tab: 'open', name: 'Harbour lamp duty', kind: 'Watch', status: 'Open', weight: '12', when: '2m' },
  { id: 'r2', tab: 'open', name: 'North road quiet', kind: 'Report', status: 'Open', weight: '4', when: '11m' },
  { id: 'r3', tab: 'open', name: 'Frost on the quay', kind: 'Notice', status: 'Open', weight: '7', when: '28m' },
  { id: 'r4', tab: 'sealed', name: 'Sealed: inner gate', kind: 'Seal', status: 'Sealed', weight: '18', when: '1h' },
  { id: 'r5', tab: 'archive', name: 'Old stipend roll', kind: 'Archive', status: 'Filed', weight: '2', when: '3d' },
  { id: 'r6', tab: 'open', name: 'Missing crate mark', kind: 'Watch', status: 'Open', weight: '9', when: '4h' },
  { id: 'r7', tab: 'open', name: 'Bell tally — dusk', kind: 'Report', status: 'Open', weight: '3', when: '6h' },
]

const CHAT: Record<ChatTab, { time: string; name: string; tone: string; text: string }[]> = {
  global: [
    { time: '15:41', name: 'Clerk Venn', tone: 'gold', text: 'Lamp oil is short on the east quay.' },
    { time: '15:42', name: 'System', tone: 'info', text: 'Watch rotation changes at dusk.' },
    { time: '15:43', name: 'Mara', tone: 'safe', text: 'North road is quiet. No tracks.' },
  ],
  local: [
    { time: '15:40', name: 'Edric', tone: 'gold', text: 'Seal the inner notice before the bell.' },
    { time: '15:44', name: 'Hale', tone: 'body', text: 'Roster is updated. Two names missing.' },
  ],
  watch: [
    { time: '15:38', name: 'Watch', tone: 'info', text: 'Frost report filed under Open.' },
    { time: '15:45', name: 'System', tone: 'info', text: 'Archive write is locked until dawn.' },
  ],
}

function activityArt(name: string): string {
  return `/icons/activity/${name}`
}

export function GenericGameComposition() {
  const [uiMode, setUiMode] = useState<UiMode>(() => readStoredUiMode())
  const [tab, setTab] = useState<RecordTab>('open')
  const [selectedId, setSelectedId] = useState('r1')
  const [query, setQuery] = useState('')
  const [kind, setKind] = useState('all')
  const [chatTab, setChatTab] = useState<ChatTab>('global')
  const [draft, setDraft] = useState('')
  const [recordTip, setRecordTip] = useState(false)
  const officeOn = uiMode === 'compact'
  const selected = LEDGER.find((entry) => entry.id === selectedId) ?? LEDGER[0]
  const rows = LEDGER.filter((entry) => entry.tab === tab).filter((entry) => {
    const hay = `${entry.name} ${entry.kind}`.toLowerCase()
    const matchQuery = query.trim() === '' || hay.includes(query.trim().toLowerCase())
    const matchKind = kind === 'all' || entry.kind.toLowerCase() === kind
    return matchQuery && matchKind
  })

  return (
    <section className="ui-shell surface-page" aria-label="Generic game composition" data-testid="ui-shell">
      <header className="ui-shell-topbar surface-raised">
        <div className="ui-shell-top-left">
          <a href="#composition" className="brand type-display">
            <img src="/auth/crest.svg" alt="" className="brand-crest" />
            Greyhaven
          </a>
          <span className="ui-shell-top-rule" aria-hidden="true" />
          <div className="ui-shell-identity">
            <CharacterPortrait className="ui-shell-portrait" avatarCode="male_unyielding" />
            <div className="ui-shell-identity-copy">
              <p className="type-item">Edric Varn</p>
              <p className="type-meta">Level 7 · Clerk</p>
              <XPBar label="Experience 62 percent" value={62} density="compact" />
            </div>
          </div>
        </div>
        <div className="ui-shell-currencies" aria-label="Currencies">
          <span className="ui-shell-coin surface-inset">
            <img src="/chrome/currency-silver.webp" alt="" />
            <span>
              <span className="type-micro">Silver</span>
              <strong className="type-numeric type-numeric-gold">12,480</strong>
            </span>
          </span>
          <span className="ui-shell-coin surface-inset">
            <img src="/chrome/currency-gold.webp" alt="" />
            <span>
              <span className="type-micro">Gold</span>
              <strong className="type-numeric type-numeric-gold">248</strong>
            </span>
          </span>
          <span className="ui-shell-coin surface-inset">
            <img src="/chrome/currency-honor.webp" alt="" />
            <span>
              <span className="type-micro">Marks</span>
              <strong className="type-numeric type-numeric-gold">36</strong>
            </span>
          </span>
          <span className="ui-shell-coin surface-inset">
            <img src="/chrome/currency-credits.webp" alt="" />
            <span>
              <span className="type-micro">Credits</span>
              <strong className="type-numeric type-numeric-gold">90</strong>
            </span>
          </span>
        </div>
        <div className="ui-shell-utilities">
          <ChromeHint label="Trophy">
            <IconButton label="Trophy">
              <UiIcon>
                <ChromeIcon name="trophy" />
              </UiIcon>
            </IconButton>
          </ChromeHint>
          <ChromeHint label="Mail">
            <span className="ui-shell-pack">
              <IconButton label="Mail">
                <UiIcon>
                  <ChromeIcon name="mail" />
                </UiIcon>
              </IconButton>
              <CounterBadge count={3} />
            </span>
          </ChromeHint>
          <ChromeHint label="Roster">
            <IconButton label="Roster">
              <UiIcon>
                <ChromeIcon name="friends" />
              </UiIcon>
            </IconButton>
          </ChromeHint>
          <ChromeHint label="Settings">
            <IconButton label="Settings">
              <UiIcon>
                <ChromeIcon name="settings" />
              </UiIcon>
            </IconButton>
          </ChromeHint>
        </div>
      </header>

      <div className="ui-shell-body">
        <nav className="ui-shell-nav surface-base" aria-label="Navigation sample">
          <p className="type-section-heading">Navigation</p>
          <ul className="ui-shell-nav-list">
            {NAV.map((entry) => (
              <li key={entry.id}>
                <a
                  href="#composition"
                  className={classNames('ui-shell-nav-link', entry.active && 'ui-nav-selected')}
                  aria-current={entry.active ? 'page' : undefined}
                >
                  <UiIcon size="sm" state={entry.active ? 'active' : 'default'}>
                    <ChromeIcon name={entry.icon} />
                  </UiIcon>
                  <span className="type-compact">{entry.label}</span>
                </a>
              </li>
            ))}
          </ul>
          <p className="type-section-heading">Quick actions</p>
          <ul className="ui-shell-nav-list">
            {QUICK.map((entry) => (
              <li key={entry.id}>
                <button type="button" className="ui-shell-nav-link">
                  <UiIcon size="sm">
                    <ChromeIcon name={entry.icon} />
                  </UiIcon>
                  <span>
                    <span className="type-compact">{entry.label}</span>
                    <span className="type-meta">{entry.meta}</span>
                  </span>
                </button>
              </li>
            ))}
          </ul>
          <div className="ui-shell-nav-foot">
            <Button
              type="button"
              variant="ghost"
              aria-pressed={officeOn}
              onClick={() => {
                const next: UiMode = uiMode === 'compact' ? 'normal' : 'compact'
                setUiMode(next)
                persistUiMode(next)
                applyUiMode(next)
              }}
            >
              {officeOn ? 'Office mode on' : 'Office mode'}
            </Button>
            <p className="type-meta">
              <span className="ui-shell-online" aria-hidden="true" />
              42 Online
            </p>
          </div>
        </nav>

        <div className="ui-shell-workspace">
          <Panel className="ui-shell-main surface-frame" data-testid="ui-shell-main">
            <div className="ui-shell-banner">
              <div className="ui-shell-banner-copy">
                <p className="type-micro">Night ledger · North clerk</p>
                <h2 className="type-page-heading">Records</h2>
                <p className="type-compact">
                  Open notices stay on the desk until sealed. Archive writes lock at dawn.
                </p>
              </div>
              <Ornament name="diamond" />
            </div>

            <div className="ui-shell-toolbar">
              <Tabs<RecordTab>
                label="Record views"
                value={tab}
                onChange={(next) => {
                  setTab(next)
                  const first = LEDGER.find((entry) => entry.tab === next)
                  if (first) {
                    setSelectedId(first.id)
                  }
                }}
                tabs={[
                  { id: 'open', label: 'Open' },
                  { id: 'sealed', label: 'Sealed' },
                  { id: 'archive', label: 'Archive' },
                ]}
              />
              <SearchInput
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Search records"
                aria-label="Search records"
              />
              <Select value={kind} onChange={(event) => setKind(event.target.value)} aria-label="Kind">
                <option value="all">All kinds</option>
                <option value="watch">Watch</option>
                <option value="report">Report</option>
                <option value="notice">Notice</option>
              </Select>
            </div>

            <div className="ui-shell-work">
              <div className="ui-shell-ledger surface-inset" data-testid="ui-shell-ledger">
                {rows.map((entry) => (
                  <CompactDataRow
                    key={entry.id}
                    as="div"
                    selected={entry.id === selected.id}
                    onClick={() => setSelectedId(entry.id)}
                    icon={
                      <UiIcon>
                        <ChromeIcon name="quests" />
                      </UiIcon>
                    }
                    primary={entry.name}
                    secondary={`${entry.kind} · ${entry.status}`}
                    metadata={<span className="type-numeric">{entry.weight}</span>}
                  />
                ))}
              </div>

              <div className="ui-shell-inspector surface-raised" data-testid="ui-shell-inspector">
                <p className="type-section-heading">Selected record</p>
                <Tooltip
                  open={recordTip}
                  placement="left"
                  density="inspector"
                  content={
                    <div className="tooltip-ledger">
                      <p className="type-item tooltip-ledger-name">{selected.name}</p>
                      <p className="type-meta">{selected.kind} · synthetic</p>
                      <dl className="tooltip-ledger-stat">
                        <dt>Weight</dt>
                        <dd className="type-numeric">{selected.weight}</dd>
                      </dl>
                    </div>
                  }
                >
                  <p
                    className="type-item"
                    onMouseEnter={() => setRecordTip(true)}
                    onMouseLeave={() => setRecordTip(false)}
                  >
                    {selected.name}
                  </p>
                </Tooltip>
                <p className="type-compact">
                  {selected.kind} · {selected.status}
                </p>
                <dl className="ui-shell-stats">
                  <div>
                    <dt className="type-micro">Weight</dt>
                    <dd className="type-numeric">{selected.weight}</dd>
                  </div>
                  <div>
                    <dt className="type-micro">Filed</dt>
                    <dd className="type-meta">{selected.when} ago</dd>
                  </div>
                  <div>
                    <dt className="type-micro">Desk</dt>
                    <dd className="type-compact">North clerk</dd>
                  </div>
                </dl>
                <div className="ui-shell-actions">
                  <Button variant="primary">Seal</Button>
                  <Button variant="secondary">Copy</Button>
                  <Button variant="ghost">Dismiss</Button>
                </div>
              </div>
            </div>

            <div className="ui-shell-strip surface-inset" data-testid="ui-shell-strip">
              <div>
                <p className="type-micro">Open</p>
                <p className="type-numeric type-numeric-gold">12</p>
              </div>
              <div>
                <p className="type-micro">Sealed</p>
                <p className="type-numeric">4</p>
              </div>
              <div>
                <p className="type-micro">Archive</p>
                <p className="type-numeric">86</p>
              </div>
              <div>
                <p className="type-micro">Duty</p>
                <p className="type-compact type-positive">Safe desk</p>
              </div>
            </div>
          </Panel>

          <Panel className="ui-shell-chat" data-testid="ui-shell-chat">
            <div className="ui-shell-chat-tabs">
              <Tabs<ChatTab>
                label="Chat sample"
                value={chatTab}
                onChange={setChatTab}
                tabs={[
                  { id: 'global', label: 'Global' },
                  { id: 'local', label: 'Local' },
                  { id: 'watch', label: 'Watch' },
                ]}
              />
              <CounterBadge count={2} />
            </div>
            <div className="ui-shell-chat-log surface-inset">
              {CHAT[chatTab].map((line) => (
                <p key={`${line.time}-${line.name}`} className="ui-shell-chat-line">
                  <time className="type-meta">{line.time}</time>
                  <span className={classNames('type-compact', `ui-shell-name-${line.tone}`)}>{line.name}</span>
                  <span className="type-compact">{line.text}</span>
                </p>
              ))}
            </div>
            <form
              className="ui-shell-chat-form"
              onSubmit={(event) => {
                event.preventDefault()
                setDraft('')
              }}
            >
              <TextInput
                value={draft}
                onChange={(event) => setDraft(event.target.value)}
                placeholder="Type a message"
                aria-label="Chat message"
              />
              <Button variant="primary" type="submit">
                Send
              </Button>
            </form>
          </Panel>
        </div>

        <aside className="ui-shell-rail surface-base" data-testid="ui-shell-rail">
          <Section title="Recent events" divider={false}>
            <ul className="ui-row-list">
              <ActivityRow
                variant="normal"
                icon={
                  <UiIcon art>
                    <img src={activityArt('scroll.webp')} alt="" />
                  </UiIcon>
                }
                primary="Harbour lamps marked."
                secondary="Watch"
                metadata="2m"
              />
              <ActivityRow
                variant="system"
                icon={
                  <UiIcon art>
                    <img src={activityArt('scroll.webp')} alt="" />
                  </UiIcon>
                }
                primary="Roster sealed for dusk."
                secondary="System"
                metadata="11m"
              />
              <ActivityRow
                variant="completed"
                icon={
                  <UiIcon art>
                    <img src={activityArt('chest.webp')} alt="" />
                  </UiIcon>
                }
                primary="Old stipend filed."
                secondary="Archive"
                metadata="3d"
              />
            </ul>
          </Section>
          <Section title="Claimable" divider={false}>
            <ul className="ui-row-list">
              <NotificationRow
                variant="reward"
                unread
                icon={
                  <UiIcon art>
                    <img src={activityArt('chest.webp')} alt="" />
                  </UiIcon>
                }
                primary="Clerk stipend"
                secondary="Night desk"
                action={
                  <Button variant="primary" type="button">
                    Claim
                  </Button>
                }
              />
            </ul>
          </Section>
          <Section title="Alerts" divider={false} className="ui-shell-alerts">
            <ul className="ui-row-list">
              <NotificationRow
                variant="warning"
                unread
                icon={
                  <UiIcon art>
                    <img src={activityArt('alert.webp')} alt="" />
                  </UiIcon>
                }
                primary="Frost on the quay"
                secondary="Open notice"
                metadata="28m"
              />
            </ul>
          </Section>
        </aside>
      </div>
    </section>
  )
}
