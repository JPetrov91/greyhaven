import type { HTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'
import type { IconWellSize } from './iconography'

type Props = Omit<HTMLAttributes<HTMLSpanElement>, 'children'> & {
  size?: IconWellSize
  active?: boolean
  children: ReactNode
}

export function IconWell({ size = 'md', active = false, className, children, ...rest }: Props) {
  return (
    <span
      className={classNames(
        'ui-icon-well',
        size === 'lg' && 'ui-icon-well-lg',
        active && 'ui-icon-well-active',
        className,
      )}
      aria-hidden="true"
      {...rest}
    >
      {children}
    </span>
  )
}
