import { useEffect, useId, useRef, useState } from 'react'
import { Button } from '../../ui/Button'
import { CharacterPortrait } from '../../ui/CharacterPortrait'
import { ChromeHint } from '../../ui/ChromeHint'
import { ChromeIcon } from '../../ui/chromeIcons'
import { CounterBadge } from '../../ui/CounterBadge'
import { IconButton } from '../../ui/IconButton'
import { UiIcon } from '../../ui/UiIcon'
import { DEV_UI_MAIN_SHELL_PATH } from '../devUi'
import { mainShellCharacter, mainShellCurrencies, mainShellInventory } from '../mainShellVisualFixture'

function formatAmount(value: number): string {
  return value.toLocaleString('en-US')
}

function CurrencyFace({
  kind,
  label,
  value,
}: {
  kind: 'gold' | 'silver' | 'honor' | 'credits'
  label: string
  value: string
}) {
  return (
    <>
      <img src={`/chrome/currency-${kind}.webp`} alt="" className="currency-icon" />
      <span className="currency-copy">
        <span className="type-micro">{label}</span>
        <strong className="type-numeric type-numeric-gold">{value}</strong>
      </span>
    </>
  )
}

/** Visual copy of production GameTopBar. Fixture data only. New UI Engine finish. */
export function MainShellGameTopBar() {
  const character = mainShellCharacter
  const itemCount = mainShellInventory.usedSlots
  const menuId = useId()
  const menuRef = useRef<HTMLDivElement>(null)
  const [menuOpen, setMenuOpen] = useState(false)

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

  return (
    <header className="game-topbar surface-base">
      <div className="game-topbar-left">
        <a href={DEV_UI_MAIN_SHELL_PATH} className="brand type-display">
          <img src="/auth/crest.svg" alt="" className="brand-crest" />
          Greyhaven
        </a>
        <span className="topbar-divider" aria-hidden="true" />
        <div className="game-topbar-identity" data-testid="topbar-identity">
          <CharacterPortrait className="topbar-portrait" avatarCode={character.avatarCode} />
          <div>
            <p className="topbar-name type-item">{character.name}</p>
            <p className="topbar-level type-meta">Level {character.level}</p>
          </div>
        </div>
      </div>
      <div className="currency-row" aria-label="Currencies">
        <span className="currency-chip surface-inset" data-testid="topbar-silver">
          <CurrencyFace kind="silver" label="Silver" value={formatAmount(mainShellCurrencies.silver)} />
        </span>
        <span className="currency-chip surface-inset" data-testid="topbar-gold">
          <CurrencyFace kind="gold" label="Gold" value={formatAmount(mainShellCurrencies.gold)} />
        </span>
        <span className="currency-chip surface-inset" data-testid="topbar-marks">
          <CurrencyFace kind="honor" label="Marks" value={formatAmount(mainShellCurrencies.marks)} />
        </span>
        <span className="currency-chip surface-inset" data-testid="topbar-credits">
          <CurrencyFace kind="credits" label="Credits" value={formatAmount(mainShellCurrencies.credits)} />
        </span>
      </div>
      <div className="game-topbar-right utility-row">
        <ChromeHint label="Trophy">
          <IconButton label="Trophy" data-testid="topbar-achievements" disabled>
            <UiIcon state="disabled">
              <ChromeIcon name="trophy" />
            </UiIcon>
          </IconButton>
        </ChromeHint>
        <ChromeHint label="Mail">
          <IconButton label="Mail" data-testid="topbar-mail" disabled>
            <UiIcon state="disabled">
              <ChromeIcon name="mail" />
            </UiIcon>
          </IconButton>
        </ChromeHint>
        <ChromeHint label="Inventory">
          <span className="ms-topbar-pack">
            <IconButton label="Inventory" data-testid="topbar-inventory">
              <UiIcon>
                <ChromeIcon name="pack" />
              </UiIcon>
            </IconButton>
            {itemCount > 0 ? <CounterBadge count={itemCount} tone="accent" /> : null}
          </span>
        </ChromeHint>
        <ChromeHint label="Friends">
          <IconButton label="Friends" data-testid="topbar-friends" disabled>
            <UiIcon state="disabled">
              <ChromeIcon name="friends" />
            </UiIcon>
          </IconButton>
        </ChromeHint>
        <ChromeHint label="Store">
          <IconButton label="Store" data-testid="topbar-store" disabled>
            <UiIcon state="disabled">
              <ChromeIcon name="store" />
            </UiIcon>
          </IconButton>
        </ChromeHint>
        <ChromeHint label="Settings">
          <IconButton label="Settings" data-testid="topbar-settings" disabled>
            <UiIcon state="disabled">
              <ChromeIcon name="settings" />
            </UiIcon>
          </IconButton>
        </ChromeHint>
        <div className="chrome-menu" ref={menuRef}>
          <ChromeHint label="Menu">
            <IconButton
              label="Menu"
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
              <Button type="button" variant="ghost" role="menuitem" data-testid="logout-button">
                Logout
              </Button>
            </div>
          ) : null}
        </div>
      </div>
    </header>
  )
}
