// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { ActivityRow } from './ActivityRow'
import { CompactDataRow } from './CompactDataRow'
import { GenericRow } from './GenericRow'
import { NotificationRow } from './NotificationRow'

afterEach(() => {
  cleanup()
})

describe('GenericRow', () => {
  it('renders icon, primary, secondary, metadata, and action slots', () => {
    render(
      <GenericRow
        testId="row"
        icon={<img src="/icon.webp" alt="" />}
        primary="Iron Sword"
        secondary="Weapon"
        metadata="12g"
        action={<button type="button">Buy</button>}
      />,
    )

    const row = screen.getByTestId('row')
    expect(row.tagName).toBe('DIV')
    expect(row.className.split(' ')).toEqual(expect.arrayContaining(['ui-row', 'ui-row-has-icon', 'ui-row-has-trail']))
    expect(row.querySelector('.ui-row-icon img')?.getAttribute('src')).toBe('/icon.webp')
    expect(row.querySelector('.ui-row-primary')?.textContent).toBe('Iron Sword')
    expect(row.querySelector('.ui-row-secondary')?.textContent).toBe('Weapon')
    expect(row.querySelector('.ui-row-meta')?.textContent).toBe('12g')
    expect(screen.getByRole('button', { name: 'Buy' })).toBeTruthy()
  })

  it('can render as a transparent row button', () => {
    render(<GenericRow as="button" testId="row" primary="Issued Steel" interactive />)
    const row = screen.getByTestId('row')
    expect(row.tagName).toBe('BUTTON')
    expect(row.getAttribute('type')).toBe('button')
    expect(row.className.split(' ')).toEqual(expect.arrayContaining(['ui-row', 'ui-row-interactive']))
  })

  it('omits empty slots and can render as a list item', () => {
    render(<GenericRow as="li" testId="row" primary="Only title" />)
    const row = screen.getByTestId('row')
    expect(row.tagName).toBe('LI')
    expect(row.className).not.toMatch(/ui-row-has-icon|ui-row-has-trail|ui-row-selected|ui-row-interactive/)
    expect(row.querySelector('.ui-row-icon')).toBeNull()
    expect(row.querySelector('.ui-row-secondary')).toBeNull()
    expect(row.querySelector('.ui-row-trail')).toBeNull()
  })

  it('marks selected and important rows without extra slots', () => {
    render(<GenericRow testId="row" primary="Selected listing" selected tone="important" />)
    const row = screen.getByTestId('row')
    expect(row.className.split(' ')).toEqual(
      expect.arrayContaining(['ui-row-selected', 'ui-row-interactive', 'ui-row-tone-important']),
    )
    expect(row.getAttribute('aria-selected')).toBe('true')
  })
})

describe('ActivityRow', () => {
  it('defaults to a list item with the normal variant', () => {
    render(<ActivityRow testId="activity" primary="Patrol returned." />)
    expect(screen.getByTestId('activity').tagName).toBe('LI')
    expect(screen.getByTestId('activity').className.split(' ')).toEqual(
      expect.arrayContaining(['ui-row', 'ui-activity-row', 'ui-activity-normal']),
    )
  })

  it('applies restrained variant classes', () => {
    const { rerender } = render(<ActivityRow variant="reward" testId="activity" primary="Reward" />)
    expect(screen.getByTestId('activity').className).toContain('ui-activity-reward')

    rerender(<ActivityRow variant="pvp" testId="activity" primary="Duel" />)
    expect(screen.getByTestId('activity').className).toContain('ui-activity-pvp')

    rerender(<ActivityRow variant="completed" testId="activity" primary="Claimed" />)
    expect(screen.getByTestId('activity').className).toContain('ui-activity-completed')
  })
})

describe('NotificationRow', () => {
  it('renders an interactive notification with unread and variant marks', () => {
    render(
      <NotificationRow
        testId="notice"
        variant="reward"
        unread
        primary="Daily reward available"
        action={<button type="button">Claim</button>}
      />,
    )
    expect(screen.getByTestId('notice').className.split(' ')).toEqual(
      expect.arrayContaining([
        'ui-row',
        'ui-notification-row',
        'ui-activity-reward',
        'ui-notification-unread',
        'ui-row-interactive',
      ]),
    )
    expect(screen.getByRole('button', { name: 'Claim' })).toBeTruthy()
  })
})

describe('CompactDataRow', () => {
  it('renders a dense selected listing row', () => {
    render(
      <CompactDataRow
        testId="listing"
        selected
        primary="Vicious Mercenary Longsword"
        secondary="Epic · Weapon"
        metadata="1,840"
      />,
    )
    expect(screen.getByTestId('listing').className.split(' ')).toEqual(
      expect.arrayContaining(['ui-row', 'ui-compact-row', 'ui-row-selected', 'ui-row-interactive']),
    )
  })
})
