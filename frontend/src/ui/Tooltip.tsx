import { cloneElement, isValidElement, useId, useRef, type ReactNode } from 'react'
import { classNames } from './classNames'
import { Floating, type FloatingPlacement } from './Floating'

type TriggerProps = {
  'aria-describedby'?: string
  'aria-expanded'?: boolean
}

export type TooltipDensity = 'default' | 'compact' | 'peek' | 'inspector'

type Props = {
  content: ReactNode
  children: ReactNode
  open: boolean
  pinned?: boolean
  placement?: FloatingPlacement
  density?: TooltipDensity
  width?: string
}

export function Tooltip({
  content,
  children,
  open,
  pinned = false,
  placement = 'right',
  density = 'default',
  width,
}: Props) {
  const tooltipId = useId()
  const anchorRef = useRef<HTMLDivElement>(null)

  const trigger = isValidElement<TriggerProps>(children)
    ? cloneElement(children, {
        'aria-describedby': open ? tooltipId : undefined,
        ...(pinned ? { 'aria-expanded': true } : {}),
      })
    : children

  const resolvedWidth =
    width ??
    (density === 'default'
      ? 'min(var(--floating-width), 70vw)'
      : density === 'compact'
        ? 'max-content'
        : density === 'inspector'
          ? 'min(16.5rem, 70vw)'
          : 'min(11.5rem, 36vw)')

  return (
    <div ref={anchorRef} className={classNames('tooltip-anchor', open && 'tooltip-open')}>
      {trigger}
      <Floating
        open={open}
        anchorRef={anchorRef}
        placement={placement}
        role="tooltip"
        id={tooltipId}
        width={resolvedWidth}
        className={classNames(
          'tooltip-panel',
          density === 'compact' && 'tooltip-panel-compact',
          density === 'peek' && 'tooltip-panel-peek',
          density === 'inspector' && 'tooltip-panel-inspector',
        )}
      >
        {content}
      </Floating>
    </div>
  )
}
