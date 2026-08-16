import type { HTMLAttributes } from 'react'
import { classNames } from './classNames'
import type { OrnamentCorner, OrnamentName } from './iconography'

type Props = HTMLAttributes<HTMLSpanElement> & {
  name: OrnamentName
  /** Placement flip for `corner` only. Default top-left. */
  corner?: OrnamentCorner
}

export function Ornament({ name, corner = 'tl', className, ...rest }: Props) {
  return (
    <span
      className={classNames(
        'ui-ornament',
        `ui-ornament-${name}`,
        name === 'corner' && corner !== 'tl' && `ui-ornament-${corner}`,
        className,
      )}
      aria-hidden="true"
      {...rest}
    />
  )
}
