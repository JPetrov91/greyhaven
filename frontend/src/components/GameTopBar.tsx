import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/AuthContext'
import { fetchCharacter } from '../api/character'
import { fetchInventory } from '../api/inventory'
import { Button } from '../ui/Button'
import { CharacterPortrait } from '../ui/CharacterPortrait'
import { ChromeIcon } from '../ui/chromeIcons'
import { ComingLaterButton, ComingLaterChip } from '../ui/ComingLater'
import { gameLink, isGameNavActive } from '../ui/gameNav'

export function GameTopBar() {
  const { me, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
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

  async function handleLogout() {
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
          Greyhaven
        </Link>
        <span className="topbar-divider" aria-hidden="true" />
        {character ? (
          <div className="game-topbar-identity" data-testid="topbar-identity">
            <CharacterPortrait className="topbar-portrait" />
            <div>
              <p className="topbar-name">{character.name}</p>
              <p className="topbar-level muted">Level {character.level}</p>
            </div>
          </div>
        ) : (
          <span className="muted">{me?.email}</span>
        )}
      </div>
      <div className="currency-row" aria-label="Currencies">
        <span className="currency-chip" data-testid="topbar-gold">
          <span className="muted">Gold</span>{' '}
          <strong>{character?.gold.toLocaleString('en-US') ?? '—'}</strong>
        </span>
        <ComingLaterChip testId="topbar-silver">Silver</ComingLaterChip>
        <ComingLaterChip testId="topbar-honor">Honor</ComingLaterChip>
        <ComingLaterChip testId="topbar-credits">Credits</ComingLaterChip>
      </div>
      <div className="game-topbar-right utility-row">
        <ComingLaterButton data-testid="topbar-achievements" className="btn-icon-chrome" aria-label="Trophy">
          <ChromeIcon name="trophy" />
        </ComingLaterButton>
        <ComingLaterButton data-testid="topbar-mail" className="btn-icon-chrome" aria-label="Mail">
          <ChromeIcon name="mail" />
        </ComingLaterButton>
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
        <ComingLaterButton data-testid="topbar-friends" className="btn-icon-chrome" aria-label="Friends">
          <ChromeIcon name="friends" />
        </ComingLaterButton>
        <Button type="button" variant="ghost" data-testid="logout-button" onClick={() => void handleLogout()}>
          Logout
        </Button>
      </div>
    </header>
  )
}
