// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { GenericGameComposition } from './GenericGameComposition'

afterEach(() => {
  cleanup()
})

describe('GenericGameComposition', () => {
  it('builds a full shell from engine primitives and fake records', () => {
    render(<GenericGameComposition />)

    expect(screen.getByTestId('ui-shell')).toBeTruthy()
    expect(screen.getByText('Greyhaven')).toBeTruthy()
    expect(screen.getByText('Edric Varn')).toBeTruthy()
    expect(screen.getByLabelText('Navigation sample')).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'Records' })).toBeTruthy()
    expect(screen.getAllByText('Harbour lamp duty').length).toBeGreaterThan(0)
    expect(screen.getByRole('button', { name: 'Seal' })).toBeTruthy()
    expect(screen.getByLabelText('Chat message')).toBeTruthy()
    expect(screen.getByText('Clerk stipend')).toBeTruthy()
    expect(document.querySelector('.ui-shell-banner')).not.toBeNull()
    expect(document.querySelector('.surface-frame')).not.toBeNull()
  })
})
