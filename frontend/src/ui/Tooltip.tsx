import { cloneElement, isValidElement, useId, useLayoutEffect, useRef, useState, type ReactNode } from 'react'
import { classNames } from './classNames'

type Placement = 'bottom-start' | 'bottom-end' | 'top-start' | 'top-end'

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
  const [placement, setPlacement] = useState<Placement>('bottom-start')

  useLayoutEffect(() => {
    if (!open) {
      setPlacement('bottom-start')
      return
    }
    const panel = panelRef.current
    if (!panel) {
      return
    }
    const rect = panel.getBoundingClientRect()
    const flipUp = rect.bottom > window.innerHeight - 8 && rect.top > rect.height
    const flipLeft = rect.right > window.innerWidth - 8
    setPlacement(`${flipUp ? 'top' : 'bottom'}-${flipLeft ? 'end' : 'start'}`)
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
          className={classNames(
            'tooltip-panel',
            placement.startsWith('top') && 'tooltip-top',
            placement.endsWith('end') && 'tooltip-end',
          )}
          role="tooltip"
        >
          {content}
        </div>
      ) : null}
    </div>
  )
}
