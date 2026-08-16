import { describe, expect, it } from 'vitest'
import { chooseFloatingPlacement, floatingCoords, FLOATING_GAP } from './floatingPlacement'

function rect(left: number, top: number, width: number, height: number): DOMRect {
  return {
    x: left,
    y: top,
    left,
    top,
    width,
    height,
    right: left + width,
    bottom: top + height,
    toJSON() {
      return {}
    },
  }
}

const viewport = { width: 1000, height: 800 }
const panel = { width: 200, height: 80 }

describe('chooseFloatingPlacement', () => {
  it('keeps the preferred side when it fits', () => {
    expect(chooseFloatingPlacement('right', rect(100, 100, 40, 20), panel, viewport)).toBe('right')
    expect(chooseFloatingPlacement('top', rect(400, 400, 40, 20), panel, viewport)).toBe('top')
  })

  it('flips when the preferred side overflows', () => {
    expect(chooseFloatingPlacement('right', rect(850, 100, 40, 20), panel, viewport)).toBe('left')
    expect(chooseFloatingPlacement('bottom', rect(100, 760, 40, 20), panel, viewport)).toBe('top')
  })
})

describe('floatingCoords', () => {
  it('offsets by the floating gap and clamps to the viewport', () => {
    const trigger = rect(100, 120, 40, 24)
    expect(floatingCoords('right', trigger, panel, viewport)).toEqual({
      top: 120,
      left: 140 + FLOATING_GAP,
    })

    const tight = floatingCoords('left', rect(10, 10, 20, 20), { width: 400, height: 400 }, { width: 300, height: 300 })
    expect(tight.left).toBe(8)
    expect(tight.top).toBe(8)
  })
})
