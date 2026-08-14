import type { HTMLAttributes } from 'react'
import { classNames } from './classNames'

type Tone = 'neutral' | 'accent' | 'danger' | 'warning'

type Props = HTMLAttributes<HTMLSpanElement> & {
  tone?: Tone
}

export function Badge({ tone = 'neutral', className, ...rest }: Props) {
  return <span className={classNames('badge', `badge-${tone}`, className)} {...rest} />
}
