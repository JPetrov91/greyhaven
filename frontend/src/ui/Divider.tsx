import type { HTMLAttributes } from 'react'
import { classNames } from './classNames'

export type DividerVariant = 'line' | 'ornament-diamond' | 'ornament-bar' | 'ornament-bronze'

type Props = HTMLAttributes<HTMLHRElement> & {
  variant?: DividerVariant
}

export function Divider({ variant = 'line', className, ...rest }: Props) {
  return (
    <hr
      className={classNames('ui-divider', variant !== 'line' && `ui-divider-${variant}`, className)}
      aria-hidden="true"
      {...rest}
    />
  )
}
