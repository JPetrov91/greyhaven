import { ProgressBar, type ProgressBarProps } from './ProgressBar'

type Props = Omit<ProgressBarProps, 'tone'>

export function DurabilityBar({ density = 'hairline', ...props }: Props) {
  return <ProgressBar {...props} tone="durability" density={density} />
}
