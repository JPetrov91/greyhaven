import type { HTMLAttributes } from 'react'
import { classNames } from './classNames'

type Tone = 'neutral' | 'accent' | 'danger'

type Props = Omit<HTMLAttributes<HTMLSpanElement>, 'children'> & {
  count: number
  max?: number
  tone?: Tone
}

export function CounterBadge({ count, max = 99, tone = 'danger', className, ...rest }: Props) {
  const overflow = count > max
  return (
    <span className={classNames('counter-badge', `counter-${tone}`, className)} {...rest}>
      {overflow ? `${max}+` : String(count)}
    </span>
  )
}
