// @vitest-environment jsdom

import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import { Divider } from './Divider'
import { Panel } from './Panel'
import { PanelHeader } from './PanelHeader'
import { Section } from './Section'
import { SectionHeader } from './SectionHeader'

afterEach(() => {
  cleanup()
})

describe('Panel variants', () => {
  it('defaults to the base surface and keeps the panel layout class', () => {
    render(<Panel data-testid="panel">Body</Panel>)
    const panel = screen.getByTestId('panel')
    expect(panel.className.split(' ')).toEqual(expect.arrayContaining(['panel', 'surface-base']))
    expect(panel.className).not.toMatch(/surface-raised|surface-inset|surface-floating/)
  })

  it('maps each variant to the matching surface class', () => {
    const { rerender } = render(
      <Panel variant="raised" data-testid="panel">
        Body
      </Panel>,
    )
    expect(screen.getByTestId('panel').className).toContain('surface-raised')

    rerender(
      <Panel variant="inset" data-testid="panel">
        Body
      </Panel>,
    )
    expect(screen.getByTestId('panel').className).toContain('surface-inset')

    rerender(
      <Panel variant="floating" data-testid="panel">
        Body
      </Panel>,
    )
    expect(screen.getByTestId('panel').className).toContain('surface-floating')

    rerender(
      <Panel variant="page" data-testid="panel">
        Body
      </Panel>,
    )
    expect(screen.getByTestId('panel').className).toContain('surface-page')

    rerender(
      <Panel variant="interactive" data-testid="panel">
        Body
      </Panel>,
    )
    expect(screen.getByTestId('panel').className).toContain('surface-interactive')

    rerender(
      <Panel variant="selected" data-testid="panel">
        Body
      </Panel>,
    )
    expect(screen.getByTestId('panel').className).toContain('surface-selected')
  })
})

describe('Section inside Panel', () => {
  it('uses heading, default line divider, and content without a nested panel frame', () => {
    render(
      <Panel title="Inventory" data-testid="panel">
        <Section title="Equipped" data-testid="section">
          Sword
        </Section>
      </Panel>,
    )

    const panel = screen.getByTestId('panel')
    expect(panel.querySelector('.panel-header')?.textContent).toContain('Inventory')
    expect(panel.querySelector('.type-panel-heading')?.textContent).toBe('Inventory')
    expect(panel.querySelector('.panel-body')).not.toBeNull()

    const section = screen.getByTestId('section')
    expect(section.className.split(' ')).toContain('ui-section')
    expect(section.className).not.toMatch(/\bpanel\b/)
    expect(section.className).not.toMatch(/surface-/)
    expect(section.querySelector('.type-section-heading')?.textContent).toBe('Equipped')
    expect(section.querySelector('.ui-divider')).not.toBeNull()
    expect(section.querySelector('.ui-divider-ornament-diamond')).toBeNull()
    expect(section.querySelector('.ui-divider-ornament-bar')).toBeNull()
    expect(section.querySelector('.ui-section-body')?.textContent).toBe('Sword')
  })

  it('omits the automatic divider when asked, and never ornaments by default', () => {
    render(
      <Section title="Bag" divider={false} data-testid="section">
        Potion
      </Section>,
    )
    const section = screen.getByTestId('section')
    expect(section.querySelector('.ui-divider')).toBeNull()
  })

  it('applies an ornamental divider only when requested', () => {
    render(
      <Section title="Chapter" divider="ornament-diamond" data-testid="section">
        Text
      </Section>,
    )
    expect(screen.getByTestId('section').querySelector('.ui-divider-ornament-diamond')).not.toBeNull()
  })
})

describe('PanelHeader', () => {
  it('renders a panel nameplate with optional actions', () => {
    render(
      <PanelHeader actions={<span>2/3</span>}>Active Expeditions</PanelHeader>,
    )
    expect(screen.getByRole('heading', { level: 2 }).textContent).toBe('Active Expeditions')
    expect(screen.getByRole('heading', { level: 2 }).className).toContain('type-panel-heading')
    expect(screen.getByText('2/3')).toBeTruthy()
  })
})

describe('SectionHeader and Divider', () => {
  it('renders a section heading with optional actions', () => {
    render(
      <SectionHeader actions={<button type="button">Edit</button>}>Stats</SectionHeader>,
    )
    expect(screen.getByRole('heading', { level: 3 }).textContent).toBe('Stats')
    expect(screen.getByRole('button', { name: 'Edit' })).toBeTruthy()
    expect(document.querySelector('.ui-ornament-accent')).toBeNull()
  })

  it('adds a heading pip only when accent is requested', () => {
    render(<Section title="Recent events" accent data-testid="section">Log</Section>)
    expect(screen.getByTestId('section').querySelector('.ui-ornament-accent')).not.toBeNull()
  })

  it('defaults Divider to a plain line', () => {
    const { container } = render(<Divider />)
    const rule = container.querySelector('hr')
    expect(rule?.className.split(' ')).toEqual(['ui-divider'])
    expect(rule?.getAttribute('aria-hidden')).toBe('true')
  })
})
