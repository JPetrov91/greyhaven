// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { ComingLaterButton, ComingLaterChip } from './ComingLater'

afterEach(() => {
  cleanup()
})

describe('Coming later placeholders', () => {
  it('disables future-feature controls without exposing fake values', () => {
    render(
      <>
        <ComingLaterButton data-testid="later-guild">Guild</ComingLaterButton>
        <ComingLaterChip testId="later-honor">Honor</ComingLaterChip>
      </>,
    )

    const button = screen.getByTestId('later-guild')
    expect(button).toHaveProperty('disabled', true)
    expect(button.getAttribute('aria-disabled')).toBe('true')
    expect(button.getAttribute('title')).toBe('Coming later')
    expect(button.querySelector('.coming-later-hint')).toBeNull()
    const honor = screen.getByTestId('later-honor')
    expect(honor.getAttribute('title')).toBe('Coming later')
    expect(honor.textContent?.replace('Coming later', '').trim()).toBe('Honor')
    expect(honor.querySelector('.coming-later-hint')).toBeNull()
    expect(honor.textContent).not.toMatch(/\d/)
  })
})
