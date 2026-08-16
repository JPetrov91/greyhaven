// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { Badge } from './Badge'
import { CounterBadge } from './CounterBadge'
import { DurabilityBar } from './DurabilityBar'
import { HealthBar } from './HealthBar'
import { ProgressBar } from './ProgressBar'
import { StaminaBar } from './StaminaBar'
import { StatusBadge } from './StatusBadge'
import { XPBar } from './XPBar'

afterEach(() => {
  cleanup()
})

describe('ProgressBar family', () => {
  it('renders a native progress meter with tone and density classes', () => {
    render(
      <ProgressBar
        value={12}
        max={20}
        label="Health 12 of 20"
        tone="health"
        density="compact"
        testId="meter"
      />,
    )
    const bar = screen.getByTestId('meter') as HTMLProgressElement
    expect(bar.tagName).toBe('PROGRESS')
    expect(bar.value).toBe(12)
    expect(bar.max).toBe(20)
    expect(bar.getAttribute('aria-label')).toBe('Health 12 of 20')
    expect(bar.className.split(' ')).toEqual(
      expect.arrayContaining(['progress-bar', 'progress-health', 'progress-compact']),
    )
  })

  it('shows an optional numeric value without replacing the progress element', () => {
    render(<ProgressBar value={8} max={10} label="XP" showValue testId="xp" />)
    const bar = screen.getByTestId('xp')
    expect(bar.tagName).toBe('PROGRESS')
    expect(bar.closest('.ui-meter')?.className).toBe('ui-meter')
    expect(bar.closest('.ui-meter')?.querySelector('.ui-meter-value')?.textContent).toBe('8 / 10')
  })

  it('maps specialized bars onto the shared tones', () => {
    render(
      <>
        <HealthBar value={4} max={10} label="Health" testId="health" />
        <StaminaBar value={3} max={10} label="Stamina" testId="stamina" />
        <XPBar value={50} label="XP" testId="xp" />
        <DurabilityBar value={85} label="Durability" density="hairline" testId="durability" />
      </>,
    )
    expect(screen.getByTestId('health').className.split(' ')).toEqual(
      expect.arrayContaining(['progress-health', 'progress-vital']),
    )
    expect(screen.getByTestId('stamina').className.split(' ')).toEqual(
      expect.arrayContaining(['progress-stamina', 'progress-vital']),
    )
    expect(screen.getByTestId('xp').className.split(' ')).toEqual(
      expect.arrayContaining(['progress-bar', 'progress-xp', 'xp-bar']),
    )
    expect(screen.getByTestId('durability').className.split(' ')).toEqual(
      expect.arrayContaining(['progress-durability', 'progress-hairline']),
    )
  })

  it('can overlay a vital value and draw segment ticks', () => {
    render(
      <HealthBar
        value={3850}
        max={3850}
        label="Health"
        showValue
        valuePlacement="overlay"
        valueText="3,850 / 3,850"
        segments={10}
        testId="vital"
      />,
    )
    const frame = screen.getByTestId('vital').closest('.ui-meter')
    expect(frame?.className).toContain('ui-meter-overlay')
    expect(frame?.querySelector('.progress-segments')).not.toBeNull()
    expect(frame?.querySelector('.ui-meter-value')?.textContent).toBe('3,850 / 3,850')
  })
})

describe('Badge family', () => {
  it('keeps compact badge and status geometry classes', () => {
    render(
      <>
        <Badge tone="accent">Listed</Badge>
        <StatusBadge tone="upgrade">Unlocked</StatusBadge>
        <StatusBadge tone="danger" icon={<span />} meta="3 turns">
          Bleed
        </StatusBadge>
        <CounterBadge count={12} />
        <CounterBadge count={140} tone="neutral" max={99} />
      </>,
    )
    expect(screen.getByText('Listed').className.split(' ')).toEqual(
      expect.arrayContaining(['badge', 'badge-accent']),
    )
    expect(screen.getByText('Unlocked').closest('.status-badge')?.className.split(' ')).toEqual(
      expect.arrayContaining(['status-badge', 'status-upgrade']),
    )
    expect(screen.getByText('Bleed').closest('.status-badge')?.className.split(' ')).toEqual(
      expect.arrayContaining(['status-badge-effect', 'status-danger']),
    )
    expect(screen.getByText('3 turns')).toBeTruthy()
    expect(screen.getByText('12').className.split(' ')).toEqual(
      expect.arrayContaining(['counter-badge', 'counter-danger']),
    )
    expect(screen.getByText('99+').className).toContain('counter-neutral')
  })
})
