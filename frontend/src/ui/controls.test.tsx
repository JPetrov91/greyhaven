// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { Button } from './Button'
import { IconButton } from './IconButton'
import { Tabs } from './Tabs'

afterEach(() => {
  cleanup()
})

describe('Button', () => {
  it('defaults to primary and supports each variant', () => {
    const { rerender } = render(<Button>Save</Button>)
    expect(screen.getByRole('button').className.split(' ')).toEqual(expect.arrayContaining(['btn', 'btn-primary']))

    rerender(<Button variant="secondary">Save</Button>)
    expect(screen.getByRole('button').className).toContain('btn-secondary')

    rerender(<Button variant="danger">Delete</Button>)
    expect(screen.getByRole('button').className).toContain('btn-danger')

    rerender(<Button variant="ghost">Cancel</Button>)
    expect(screen.getByRole('button').className).toContain('btn-ghost')
  })

  it('locks the control while loading', () => {
    render(
      <Button loading data-testid="save">
        Save
      </Button>,
    )
    const button = screen.getByTestId('save')
    expect(button).toHaveProperty('disabled', true)
    expect(button.getAttribute('aria-busy')).toBe('true')
    expect(button.className).toContain('btn-loading')
  })
})

describe('IconButton', () => {
  it('uses the button language with a required accessible name', () => {
    render(
      <IconButton label="Allocate strength" data-testid="plus">
        +
      </IconButton>,
    )
    const button = screen.getByTestId('plus')
    expect(button.getAttribute('aria-label')).toBe('Allocate strength')
    expect(button.className.split(' ')).toEqual(expect.arrayContaining(['btn', 'btn-icon', 'btn-secondary']))
  })
})

describe('Tabs', () => {
  it('marks the selected tab and skips disabled tabs', () => {
    const onChange = vi.fn()
    render(
      <Tabs
        label="Views"
        value="gear"
        onChange={onChange}
        tabs={[
          { id: 'gear', label: 'Gear' },
          { id: 'later', label: 'Housing', disabled: true },
          { id: 'log', label: 'Log' },
        ]}
      />,
    )

    const selected = screen.getByRole('tab', { name: 'Gear' })
    const disabled = screen.getByRole('tab', { name: 'Housing' })
    expect(selected.getAttribute('aria-selected')).toBe('true')
    expect(selected.className).toContain('tab-active')
    expect(disabled).toHaveProperty('disabled', true)

    fireEvent.click(disabled)
    expect(onChange).not.toHaveBeenCalled()

    fireEvent.keyDown(selected, { key: 'ArrowRight' })
    expect(onChange).toHaveBeenCalledWith('log')
  })

  it('marks filter groups without changing tab semantics', () => {
    render(
      <Tabs
        kind="filters"
        label="Rarity"
        value="all"
        onChange={vi.fn()}
        tabs={[
          { id: 'all', label: 'All' },
          { id: 'rare', label: 'Rare' },
        ]}
      />,
    )

    expect(screen.getByRole('group', { name: 'Rarity' }).className).toContain('tabs-filters')
    expect(screen.getByRole('button', { name: 'All' }).getAttribute('aria-pressed')).toBe('true')
  })
})
