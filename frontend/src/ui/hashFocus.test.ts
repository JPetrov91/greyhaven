// @vitest-environment jsdom

import { afterEach, describe, expect, it } from 'vitest'
import { focusSection } from './hashFocus'

afterEach(() => {
  document.body.innerHTML = ''
})

describe('focusSection', () => {
  it('scrolls to the section and focuses its heading', () => {
    document.body.innerHTML = '<section id="inventory"><h2>Inventory</h2></section>'
    const heading = document.querySelector('h2') as HTMLHeadingElement
    heading.focus = () => {
      heading.dataset.focused = 'true'
    }
    const section = document.getElementById('inventory') as HTMLElement
    section.scrollIntoView = () => {
      section.dataset.scrolled = 'true'
    }

    focusSection('inventory')

    expect(section.dataset.scrolled).toBe('true')
    expect(heading.tabIndex).toBe(-1)
    expect(heading.dataset.focused).toBe('true')
  })
})
