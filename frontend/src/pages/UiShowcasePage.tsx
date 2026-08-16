import { useState } from 'react'
import { GenericGameComposition } from '../dev/GenericGameComposition'
import { ActivityRow, type ActivityRowVariant } from '../ui/ActivityRow'
import { CompactDataRow } from '../ui/CompactDataRow'
import { Badge } from '../ui/Badge'
import { Button, type ButtonVariant } from '../ui/Button'
import { ChromeIcon } from '../ui/chromeIcons'
import { CounterBadge } from '../ui/CounterBadge'
import { Divider } from '../ui/Divider'
import { DurabilityBar } from '../ui/DurabilityBar'
import { Field } from '../ui/Field'
import { GenericRow } from '../ui/GenericRow'
import { NotificationRow } from '../ui/NotificationRow'
import { HealthBar } from '../ui/HealthBar'
import { IconButton } from '../ui/IconButton'
import { IconWell } from '../ui/IconWell'
import { Ornament } from '../ui/Ornament'
import { Panel } from '../ui/Panel'
import { ProgressBar } from '../ui/ProgressBar'
import { RarityBadge } from '../ui/RarityBadge'
import { SearchInput } from '../ui/SearchInput'
import { Section } from '../ui/Section'
import { Select } from '../ui/Select'
import { StaminaBar } from '../ui/StaminaBar'
import { StatusBadge } from '../ui/StatusBadge'
import { Tabs } from '../ui/Tabs'
import { Textarea } from '../ui/Textarea'
import { TextInput } from '../ui/TextInput'
import { Tooltip } from '../ui/Tooltip'
import { UiIcon } from '../ui/UiIcon'
import { applyUiMode, persistUiMode, readStoredUiMode, type UiMode } from '../ui/uiMode'
import { XPBar } from '../ui/XPBar'

const COLOR_TOKENS = [
  { name: '--color-page-bg', alpha: false },
  { name: '--color-surface-base', alpha: false },
  { name: '--color-surface-raised', alpha: false },
  { name: '--color-surface-inset', alpha: false },
  { name: '--color-surface-interactive', alpha: true },
  { name: '--color-surface-selected', alpha: true },
  { name: '--color-surface-floating', alpha: false },
  { name: '--color-bronze-dim', alpha: false },
  { name: '--color-bronze-normal', alpha: false },
  { name: '--color-bronze-strong', alpha: false },
  { name: '--color-gold-dim', alpha: false },
  { name: '--color-gold-normal', alpha: false },
  { name: '--color-gold-strong', alpha: false },
  { name: '--color-gold-highlight', alpha: false },
  { name: '--color-text-primary', alpha: false },
  { name: '--color-text-secondary', alpha: false },
  { name: '--color-text-muted', alpha: false },
  { name: '--color-text-disabled', alpha: false },
  { name: '--color-text-bright', alpha: false },
  { name: '--color-positive', alpha: false },
  { name: '--color-negative', alpha: false },
  { name: '--color-warning', alpha: false },
  { name: '--color-info', alpha: false },
  { name: '--color-border-subtle', alpha: true },
  { name: '--color-border-default', alpha: true },
  { name: '--color-border-interactive', alpha: true },
  { name: '--color-border-selected', alpha: true },
] as const

const SURFACES = ['page', 'base', 'raised', 'inset', 'interactive', 'selected', 'floating'] as const

const TYPE_ROLES = [
  { role: 'Display', className: 'type-display', sample: 'Greyhaven' },
  { role: 'Page Heading', className: 'type-page-heading', sample: 'Marketplace' },
  { role: 'Panel Heading', className: 'type-panel-heading', sample: 'Character Overview' },
  { role: 'Section Heading', className: 'type-section-heading', sample: 'Navigation' },
  { role: 'Item Name', className: 'type-item', sample: 'Iron Plate Boots' },
  { role: 'Body UI', className: 'type-body', sample: 'The road north is quiet tonight.' },
  { role: 'Compact UI', className: 'type-compact', sample: 'Claim rewards' },
  { role: 'Metadata', className: 'type-meta', sample: 'Updated 4m ago' },
  { role: 'Numeric UI', className: 'type-numeric', sample: '1 250 764' },
] as const

