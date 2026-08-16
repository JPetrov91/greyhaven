import { ProgressBar, type ProgressBarProps } from './ProgressBar'

type Props = Omit<ProgressBarProps, 'tone'>

export function HealthBar({ density = 'vital', ...props }: Props) {
  return <ProgressBar {...props} tone="health" density={density} />
}
