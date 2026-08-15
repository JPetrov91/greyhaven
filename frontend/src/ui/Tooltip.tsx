import { cloneElement, isValidElement, useId, useLayoutEffect, useRef, useState, type ReactNode } from 'react'
import { classNames } from './classNames'

type Placement = 'right' | 'left' | 'top' | 'bottom'

type TriggerProps = {
  'aria-describedby'?: string
  'aria-expanded'?: boolean
}

type Props = {
  content: ReactNode
  children: ReactNode
  open: boolean
  pinned?: boolean
}

export function Tooltip({ content, children, open, pinned = false }: Props) {
  const tooltipId = useId()
  const panelRef = useRef<HTMLDivElement>(null)
  const [placement, setPlacement] = useState<Placement>('right')

  useLayoutEffect(() => {
    if (!open) {
      setPlacement('right')
      return
    }
    const panel = panelRef.current
    const trigger = panel?.previousElementSibling as HTMLElement | null
    if (!panel || !trigger) {
      return
    }
    const t = trigger.getBoundingClientRect()
    const p = panel.getBoundingClientRect()
    const gap = 12
    const right = window.innerWidth - t.right
    const left = t.left
    const bottom = window.innerHeight - t.bottom
    const top = t.top
    if (right >= p.width + gap) {
      setPlacement('right')
    } else if (left >= p.width + gap) {
      setPlacement('left')
    } else if (top >= p.height + gap) {
      setPlacement('top')
    } else if (bottom >= p.height + gap) {
      setPlacement('bottom')
    } else {
      setPlacement(right >= left ? 'right' : 'left')
    }
  }, [open])

  const trigger = isValidElement<TriggerProps>(children)
    ? cloneElement(children, {
        'aria-describedby': open ? tooltipId : undefined,
        ...(pinned ? { 'aria-expanded': true } : {}),
      })
    : children

  return (
    <div className={classNames('tooltip-anchor', open && 'tooltip-open')}>
      {trigger}
      {open ? (
        <div
          ref={panelRef}
          id={tooltipId}
          className={classNames('tooltip-panel', `tooltip-${placement}`)}
          role="tooltip"
        >
          {content}
        </div>
      ) : null}
    </div>
  )
}
