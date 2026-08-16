import { useLayoutEffect, useRef, useState, type CSSProperties, type ReactNode, type RefObject } from 'react'
import { createPortal } from 'react-dom'
import { classNames } from './classNames'
import {
  chooseFloatingPlacement,
  floatingCoords,
  type FloatingPlacement,
} from './floatingPlacement'

type Layer = 'tooltip' | 'dropdown'

type Props = {
  open: boolean
  anchorRef: RefObject<HTMLElement | null>
  children: ReactNode
  placement?: FloatingPlacement
  width?: string
  matchAnchor?: boolean
  role?: string
  id?: string
  className?: string
  layer?: Layer
}

export function Floating({
  open,
  anchorRef,
  children,
  placement = 'right',
  width,
  matchAnchor = false,
  role,
  id,
  className,
  layer = 'tooltip',
}: Props) {
  const panelRef = useRef<HTMLDivElement>(null)
  const [resolved, setResolved] = useState<FloatingPlacement>(placement)
  const [coords, setCoords] = useState({ top: 0, left: 0 })

  useLayoutEffect(() => {
    if (!open) {
      setResolved(placement)
      return
    }

    function update() {
      const anchor = anchorRef.current
      const panel = panelRef.current
      if (!anchor || !panel) {
        return
      }
      const trigger = anchor.getBoundingClientRect()
      const size = {
        width: matchAnchor ? trigger.width : panel.offsetWidth,
        height: panel.offsetHeight,
      }
      const viewport = { width: window.innerWidth, height: window.innerHeight }
      const next = chooseFloatingPlacement(placement, trigger, size, viewport)
      setResolved(next)
      setCoords(floatingCoords(next, trigger, size, viewport))
    }

    update()
    window.addEventListener('resize', update)
    window.addEventListener('scroll', update, true)
    return () => {
      window.removeEventListener('resize', update)
      window.removeEventListener('scroll', update, true)
    }
  }, [anchorRef, matchAnchor, open, placement, children])

  if (!open || typeof document === 'undefined') {
    return null
  }

  const style: CSSProperties = {
    top: coords.top,
    left: coords.left,
    width: matchAnchor && anchorRef.current ? `${anchorRef.current.getBoundingClientRect().width}px` : width,
  }

  return createPortal(
    <div
      ref={panelRef}
      id={id}
      role={role}
      style={style}
      className={classNames(
        'ui-floating',
        `ui-floating-${resolved}`,
        role === 'tooltip' && `tooltip-${resolved}`,
        layer === 'dropdown' && 'ui-floating-dropdown',
        className,
      )}
    >
      {children}
    </div>,
    document.body,
  )
}

export type { FloatingPlacement }
