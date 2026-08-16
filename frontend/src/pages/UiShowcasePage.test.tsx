// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { UiShowcasePage } from './UiShowcasePage'

afterEach(() => {
  cleanup()
})

describe('UiShowcasePage', () => {
  it('opens with a generic game composition and the component reference', () => {
    render(<UiShowcasePage />)

    expect(screen.getByTestId('ui-showcase')).toBeTruthy()
    expect(screen.getByTestId('ui-shell')).toBeTruthy()
    expect(screen.getByTestId('ui-shell-main')).toBeTruthy()
    expect(screen.getByTestId('ui-shell-ledger')).toBeTruthy()
    expect(screen.getByTestId('ui-shell-inspector')).toBeTruthy()
    expect(screen.getByTestId('ui-shell-strip')).toBeTruthy()
    expect(screen.getByTestId('ui-shell-chat')).toBeTruthy()
    expect(screen.getByTestId('ui-shell-rail')).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'Records' })).toBeTruthy()
    expect(screen.getByText('Component Reference')).toBeTruthy()
  })

  it('exposes every visual QA chapter on one page', () => {
    render(<UiShowcasePage />)

    expect(screen.getByTestId('showcase-colors')).toBeTruthy()
    expect(screen.getByTestId('showcase-surfaces')).toBeTruthy()
    expect(screen.getByTestId('showcase-typography')).toBeTruthy()
    expect(screen.getByTestId('showcase-type-acceptance')).toBeTruthy()
    expect(screen.getByText('Marketplace')).toBeTruthy()
    expect(screen.getAllByText('Navigation').length).toBeGreaterThan(0)
    expect(screen.getByText('+12 Strength · Safe Zone')).toBeTruthy()
    expect(screen.getByTestId('showcase-icon-scale')).toBeTruthy()
    expect(screen.getByTestId('showcase-icon-wells')).toBeTruthy()
    expect(screen.getByTestId('showcase-selected-marks')).toBeTruthy()
    expect(document.querySelector('.ui-mark-frame')).not.toBeNull()
    expect(document.querySelector('.ui-icon-well-active')).not.toBeNull()
    expect(screen.getByTestId('showcase-controls')).toBeTruthy()
    expect(screen.getByTestId('showcase-examples')).toBeTruthy()
    expect(screen.getByTestId('showcase-example-panel')).toBeTruthy()
    expect(screen.getByTestId('showcase-forms')).toBeTruthy()
    expect(screen.getByTestId('showcase-meters')).toBeTruthy()
    expect(screen.getByTestId('showcase-meter-samples')).toBeTruthy()
    expect(screen.getByTestId('showcase-scrollbar')).toBeTruthy()
    expect(
      Array.from(document.querySelectorAll('.tooltip-ledger-name')).some((node) => node.textContent === 'Verdant Signet'),
    ).toBe(true)
    expect(screen.getByText('Battle Shout')).toBeTruthy()
    expect(screen.getByTestId('showcase-rows')).toBeTruthy()
    expect(screen.getByTestId('showcase-activity-feed')).toBeTruthy()
    expect(screen.getByTestId('showcase-notification-stack')).toBeTruthy()
    expect(screen.getByTestId('showcase-market-list')).toBeTruthy()
    expect(screen.getByTestId('showcase-compact-log')).toBeTruthy()
    expect(document.querySelector('.ui-row-selected')).not.toBeNull()
    expect(document.querySelector('.ui-notification-row')).not.toBeNull()
    expect(document.querySelector('.ui-compact-row')).not.toBeNull()
    expect(screen.getByTestId('showcase-states')).toBeTruthy()
  })

  it('shows the surface composition and the required control states', () => {
    render(<UiShowcasePage />)

    for (const surface of ['page', 'base', 'raised', 'inset', 'interactive', 'selected', 'floating']) {
      expect(screen.getByTestId(`surface-${surface}`).className).toContain(`surface-${surface}`)
    }
    expect(screen.getByTestId('surface-raised').closest('[data-testid="surface-base"]')).not.toBeNull()
    expect(screen.getByTestId('surface-inset').closest('[data-testid="surface-base"]')).not.toBeNull()
    expect(screen.getByTestId('surface-floating').closest('[data-testid="surface-base"]')).not.toBeNull()

    expect(screen.getByRole('button', { name: 'Saving' }).className).toContain('btn-loading')
    expect(document.querySelector('.is-force-hover')).not.toBeNull()
    expect(document.querySelector('.is-force-focus')).not.toBeNull()
    expect(document.querySelector('.ui-control-error')).not.toBeNull()
    expect(document.querySelector('.tab-active')).not.toBeNull()
    expect(document.querySelector('button.btn:disabled')).not.toBeNull()
  })
})
