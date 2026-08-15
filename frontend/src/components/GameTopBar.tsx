import { useEffect, useId, useRef, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/AuthContext'
import { fetchCharacter } from '../api/character'
import { fetchInventory } from '../api/inventory'
import { Button } from '../ui/Button'
import { CharacterPortrait } from '../ui/CharacterPortrait'
import { ChromeHint } from '../ui/ChromeHint'
import { ChromeIcon } from '../ui/chromeIcons'
import { ComingLaterButton, ComingLaterChip, COMING_LATER_LABEL } from '../ui/ComingLater'
import { gameLink, isGameNavActive } from '../ui/gameNav'

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
    <header className="game-topbar">
      <div className="game-topbar-left">
        <Link to={gameLink('home')} className="brand">
          <img src="/auth/crest.svg" alt="" className="brand-crest" />
          Greyhaven
        </Link>
        <span className="topbar-divider" aria-hidden="true" />
        {character ? (
          <div className="game-topbar-identity" data-testid="topbar-identity">
            <CharacterPortrait className="topbar-portrait" avatarCode={character.avatarCode} />
            <div>
              <p className="topbar-name">{character.name}</p>
              <p className="topbar-level muted">Level {character.level}</p>
            </div>
          </div>
        ) : (
          <span className="muted">{me?.email}</span>
        )}
        {combatContext ? (
          <p className="topbar-combat-chip" data-testid="topbar-combat-context">
            COMBAT — {combatContext.monsterName} · Round {combatContext.roundNumber}
          </p>
        ) : null}
      </div>
      <div className="currency-row" aria-label="Currencies">
        <ComingLaterChip testId="topbar-silver">
          <CurrencyFace kind="silver" label="Silver" />
        </ComingLaterChip>
        <span className="currency-chip" data-testid="topbar-gold">
          <CurrencyFace kind="gold" label="Gold" value={character?.gold.toLocaleString('en-US') ?? '—'} />
        </span>
        <span className="currency-chip" data-testid="topbar-marks">
          <CurrencyFace kind="honor" label="Marks" value={character?.arenaMarks.toLocaleString('en-US') ?? '—'} />
        </span>
        <ComingLaterChip testId="topbar-credits">
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
          <Link
            to={gameLink('inventory')}
            data-testid="topbar-inventory"
            className="btn btn-ghost btn-icon-chrome"
            aria-label="Inventory"
            aria-current={packActive ? 'page' : undefined}
          >
            <ChromeIcon name="pack" />
            {itemCount > 0 ? <span className="inventory-badge">{itemCount}</span> : null}
          </Link>
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
            <Button
              type="button"
              variant="ghost"
              className="btn-icon-chrome"
              data-testid="topbar-menu"
              aria-label="Menu"
              aria-haspopup="menu"
              aria-expanded={menuOpen}
              aria-controls={menuId}
              onClick={() => setMenuOpen((open) => !open)}
            >
              <ChromeIcon name="menu" />
            </Button>
          </ChromeHint>
          {menuOpen ? (
            <div className="chrome-menu-panel" id={menuId} role="menu" data-testid="topbar-menu-panel">
              <button type="button" role="menuitem" data-testid="logout-button" onClick={() => void handleLogout()}>
                Logout
              </button>
            </div>
          ) : null}
        </div>
      </div>
    </header>
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
        <span className="muted">{label}</span>
        {value ? <strong> {value}</strong> : null}
      </span>
    </>
  )
}
