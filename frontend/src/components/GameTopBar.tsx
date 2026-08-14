import { NavLink, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/AuthContext'
import { fetchCharacter } from '../api/character'
import { fetchInventory } from '../api/inventory'
import { Button } from '../ui/Button'
import { ComingLaterButton, ComingLaterChip } from '../ui/ComingLater'
import { gameLink } from '../ui/gameNav'

export function GameTopBar() {
  const { me, logout } = useAuth()
  const navigate = useNavigate()
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
  const initial = character?.name.trim().charAt(0).toUpperCase() || '?'
  const itemCount = inventoryQuery.data?.usedSlots ?? 0

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
        <NavLink to={gameLink('home')} className="brand">
          Greyhaven
        </NavLink>
        {character ? (
          <div className="game-topbar-identity" data-testid="topbar-identity">
            <div className="portrait topbar-portrait" aria-hidden="true">
              {initial}
            </div>
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
          <span className="muted">Gold</span>
          <strong>{character?.gold.toLocaleString('en-US') ?? '—'}</strong>
        </span>
        <ComingLaterChip testId="topbar-silver">Silver</ComingLaterChip>
        <ComingLaterChip testId="topbar-honor">Honor</ComingLaterChip>
        <ComingLaterChip testId="topbar-credits">Credits</ComingLaterChip>
      </div>
      <div className="game-topbar-right utility-row">
        <ComingLaterButton data-testid="topbar-achievements">Trophy</ComingLaterButton>
        <ComingLaterButton data-testid="topbar-mail">Mail</ComingLaterButton>
        <NavLink to={gameLink('inventory')} data-testid="topbar-inventory" className="btn btn-ghost">
          Pack
          {itemCount > 0 ? <span className="inventory-badge">{itemCount}</span> : null}
        </NavLink>
        <ComingLaterButton data-testid="topbar-friends">Friends</ComingLaterButton>
        <Button type="button" variant="ghost" data-testid="logout-button" onClick={() => void handleLogout()}>
          Logout
        </Button>
      </div>
    </header>
  )
}
