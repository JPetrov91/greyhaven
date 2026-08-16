// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { IconButton } from './IconButton'
import { IconWell } from './IconWell'
import { ICON_GRID, ICON_STROKE, ORNAMENT_NAMES, ORNAMENT_STROKE } from './iconography'
import { Ornament } from './Ornament'
import { SectionHeader } from './SectionHeader'
import { UiIcon } from './UiIcon'

afterEach(() => {
  cleanup()
})

describe('icon contract', () => {
  it('locks chrome glyphs to a 24 grid and 1.6 stroke, ornaments to 1px metal', () => {
    expect(ICON_GRID).toBe(24)
    expect(ICON_STROKE).toBe(1.6)
    expect(ORNAMENT_STROKE).toBe(1)
  })

  it('does not grow the ornament pack', () => {
    expect(ORNAMENT_NAMES).toEqual(['divider', 'corner', 'diamond', 'accent'])
  })
})

describe('UiIcon', () => {
  it('defaults to md and can mark disabled, active, and painted art', () => {
    const { rerender } = render(
      <UiIcon data-testid="icon">
        <svg viewBox="0 0 24 24" />
      </UiIcon>,
    )
    expect(screen.getByTestId('icon').className.split(' ')).toEqual(
      expect.arrayContaining(['ui-icon', 'ui-icon-md']),
    )
    expect(screen.getByTestId('icon').getAttribute('aria-hidden')).toBe('true')

    rerender(
      <UiIcon size="lg" state="disabled" data-testid="icon">
        <svg viewBox="0 0 24 24" />
      </UiIcon>,
    )
    expect(screen.getByTestId('icon').className).toContain('ui-icon-lg')
    expect(screen.getByTestId('icon').className).toContain('ui-icon-disabled')

    rerender(
      <UiIcon art state="active" data-testid="icon">
        <img src="/icons/nav/home.webp" alt="" />
      </UiIcon>,
    )
    expect(screen.getByTestId('icon').className.split(' ')).toEqual(
      expect.arrayContaining(['ui-icon', 'ui-icon-art', 'ui-icon-active']),
    )
  })
})

describe('IconWell', () => {
  it('sizes a quiet metal well and can mark the active container', () => {
    const { rerender } = render(
      <IconWell data-testid="well">
        <UiIcon>
          <svg viewBox="0 0 24 24" />
        </UiIcon>
      </IconWell>,
    )
    expect(screen.getByTestId('well').className.split(' ')).toEqual(expect.arrayContaining(['ui-icon-well']))
    expect(screen.getByTestId('well').className).not.toMatch(/ui-icon-well-lg|ui-icon-well-active/)

    rerender(
      <IconWell size="lg" active data-testid="well">
        <UiIcon>
          <svg viewBox="0 0 24 24" />
        </UiIcon>
      </IconWell>,
    )
    expect(screen.getByTestId('well').className.split(' ')).toEqual(
      expect.arrayContaining(['ui-icon-well', 'ui-icon-well-lg', 'ui-icon-well-active']),
    )
  })
})

describe('Ornament', () => {
  it('renders each pack mark from the shared asset classes', () => {
    for (const name of ORNAMENT_NAMES) {
      const { unmount } = render(<Ornament name={name} data-testid={`ornament-${name}`} />)
      const mark = screen.getByTestId(`ornament-${name}`)
      expect(mark.className.split(' ')).toEqual(expect.arrayContaining(['ui-ornament', `ui-ornament-${name}`]))
      expect(mark.querySelector('svg')).toBeNull()
      unmount()
    }
  })

  it('flips corner placement without extra assets', () => {
    render(<Ornament name="corner" corner="br" data-testid="corner" />)
    expect(screen.getByTestId('corner').className).toContain('ui-ornament-br')
  })
})

describe('SectionHeader accent', () => {
  it('keeps the heading pip opt-in', () => {
    const { rerender } = render(<SectionHeader>Navigation</SectionHeader>)
    expect(document.querySelector('.ui-ornament-accent')).toBeNull()

    rerender(<SectionHeader accent>Navigation</SectionHeader>)
    expect(document.querySelector('.ui-ornament-accent')).not.toBeNull()
    expect(screen.getByRole('heading', { level: 3 }).textContent).toBe('Navigation')
  })
})

describe('IconButton glyph size', () => {
  it('keeps a raw svg child on the shared md size class contract', () => {
    render(
      <IconButton label="Settings">
        <svg data-testid="glyph" viewBox="0 0 24 24" />
      </IconButton>,
    )
    expect(screen.getByTestId('glyph')).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Settings' }).className).toContain('btn-icon')
  })
})
