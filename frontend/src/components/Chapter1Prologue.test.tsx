// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { Chapter1Prologue } from './Chapter1Prologue'

afterEach(() => {
  cleanup()
})

describe('Chapter1Prologue', () => {
  it('inserts the created name and never names Bren', () => {
    render(<Chapter1Prologue name="Thorne" onFinished={() => undefined} />)
    expect(screen.getByTestId('chapter1-kicker').textContent).toBe('Chapter 1')
    expect(screen.getByTestId('chapter1-title').textContent).toBe('The Open Gates')
    fireEvent.click(screen.getByTestId('chapter1-continue'))
    expect(screen.getByText(/“Thorne.”/)).toBeTruthy()
    expect(screen.queryByText(/Bren/i)).toBeNull()
  })

  it('Skip and Enter the Square both finish', () => {
    const onFinished = vi.fn()
    render(<Chapter1Prologue name="Thorne" onFinished={onFinished} />)
    fireEvent.click(screen.getByTestId('chapter1-skip'))
    expect(onFinished).toHaveBeenCalledTimes(1)

    onFinished.mockClear()
    cleanup()
    render(<Chapter1Prologue name="Thorne" onFinished={onFinished} />)
    fireEvent.click(screen.getByTestId('chapter1-continue'))
    fireEvent.click(screen.getByTestId('chapter1-continue'))
    fireEvent.click(screen.getByTestId('chapter1-continue'))
    fireEvent.click(screen.getByTestId('chapter1-enter'))
    expect(onFinished).toHaveBeenCalledTimes(1)
  })
})
