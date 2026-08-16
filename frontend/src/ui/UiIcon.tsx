import type { HTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'
import type { IconSize, IconState } from './iconography'

type Props = Omit<HTMLAttributes<HTMLSpanElement>, 'children'> & {
  size?: IconSize
  state?: IconState
  /** Painted webp / img — size only, no stroke treatment. */
  art?: boolean
  children: ReactNode
}

export function UiIcon({
  size = 'md',
  state = 'default',
  art = false,
  className,
  children,
  ...rest
}: Props) {
  return (
    <span
      className={classNames(
        'ui-icon',
        `ui-icon-${size}`,
        art && 'ui-icon-art',
        state !== 'default' && `ui-icon-${state}`,
        className,
      )}
      aria-hidden="true"
      {...rest}
    >
      {children}
    </span>
  )
}
