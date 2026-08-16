export type FloatingPlacement = 'top' | 'right' | 'bottom' | 'left'

export type RectSize = {
  width: number
  height: number
}

export type ViewportSize = {
  width: number
  height: number
}

export const FLOATING_GAP = 12
export const FLOATING_VIEWPORT_PAD = 8

const OPPOSITE: Record<FloatingPlacement, FloatingPlacement> = {
  top: 'bottom',
  bottom: 'top',
  left: 'right',
  right: 'left',
}

const ALL: FloatingPlacement[] = ['right', 'left', 'top', 'bottom']

function spaceFor(trigger: DOMRect, viewport: ViewportSize): Record<FloatingPlacement, number> {
  return {
    right: viewport.width - trigger.right,
    left: trigger.left,
    bottom: viewport.height - trigger.bottom,
    top: trigger.top,
  }
}

function needFor(panel: RectSize): Record<FloatingPlacement, number> {
  return {
    right: panel.width + FLOATING_GAP,
    left: panel.width + FLOATING_GAP,
    bottom: panel.height + FLOATING_GAP,
    top: panel.height + FLOATING_GAP,
  }
}

export function chooseFloatingPlacement(
  preferred: FloatingPlacement,
  trigger: DOMRect,
  panel: RectSize,
  viewport: ViewportSize,
): FloatingPlacement {
  const space = spaceFor(trigger, viewport)
  const need = needFor(panel)
  const order: FloatingPlacement[] = [
    preferred,
    OPPOSITE[preferred],
    ...ALL.filter((placement) => placement !== preferred && placement !== OPPOSITE[preferred]),
  ]

  for (const placement of order) {
    if (space[placement] >= need[placement]) {
      return placement
    }
  }

  return ALL.reduce((best, next) => (space[next] > space[best] ? next : best))
}

export function floatingCoords(
  placement: FloatingPlacement,
  trigger: DOMRect,
  panel: RectSize,
  viewport: ViewportSize,
): { top: number; left: number } {
  let top = trigger.top
  let left = trigger.left

  if (placement === 'right') {
    left = trigger.right + FLOATING_GAP
    top = trigger.top
  } else if (placement === 'left') {
    left = trigger.left - panel.width - FLOATING_GAP
    top = trigger.top
  } else if (placement === 'top') {
    left = trigger.left
    top = trigger.top - panel.height - FLOATING_GAP
  } else {
    left = trigger.left
    top = trigger.bottom + FLOATING_GAP
  }

  const maxLeft = Math.max(FLOATING_VIEWPORT_PAD, viewport.width - panel.width - FLOATING_VIEWPORT_PAD)
  const maxTop = Math.max(FLOATING_VIEWPORT_PAD, viewport.height - panel.height - FLOATING_VIEWPORT_PAD)
  left = Math.min(Math.max(left, FLOATING_VIEWPORT_PAD), maxLeft)
  top = Math.min(Math.max(top, FLOATING_VIEWPORT_PAD), maxTop)

  return { top, left }
}