const TYPE_TONES = [
  { role: 'Positive', className: 'type-compact type-positive', sample: '+12 Strength · Safe Zone' },
  { role: 'Negative', className: 'type-compact type-negative', sample: '−8 Armor · Rift Invasion' },
  { role: 'Warning', className: 'type-compact type-warning', sample: 'Durability 22 / 100' },
] as const

const TYPE_ACCEPTANCE = [
  'Page titles are inscriptional Cinzel, large tracked caps.',
  'Panel titles are a separate small Cinzel nameplate.',
  'Section labels are small bronze caps, quieter than body.',
  'Body is silver-parchment Source Sans 3, not cream and not white.',
  'Numbers out-read adjacent labels (brightness, tabular).',
  'Display serif is not used on buttons, tabs, fields, or rows.',
  'Gold is aged brass on emphasis only — not lemon jewelry.',
] as const

const BUTTON_VARIANTS: ButtonVariant[] = ['primary', 'secondary', 'ghost', 'danger']

const ACTIVITY_VARIANTS: { variant: ActivityRowVariant; primary: string; secondary: string }[] = [
  { variant: 'normal', primary: 'You arrived in Old Town.', secondary: 'Travel' },
  { variant: 'system', primary: 'Quest accepted: Night Watch.', secondary: 'System' },
  { variant: 'reward', primary: 'LEVEL UP — you are now 4.', secondary: 'Progress' },
  { variant: 'warning', primary: 'Durability is low on Iron helm.', secondary: 'Alert' },
  { variant: 'market', primary: 'You sold Wolf pelt for 12 gold.', secondary: 'Market' },
  { variant: 'pvp', primary: 'You defeated a Street thug.', secondary: 'Combat' },
  { variant: 'completed', primary: 'You claimed your Forest rewards.', secondary: 'Done' },
]

const SCROLL_LOG = [
  'ROUND 5',
  'You strike the Warden for 486.',
  'The Warden blocks 120.',
  'Bleed ticks for 64.',
  'Battle Shout remaining: 2 turns.',
  'You recover 18 stamina.',
  'ROUND 6',
  'Heavy Strike — 100% hit chance.',
  'Second Wind is unavailable.',
  'The Warden prepares a crushing blow.',
] as const

const CHAPTERS = [
  { id: 'composition', label: 'Composition' },
  { id: 'typography', label: 'Typography' },
  { id: 'colors', label: 'Colors' },
  { id: 'surfaces', label: 'Surfaces' },
  { id: 'buttons', label: 'Buttons' },
  { id: 'tabs', label: 'Tabs' },
  { id: 'inputs', label: 'Inputs' },
  { id: 'progress', label: 'Progress' },
  { id: 'badges', label: 'Badges' },
  { id: 'rows', label: 'Rows' },
  { id: 'tooltips', label: 'Tooltips' },
  { id: 'states', label: 'States' },
] as const

