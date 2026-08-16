// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { Dropdown } from './Dropdown'
import { SearchInput } from './SearchInput'
import { Select } from './Select'
import { Textarea } from './Textarea'
import { TextInput } from './TextInput'
import { Tooltip } from './Tooltip'

afterEach(() => {
  cleanup()
})

describe('form primitives', () => {
  it('marks error state on text, search, textarea, and select', () => {
    render(
      <>
        <TextInput error aria-label="Name" />
        <SearchInput error aria-label="Find" />
        <Textarea error aria-label="Notes" />
        <Select error aria-label="Slot">
          <option value="head">Head</option>
        </Select>
      </>,
    )

    expect(screen.getByLabelText('Name').className.split(' ')).toEqual(
      expect.arrayContaining(['ui-control', 'ui-input', 'ui-control-error']),
    )
    expect(screen.getByLabelText('Name').getAttribute('aria-invalid')).toBe('true')
    expect(screen.getByLabelText('Find').getAttribute('type')).toBe('search')
    expect(screen.getByLabelText('Find').className).toContain('ui-control-error')
    expect(screen.getByLabelText('Notes').className).toContain('ui-textarea')
    expect(screen.getByLabelText('Slot').className).toContain('ui-select')
  })

  it('opens a dropdown listbox and chooses an option', () => {
    const onChange = vi.fn()
    render(
      <Dropdown
        aria-label="Rarity"
        testId="rarity-dropdown"
        value="COMMON"
        onChange={onChange}
        options={[
          { value: 'COMMON', label: 'Common' },
          { value: 'RARE', label: 'Rare' },
          { value: 'EPIC', label: 'Epic', disabled: true },
        ]}
      />,
    )

    const trigger = screen.getByTestId('rarity-dropdown')
    expect(trigger.className).toContain('ui-dropdown-trigger')
    fireEvent.click(trigger)
    expect(screen.getByRole('listbox')).toBeTruthy()
    fireEvent.mouseDown(screen.getByRole('option', { name: 'Rare' }))
    expect(onChange).toHaveBeenCalledWith('RARE')
  })
})

describe('Tooltip floating primitive', () => {
  it('renders a portaled tooltip with placement classes', () => {
    render(
      <Tooltip content="Brass hint" open>
        <button type="button">Hint</button>
      </Tooltip>,
    )
    const tooltip = screen.getByRole('tooltip')
    expect(tooltip.className.split(' ')).toEqual(expect.arrayContaining(['ui-floating', 'tooltip-panel']))
    expect(tooltip.className).toMatch(/tooltip-(top|right|bottom|left)/)
    expect(screen.getByRole('button').getAttribute('aria-describedby')).toBe(tooltip.id)
  })
})
