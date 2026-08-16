import { ProgressBar, type ProgressBarProps } from './ProgressBar'

type Props = Omit<ProgressBarProps, 'tone'>

export function StaminaBar({ density = 'vital', ...props }: Props) {
  return <ProgressBar {...props} tone="stamina" density={density} />
}
