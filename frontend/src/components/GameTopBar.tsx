import { useEffect, useId, useRef, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/AuthContext'
import { fetchCharacter } from '../api/character'
import { fetchInventory } from '../api/inventory'
import type { ProgressionResponse } from '../api/types'
import { Button } from '../ui/Button'
import { CharacterPortrait } from '../ui/CharacterPortrait'
import { ChromeHint } from '../ui/ChromeHint'
import { ChromeIcon } from '../ui/chromeIcons'
import { ComingLaterButton, ComingLaterChip, COMING_LATER_LABEL } from '../ui/ComingLater'
import { CounterBadge } from '../ui/CounterBadge'
import { gameLink, isGameNavActive } from '../ui/gameNav'
import { IconButton } from '../ui/IconButton'
import { UiIcon } from '../ui/UiIcon'
import { XPBar } from '../ui/XPBar'

type CombatContext = {
  monsterName: string
  roundNumber: number
}

type Props = {
  combatContext?: CombatContext | null
}

export function GameTopBar({ combatContext = null }: Props) {
  const { me, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const menuId = useId()
  const menuRef = useRef<HTMLDivElement>(null)
  const [menuOpen, setMenuOpen] = useState(false)
  const characterQuery = useQuery({
    queryKey: ['character'],
    queryFn: fetchCharacter,
    retry: false,
  })
  const inventoryQuery = useQuery({
    queryKey: ['inventory'],
    queryFn: fetchInventory,
    retry: false,
    enabled: !!characterQuery.data,
  })

  const character = characterQuery.data
  const itemCount = inventoryQuery.data?.usedSlots ?? 0
  const packActive = isGameNavActive('inventory', location)

  useEffect(() => {
    if (!menuOpen) {
      return
    }
    function onPointerDown(event: MouseEvent) {
      if (!menuRef.current?.contains(event.target as Node)) {
        setMenuOpen(false)
      }
    }
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('mousedown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [menuOpen])

  async function handleLogout() {
    setMenuOpen(false)
    try {
      await logout()
    } finally {
      navigate('/login', { replace: true })
    }
  }

  return (
    <header className="game-topbar surface-raised">
      <div className="game-topbar-left">
        <Link to={gameLink('home')} className="brand type-display">
          <img src="/auth/crest.svg" alt="" className="brand-crest" />
          Greyhaven
        </Link>
        <span className="topbar-divider" aria-hidden="true" />
        {character ? (
          <div className="game-topbar-identity" data-testid="topbar-identity">
            <CharacterPortrait className="topbar-portrait" avatarCode={character.avatarCode} />
            <div className="topbar-identity-copy">
              <p className="topbar-name type-item">{character.name}</p>
              <div className="topbar-progress">
                <p className="topbar-level type-meta">Level {character.level}</p>
                <TopbarXpBar progression={character.progression} />
              </div>
            </div>
          </div>
        ) : (
          <span className="type-meta">{me?.email}</span>
        )}
        {combatContext ? (
          <p className="topbar-combat-chip" data-testid="topbar-combat-context">
            COMBAT — {combatContext.monsterName} · Round {combatContext.roundNumber}
          </p>
        ) : null}
      </div>
      <div className="currency-row" aria-label="Currencies">
        <ComingLaterChip testId="topbar-silver" className="currency-chip surface-inset">
          <CurrencyFace kind="silver" label="Silver" />
        </ComingLaterChip>
        <span className="currency-chip surface-inset" data-testid="topbar-gold">
          <CurrencyFace kind="gold" label="Gold" value={character?.gold.toLocaleString('en-US') ?? '—'} />
        </span>
        <span className="currency-chip surface-inset" data-testid="topbar-marks">
          <CurrencyFace kind="honor" label="Marks" value={character?.arenaMarks.toLocaleString('en-US') ?? '—'} />
        </span>
        <ComingLaterChip testId="topbar-credits" className="currency-chip surface-inset">
          <CurrencyFace kind="credits" label="Credits" />
        </ComingLaterChip>
      </div>
      <div className="game-topbar-right utility-row">
        <ChromeHint label={`Trophy — ${COMING_LATER_LABEL}`}>
          <ComingLaterButton data-testid="topbar-achievements" className="btn-icon-chrome" aria-label="Trophy" title="">
            <ChromeIcon name="trophy" />
          </ComingLaterButton>
        </ChromeHint>
        <ChromeHint label={`Mail — ${COMING_LATER_LABEL}`}>
          <ComingLaterButton data-testid="topbar-mail" className="btn-icon-chrome" aria-label="Mail" title="">
            <ChromeIcon name="mail" />
          </ComingLaterButton>
        </ChromeHint>
        <ChromeHint label="Inventory">
          <span className="topbar-pack">
            <Link
              to={gameLink('inventory')}
              data-testid="topbar-inventory"
              className="btn btn-ghost btn-icon-chrome"
              aria-label="Inventory"
              aria-current={packActive ? 'page' : undefined}
            >
              <UiIcon>
                <ChromeIcon name="pack" />
              </UiIcon>
            </Link>
            {itemCount > 0 ? <CounterBadge count={itemCount} tone="accent" /> : null}
          </span>
        </ChromeHint>
        <ChromeHint label={`Friends — ${COMING_LATER_LABEL}`}>
          <ComingLaterButton data-testid="topbar-friends" className="btn-icon-chrome" aria-label="Friends" title="">
            <ChromeIcon name="friends" />
          </ComingLaterButton>
        </ChromeHint>
        <ChromeHint label={`Store — ${COMING_LATER_LABEL}`}>
          <ComingLaterButton data-testid="topbar-store" className="btn-icon-chrome" aria-label="Store" title="">
            <ChromeIcon name="store" />
          </ComingLaterButton>
        </ChromeHint>
        <ChromeHint label={`Settings — ${COMING_LATER_LABEL}`}>
          <ComingLaterButton data-testid="topbar-settings" className="btn-icon-chrome" aria-label="Settings" title="">
            <ChromeIcon name="settings" />
          </ComingLaterButton>
        </ChromeHint>
        <div className="chrome-menu" ref={menuRef}>
          <ChromeHint label="Menu">
            <IconButton
              label="Menu"
              variant="ghost"
              className="btn-icon-chrome"
              data-testid="topbar-menu"
              aria-haspopup="menu"
              aria-expanded={menuOpen}
              aria-controls={menuId}
              onClick={() => setMenuOpen((open) => !open)}
            >
              <UiIcon>
                <ChromeIcon name="menu" />
              </UiIcon>
            </IconButton>
          </ChromeHint>
          {menuOpen ? (
            <div className="chrome-menu-panel surface-floating" id={menuId} role="menu" data-testid="topbar-menu-panel">
              <Button type="button" variant="ghost" role="menuitem" data-testid="logout-button" onClick={() => void handleLogout()}>
                Logout
              </Button>
            </div>
          ) : null}
        </div>
      </div>
    </header>
  )
}

function TopbarXpBar({ progression }: { progression?: ProgressionResponse }) {
  if (!progression) {
    return null
  }
  const percent = progression.maxLevel ? 100 : Math.min(100, Math.max(0, progression.progressPercent))
  const percentLabel = `${percent}%`
  return (
    <XPBar
      className="topbar-xp"
      density="compact"
      value={percent}
      max={100}
      showValue
      valuePlacement="beside"
      valueText={percentLabel}
      label={progression.maxLevel ? 'Maximum level' : `${percentLabel} to next level`}
      testId="topbar-xp"
    />
  )
}

function CurrencyFace({
  kind,
  label,
  value,
}: {
  kind: 'gold' | 'silver' | 'honor' | 'credits'
  label: string
  value?: string
}) {
  return (
    <>
      <img src={`/chrome/currency-${kind}.webp`} alt="" className="currency-icon" />
      <span className="currency-copy">
        <span className="type-micro">{label}</span>
        {value ? <strong className="type-numeric type-numeric-gold"> {value}</strong> : null}
      </span>
    </>
  )
}
