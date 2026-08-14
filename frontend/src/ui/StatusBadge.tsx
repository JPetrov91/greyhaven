import type { HTMLAttributes } from 'react'
import { classNames } from './classNames'

type Tone = 'safe' | 'danger' | 'neutral' | 'upgrade' | 'downgrade' | 'mixed'

type Props = HTMLAttributes<HTMLSpanElement> & {
  tone?: Tone
}

export function StatusBadge({ tone = 'neutral', className, ...rest }: Props) {
  return <span className={classNames('status-badge', `status-${tone}`, className)} {...rest} />
}
