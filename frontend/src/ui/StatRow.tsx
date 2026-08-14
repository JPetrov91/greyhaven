import type { ReactNode } from 'react'
import { classNames } from './classNames'

type Props = {
  label: string
  value: ReactNode
  delta?: number
  testId?: string
  children?: ReactNode
  className?: string
}

export function StatRow({ label, value, delta, testId, children, className }: Props) {
  return (
    <>
      <dt className={className}>{label}</dt>
      <dd className={classNames('stat-value', className)} data-testid={testId}>
        {value}
        {delta != null && delta !== 0 ? (
          <span className={classNames('stat-delta', delta > 0 ? 'stat-delta-up' : 'stat-delta-down')}>
            {delta > 0 ? '+' : ''}
            {delta}
          </span>
        ) : null}
        {children}
      </dd>
    </>
  )
}
