import { classNames } from './classNames'

type Props = {
  value: number
  max?: number
  label: string
  testId?: string
  className?: string
  tone?: 'xp' | 'health' | 'stamina'
}

export function ProgressBar({ value, max = 100, label, testId, className, tone = 'xp' }: Props) {
  return (
    <progress
      className={classNames('progress-bar', `progress-${tone}`, className)}
      max={max}
      value={value}
      aria-label={label}
      data-testid={testId}
    />
  )
}
