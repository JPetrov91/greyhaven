import { classNames } from './classNames'
import { ProgressBar, type ProgressBarProps } from './ProgressBar'

type Props = Omit<ProgressBarProps, 'tone'>

export function XPBar({ className, ...props }: Props) {
  return <ProgressBar {...props} tone="xp" className={classNames('xp-bar', className)} />
}