export function UiShowcasePage() {
  const [uiMode, setUiMode] = useState<UiMode>(() => readStoredUiMode())
  const [listingTab, setListingTab] = useState<'listings' | 'orders' | 'history'>('listings')
  const [query, setQuery] = useState('iron')
  const [district, setDistrict] = useState('harbour')
  const [tooltipOpen, setTooltipOpen] = useState(false)

  function toggleUiMode() {
    const next: UiMode = uiMode === 'compact' ? 'normal' : 'compact'
    setUiMode(next)
    persistUiMode(next)
    applyUiMode(next)
  }

  return (
    <div className="ui-showcase" data-testid="ui-showcase">
      <div id="composition">
        <GenericGameComposition />
      </div>

      <div className="ui-showcase-reference" id="reference">
        <header className="ui-showcase-top">
          <div className="ui-showcase-top-row">
            <div>
              <p className="type-display">Greyhaven</p>
              <p className="type-micro">Dev only · /dev/ui · Part A</p>
              <h1 className="type-page-heading">Component Reference</h1>
              <p className="type-body ui-showcase-lede">
                Primitive catalogue. Visual QA of the engine as a game is the composition above, compared
                against docs/mockups.
              </p>
            </div>
            <Button
              type="button"
              variant="secondary"
              data-testid="ui-showcase-mode"
              aria-pressed={uiMode === 'compact'}
              onClick={toggleUiMode}
            >
              {uiMode === 'compact' ? 'Office mode on' : 'Office mode off'}
            </Button>
          </div>
          <nav className="ui-showcase-nav" aria-label="Showcase sections">
            {CHAPTERS.map((chapter) => (
              <a key={chapter.id} href={`#${chapter.id}`}>
                {chapter.label}
              </a>
            ))}
          </nav>
        </header>

        <div className="ui-showcase-body">
          <Panel id="typography" className="ui-showcase-chapter" title="Typography" data-testid="showcase-typography">
            <Section title="Roles" divider>
              <div className="ui-showcase-type">
                {TYPE_ROLES.map((entry) => (
                  <div key={entry.className} className="ui-showcase-type-row">
                    <p className="type-micro">{entry.role}</p>
                    <p className={`${entry.className} ui-showcase-type-sample`}>{entry.sample}</p>
                  </div>
                ))}
              </div>
            </Section>
            <Section title="Positive / Negative / Warning" divider>
              <div className="ui-showcase-type">
                {TYPE_TONES.map((entry) => (
                  <div key={entry.role} className="ui-showcase-type-row">
                    <p className="type-micro">{entry.role}</p>
                    <p className={`${entry.className} ui-showcase-type-sample`}>{entry.sample}</p>
                  </div>
                ))}
                <div className="ui-showcase-type-row">
                  <p className="type-micro">Numeric gold</p>
                  <p className="type-numeric type-numeric-gold ui-showcase-type-sample">248 · 1 250 764</p>
                </div>
              </div>
            </Section>
            <Section title="Acceptance" divider>
              <ul className="ui-showcase-accept" data-testid="showcase-type-acceptance">
                {TYPE_ACCEPTANCE.map((line) => (
                  <li key={line} className="type-compact">
                    {line}
                  </li>
                ))}
              </ul>
            </Section>
          </Panel>

          <Panel id="colors" className="ui-showcase-chapter" title="Colors" data-testid="showcase-colors">
            <Section title="Semantic tokens" divider>
              <div className="ui-showcase-grid">
                {COLOR_TOKENS.map((token) => (
                  <figure key={token.name} className="ui-showcase-swatch">
                    <div
                      className={
                        token.alpha ? 'ui-showcase-swatch-chip ui-showcase-swatch-chip-check' : 'ui-showcase-swatch-chip'
                      }
                      style={{ ['--swatch' as string]: `var(${token.name})` }}
                    />
                    <figcaption className="type-meta ui-showcase-swatch-name">{token.name}</figcaption>
                  </figure>
                ))}
              </div>
            </Section>
          </Panel>

          <Panel id="surfaces" className="ui-showcase-chapter" title="Surfaces" data-testid="showcase-surfaces">
            <Section title="Nested workspace" divider="ornament-bronze">
              <div className="ui-showcase-stage surface-page" data-testid="surface-page">
                <Panel className="ui-showcase-compose-base surface-frame" data-testid="surface-base" title="Inspector">
                  <div className="surface-raised ui-showcase-compose-raised" data-testid="surface-raised">
                    <p className="type-section-heading">Raised plate</p>
                    <p className="type-compact">Ragnar Ironfist · Level 7</p>
                    <p className="type-meta">Warm-black metal above the workspace frame</p>
                  </div>
                  <div className="ui-showcase-compose-body">
                    <div className="surface-inset ui-showcase-compose-inset" data-testid="surface-inset">
                      <p className="type-section-heading">Battle log</p>
                      <p className="type-compact">You strike the street thug for 14.</p>
                      <p className="type-compact type-negative">The thug cuts you for 6.</p>
                      <p className="type-meta">Inset well · darker than the panel</p>
                    </div>
                    <div className="ui-showcase-compose-side">
                      <div className="surface-selected ui-showcase-compose-row" data-testid="surface-selected">
                        <p className="type-compact">Home</p>
                        <p className="type-meta">Selected · brighter bronze rim</p>
                      </div>
                      <div className="surface-interactive ui-showcase-compose-row" data-testid="surface-interactive">
                        <p className="type-compact">Locations</p>
                        <p className="type-meta">Interactive · quieter metal</p>
                      </div>
                      <div className="surface-floating ui-showcase-compose-float" data-testid="surface-floating">
                        <p className="type-section-heading">Selected item</p>
                        <p className="type-item">Verdant Signet</p>
                        <p className="type-numeric type-numeric-gold">+4 Will</p>
                        <p className="type-meta">Floating plate · tight umbra</p>
                      </div>
                    </div>
                  </div>
                </Panel>
              </div>
            </Section>
            <p className="type-meta ui-showcase-caption">
              Global background · Base panel · Raised · Inset · Selected · Floating
            </p>
            <ul className="ui-showcase-surface-index">
              {SURFACES.map((surface) => (
                <li key={surface} className="type-meta">
                  .surface-{surface}
                </li>
              ))}
            </ul>
            <Section title="Section inside a panel" divider>
              <p className="type-body">A Section is not another Panel. Heading, line, then content.</p>
            </Section>
            <Section title="Dividers" divider={false}>
              <div className="ui-showcase-stack">
                <p className="type-meta">Line</p>
                <Divider />
                <p className="type-meta">Chapter break — rare</p>
                <Divider variant="ornament-bronze" />
              </div>
            </Section>
            <Section title="Icon scale" accent>
              <div className="ui-showcase-row" data-testid="showcase-icon-scale">
                {(['sm', 'md', 'lg', 'xl'] as const).map((size) => (
                  <span key={size} className="ui-showcase-icon-sample">
                    <UiIcon size={size}>
                      <ChromeIcon name="mail" className="" />
                    </UiIcon>
                    <span className="type-meta">{size}</span>
                  </span>
                ))}
                <UiIcon size="md" state="disabled">
                  <ChromeIcon name="settings" className="" />
                </UiIcon>
                <UiIcon size="md" state="active">
                  <ChromeIcon name="home" className="" />
                </UiIcon>
              </div>
            </Section>
            <Section title="Icon wells" accent>
              <div className="ui-showcase-row" data-testid="showcase-icon-wells">
                <IconWell>
                  <UiIcon>
                    <ChromeIcon name="mail" className="" />
                  </UiIcon>
                </IconWell>
                <IconWell active>
                  <UiIcon state="active">
                    <ChromeIcon name="pack" className="" />
                  </UiIcon>
                </IconWell>
                <IconWell size="lg">
                  <UiIcon size="lg" art>
                    <img src="/icons/actions/travel.webp" alt="" />
                  </UiIcon>
                </IconWell>
                <IconButton label="Trophy">
                  <UiIcon>
                    <ChromeIcon name="trophy" className="" />
                  </UiIcon>
                </IconButton>
              </div>
            </Section>
            <Section title="Selected markers" divider={false}>
              <div className="ui-showcase-marks-grid" data-testid="showcase-selected-marks">
                <div className="ui-showcase-compose-row surface-selected">
                  <p className="type-compact">Home</p>
                  <p className="type-meta">Nav · left metal</p>
                </div>
                <div className="ui-showcase-compose-row ui-mark-selected">
                  <p className="type-compact">Listing row</p>
                  <p className="type-meta">Ledger · selected tick</p>
                </div>
                <div className="ui-showcase-slot-well surface-inset ui-mark-frame">
                  <UiIcon art>
                    <img src="/items/copper_ring.webp" alt="" />
                  </UiIcon>
                </div>
              </div>
            </Section>
            <p className="type-meta ui-showcase-caption">
              Frame corners on the composition workspace. Heading pips are chapter hardware, not content.
            </p>
            <div className="ui-showcase-ornament-row" aria-hidden="true">
              <Ornament name="corner" />
              <Ornament name="divider" />
              <Ornament name="corner" corner="tr" />
            </div>
          </Panel>

          <Panel id="buttons" className="ui-showcase-chapter" title="Buttons" data-testid="showcase-controls">
            <Section title="Button variants" divider={false}>
              <div className="ui-showcase-row">
                {BUTTON_VARIANTS.map((variant) => (
                  <Button key={variant} variant={variant}>
                    {variant}
                  </Button>
                ))}
                <IconButton label="Mail" variant="primary">
                  <UiIcon>
                    <ChromeIcon name="mail" className="" />
                  </UiIcon>
                </IconButton>
                <IconButton label="Disabled" disabled>
                  <UiIcon state="disabled">
                    <ChromeIcon name="menu" className="" />
                  </UiIcon>
                </IconButton>
              </div>
            </Section>
          </Panel>

          <Panel id="tabs" className="ui-showcase-chapter" title="Tabs" data-testid="showcase-examples">
            <Section title="Text on the frame">
              <div className="ui-showcase-example" data-testid="showcase-example-panel">
                <Tabs<'listings' | 'orders' | 'history'>
                  label="Listing views"
                  value={listingTab}
                  onChange={setListingTab}
                  tabs={[
                    { id: 'listings', label: 'Listings' },
                    { id: 'orders', label: 'Orders' },
                    { id: 'history', label: 'History' },
                  ]}
                />
                <div className="surface-inset ui-showcase-example-well">
                  <p className="type-section-heading">Selected listing</p>
                  <p className="type-item">Militia shortsword</p>
                  <p className="type-compact">Common · Main hand · Harbour stall</p>
                  <p className="type-numeric type-numeric-gold">24</p>
                  <p className="type-meta">
                    {listingTab === 'listings' ? 'Open market' : listingTab === 'orders' ? 'Your bids' : 'Settled trades'}
                  </p>
                </div>
                <div className="ui-showcase-example-actions">
                  <Button variant="primary">Buy</Button>
                  <Button variant="secondary">Compare</Button>
                </div>
              </div>
            </Section>
          </Panel>

          <Panel id="inputs" className="ui-showcase-chapter" title="Inputs" data-testid="showcase-forms">
            <Section title="Input, search, select, textarea" divider>
              <div className="ui-showcase-row">
                <Field label="Name" className="ui-showcase-field">
                  <TextInput defaultValue="Edric Varn" />
                </Field>
                <Field label="Search" className="ui-showcase-field">
                  <SearchInput value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Find an item" />
                </Field>
                <Field label="District" className="ui-showcase-field">
                  <Select value={district} onChange={(event) => setDistrict(event.target.value)}>
                    <option value="harbour">Harbour</option>
                    <option value="old-town">Old Town</option>
                    <option value="market">Market</option>
                  </Select>
                </Field>
              </div>
              <Field label="Duty notes" className="ui-showcase-field ui-showcase-field-wide">
                <Textarea defaultValue="Harbour lamps are lit. The north road is quiet." rows={3} />
              </Field>
            </Section>
            <Section title="Scrollbar" divider>
              <div className="ui-showcase-scroll-well surface-inset" data-testid="showcase-scrollbar">
                {SCROLL_LOG.map((line) => (
                  <p key={line} className="type-compact">
                    {line}
                  </p>
                ))}
              </div>
            </Section>
          </Panel>

          <Panel id="progress" className="ui-showcase-chapter" title="Progress" data-testid="showcase-meters">
            <Section title="Progress bars" divider>
              <div className="ui-showcase-meters">
                <ProgressBar label="Experience" value={64} showValue />
                <HealthBar label="Health" value={72} showValue />
                <StaminaBar label="Stamina" value={40} showValue />
                <XPBar label="XP compact" value={18} density="compact" showValue />
                <DurabilityBar label="Durability" value={22} />
              </div>
            </Section>
            <Section title="Realistic samples" divider>
              <div className="ui-showcase-samples" data-testid="showcase-meter-samples">
                <div className="ui-showcase-sample">
                  <p className="type-section-heading">Player vitals</p>
                  <HealthBar
                    label="Health 3850 of 3850"
                    value={3850}
                    max={3850}
                    showValue
                    valuePlacement="overlay"
                    valueText="3,850 / 3,850"
                    segments={10}
                  />
                  <StaminaBar
                    label="Stamina 84 of 120"
                    value={84}
                    max={120}
                    showValue
                    valuePlacement="overlay"
                    valueText="84 / 120"
                    segments={10}
                  />
                </div>
                <div className="ui-showcase-sample">
                  <p className="type-section-heading">XP</p>
                  <div className="ui-showcase-xp-block">
                    <span className="type-item">Ragnar Ironfist</span>
                    <XPBar
                      label="Experience 62 percent"
                      value={62}
                      showValue
                      valuePlacement="beside"
                      valueText="62%"
                    />
                  </div>
                </div>
                <div className="ui-showcase-sample">
                  <p className="type-section-heading">Item durability</p>
                  <div className="ui-showcase-xp-block">
                    <span className="type-meta">Durability</span>
                    <DurabilityBar
                      label="Durability 85 of 100"
                      value={85}
                      max={100}
                      showValue
                      valuePlacement="beside"
                      valueText="85 / 100"
                    />
                  </div>
                </div>
                <div className="ui-showcase-sample">
                  <p className="type-section-heading">Status effects</p>
                  <div className="ui-showcase-status-list">
                    <StatusBadge tone="upgrade" icon={<span />} meta="2 turns">
                      Battle Shout
                    </StatusBadge>
                    <StatusBadge tone="danger" icon={<span />} meta="3 turns">
                      Bleed
                    </StatusBadge>
                    <StatusBadge tone="safe" icon={<span />} meta="30m remaining">
                      Fortified
                    </StatusBadge>
                  </div>
                </div>
              </div>
            </Section>
          </Panel>

          <Panel id="badges" className="ui-showcase-chapter" title="Badges">
            <Section title="Counters and status" divider>
              <div className="ui-showcase-row">
                <Badge>Neutral</Badge>
                <Badge tone="accent">Accent</Badge>
                <Badge tone="warning">Warning</Badge>
                <Badge tone="danger">Danger</Badge>
                <StatusBadge tone="safe">Safe</StatusBadge>
                <StatusBadge tone="upgrade">Upgrade</StatusBadge>
                <StatusBadge tone="mixed">Mixed</StatusBadge>
                <span className="ui-showcase-counter-host">
                  Mail
                  <CounterBadge count={3} />
                </span>
                <span className="ui-showcase-counter-host">
                  Global
                  <CounterBadge count={12} />
                </span>
                <CounterBadge count={128} tone="accent" />
                <RarityBadge rarity="COMMON" />
                <RarityBadge rarity="UNCOMMON" />
                <RarityBadge rarity="RARE" />
                <RarityBadge rarity="EPIC" />
              </div>
            </Section>
          </Panel>

          <Panel id="rows" className="ui-showcase-chapter" title="Rows" data-testid="showcase-rows">
            <Section title="Generic rows" divider>
              <div className="ui-row-list">
                <GenericRow
                  icon={
                    <UiIcon>
                      <ChromeIcon name="trophy" className="" />
                    </UiIcon>
                  }
                  primary="Militia shortsword"
                  secondary="Main hand · Common"
                  metadata="1.2 kg"
                  action={
                    <Button variant="ghost" type="button">
                      Equip
                    </Button>
                  }
                />
                <GenericRow primary="Empty slot" secondary="No secondary action" metadata="—" />
              </div>
            </Section>
            <Section title="Activity feed" divider>
              <ul className="ui-row-list" data-testid="showcase-activity-feed">
                {ACTIVITY_VARIANTS.map((entry) => (
                  <ActivityRow
                    key={entry.variant}
                    variant={entry.variant}
                    icon={
                      <UiIcon art>
                        <img src={`/icons/activity/${activityArt(entry.variant)}`} alt="" />
                      </UiIcon>
                    }
                    primary={entry.primary}
                    secondary={entry.secondary}
                    metadata="now"
                  />
                ))}
              </ul>
            </Section>
            <Section title="Notification stack" divider>
              <ul className="ui-row-list" data-testid="showcase-notification-stack">
                <NotificationRow
                  variant="reward"
                  unread
                  icon={
                    <UiIcon art>
                      <img src="/icons/activity/chest.webp" alt="" />
                    </UiIcon>
                  }
                  primary="Daily reward available"
                  secondary="Office stipend"
                  action={
                    <Button variant="primary" type="button">
                      Claim
                    </Button>
                  }
                />
                <NotificationRow
                  variant="system"
                  unread
                  icon={
                    <UiIcon art>
                      <img src="/icons/activity/scroll.webp" alt="" />
                    </UiIcon>
                  }
                  primary="Daily Quest Ready"
                  secondary="3 tasks available"
                  metadata="12m"
                />
                <NotificationRow
                  variant="market"
                  icon={
                    <UiIcon art>
                      <img src="/icons/activity/gold.webp" alt="" />
                    </UiIcon>
                  }
                  primary="Market Alert"
                  secondary="12 new listings added"
                  metadata="1h"
                />
                <NotificationRow
                  variant="warning"
                  unread
                  icon={
                    <UiIcon art>
                      <img src="/icons/activity/alert.webp" alt="" />
                    </UiIcon>
                  }
                  primary="Rift Invasion"
                  secondary="30m remaining"
                />
              </ul>
            </Section>
            <Section title="Dense marketplace-like list" divider>
              <div className="ui-row-list" data-testid="showcase-market-list">
                <CompactDataRow
                  as="div"
                  selected
                  icon={
                    <UiIcon art>
                      <img src="/items/militia_shortsword.webp" alt="" />
                    </UiIcon>
                  }
                  primary={<span className="rarity rarity-rare">Vicious Mercenary Longsword</span>}
                  secondary="Rare · Weapon · 47"
                  metadata={<span className="type-numeric type-numeric-gold">1,840</span>}
                  action={
                    <Button variant="primary" type="button">
                      Buy
                    </Button>
                  }
                />
                <CompactDataRow
                  as="div"
                  icon={
                    <UiIcon art>
                      <img src="/items/wolf_pelt.webp" alt="" />
                    </UiIcon>
                  }
                  primary={<span className="rarity rarity-common">Wolf pelt</span>}
                  secondary="Common · Material · 1"
                  metadata={<span className="type-numeric type-numeric-gold">6</span>}
                  action={
                    <Button variant="secondary" type="button">
                      Buy
                    </Button>
                  }
                />
                <CompactDataRow
                  as="div"
                  icon={
                    <UiIcon art>
                      <img src="/items/copper_ring.webp" alt="" />
                    </UiIcon>
                  }
                  primary={<span className="rarity rarity-epic">Verdant Signet</span>}
                  secondary="Epic · Ring · 24"
                  metadata={<span className="type-numeric type-numeric-gold">240</span>}
                  action={
                    <Button variant="secondary" type="button">
                      Buy
                    </Button>
                  }
                />
              </div>
            </Section>
            <Section title="Compact log" divider>
              <ul className="ui-row-list surface-inset ui-showcase-scroll-well" data-testid="showcase-compact-log">
                <CompactDataRow
                  tone="secondary"
                  interactive={false}
                  icon={
                    <UiIcon art>
                      <img src="/icons/activity/scroll.webp" alt="" />
                    </UiIcon>
                  }
                  primary="ROUND 5"
                  metadata="1m ago"
                />
                <CompactDataRow
                  tone="important"
                  interactive={false}
                  icon={
                    <UiIcon art>
                      <img src="/icons/activity/swords.webp" alt="" />
                    </UiIcon>
                  }
                  primary={
                    <>
                      You strike the Warden for <span className="type-negative">486</span>
                    </>
                  }
                  metadata="Just now"
                />
                <CompactDataRow
                  interactive={false}
                  icon={
                    <UiIcon art>
                      <img src="/icons/activity/alert.webp" alt="" />
                    </UiIcon>
                  }
                  primary={
                    <>
                      Bleed ticks for <span className="type-negative">64</span>
                    </>
                  }
                  secondary="DoT"
                  metadata="20s ago"
                />
                <CompactDataRow
                  tone="secondary"
                  interactive={false}
                  icon={
                    <UiIcon art>
                      <img src="/icons/activity/chest.webp" alt="" />
                    </UiIcon>
                  }
                  primary="Battle Shout remaining: 2 turns"
                  metadata="20s ago"
                />
              </ul>
            </Section>
          </Panel>

          <Panel id="tooltips" className="ui-showcase-chapter" title="Tooltips">
            <Section title="Hover and inspector" divider>
              <div className="ui-showcase-row">
                <Tooltip
                  open={tooltipOpen}
                  placement="bottom"
                  density="compact"
                  content={<p className="type-compact">Hover copy for visual QA.</p>}
                >
                  <Button
                    variant="secondary"
                    onMouseEnter={() => setTooltipOpen(true)}
                    onMouseLeave={() => setTooltipOpen(false)}
                  >
                    Hover for tooltip
                  </Button>
                </Tooltip>
                <div className="tooltip-panel tooltip-panel-compact surface-floating" role="tooltip">
                  <p className="type-compact">Pinned tooltip stays open.</p>
                </div>
                <div className="tooltip-panel tooltip-panel-inspector surface-floating" role="tooltip">
                  <div className="tooltip-ledger">
                    <div className="tooltip-ledger-head">
                      <span className="tooltip-ledger-icon" aria-hidden="true" />
                      <div>
                        <p className="type-item tooltip-ledger-name" style={{ color: 'var(--rarity-epic)' }}>
                          Verdant Signet
                        </p>
                        <p className="type-meta">Epic Ring · Item Level 62</p>
                      </div>
                    </div>
                    <dl className="tooltip-ledger-stat">
                      <dt>Strength</dt>
                      <dd className="type-numeric">+12</dd>
                    </dl>
                    <dl className="tooltip-ledger-stat">
                      <dt>Vitality</dt>
                      <dd className="type-numeric">+8</dd>
                    </dl>
                    <DurabilityBar
                      label="Durability 85 of 100"
                      value={85}
                      max={100}
                      showValue
                      valuePlacement="beside"
                      valueText="85 / 100"
                    />
                  </div>
                </div>
              </div>
            </Section>
          </Panel>

          <Panel id="states" className="ui-showcase-chapter" title="States" data-testid="showcase-states">
            <Section title="Forced samples" divider>
              <p className="type-meta ui-showcase-caption">
                Hover and focus are frozen with the same tokens as the live engine. Focus is a tight aged-metal
                ring, not a yellow glow. Interact with the live controls above to confirm motion.
              </p>
              <div className="ui-showcase-state-grid">
                <div className="ui-showcase-state-card">
                  <p className="type-micro">Hover</p>
                  <Button variant="primary" className="is-force-hover" tabIndex={-1}>
                    Primary
                  </Button>
                  <Button variant="secondary" className="is-force-hover" tabIndex={-1}>
                    Secondary
                  </Button>
                  <button type="button" className="tab is-force-hover" tabIndex={-1}>
                    Tab
                  </button>
                  <TextInput className="is-force-hover" defaultValue="Hover border" readOnly tabIndex={-1} />
                </div>
                <div className="ui-showcase-state-card">
                  <p className="type-micro">Selected</p>
                  <div className="surface-selected ui-showcase-surface">
                    <p className="type-compact">.surface-selected</p>
                  </div>
                  <button type="button" className="tab tab-active" tabIndex={-1}>
                    Selected tab
                  </button>
                  <CompactDataRow as="div" selected primary="Selected listing" metadata="24" />
                </div>
                <div className="ui-showcase-state-card">
                  <p className="type-micro">Disabled</p>
                  <Button disabled>Primary</Button>
                  <IconButton label="Disabled settings" disabled>
                    <UiIcon state="disabled">
                      <ChromeIcon name="settings" className="" />
                    </UiIcon>
                  </IconButton>
                  <TextInput defaultValue="Locked name" disabled />
                  <SearchInput defaultValue="Locked search" disabled />
                </div>
                <div className="ui-showcase-state-card">
                  <p className="type-micro">Error</p>
                  <TextInput defaultValue="" error placeholder="Required" />
                  <SearchInput defaultValue="" error placeholder="No matches" />
                  <Select error defaultValue="">
                    <option value="">Choose a district</option>
                    <option value="harbour">Harbour</option>
                  </Select>
                </div>
                <div className="ui-showcase-state-card">
                  <p className="type-micro">Focus</p>
                  <Button variant="secondary" className="is-force-focus" tabIndex={-1}>
                    Focus ring
                  </Button>
                  <TextInput className="is-force-focus" defaultValue="Focused field" readOnly tabIndex={-1} />
                </div>
                <div className="ui-showcase-state-card">
                  <p className="type-micro">Loading</p>
                  <Button loading>Saving</Button>
                  <Button variant="secondary" loading>
                    Listing
                  </Button>
                </div>
              </div>
            </Section>
          </Panel>
        </div>
      </div>
    </div>
  )
}

function activityArt(variant: ActivityRowVariant): string {
  switch (variant) {
    case 'pvp':
      return 'swords.webp'
    case 'market':
      return 'gold.webp'
    case 'warning':
      return 'alert.webp'
    case 'reward':
      return 'chest.webp'
    case 'completed':
      return 'chest.webp'
    case 'system':
      return 'scroll.webp'
    default:
      return 'scroll.webp'
  }
}
