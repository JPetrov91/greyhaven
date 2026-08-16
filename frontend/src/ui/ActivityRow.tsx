import { classNames } from './classNames'
import { GenericRow, type GenericRowProps } from './GenericRow'

export type ActivityRowVariant = 'normal' | 'system' | 'reward' | 'warning' | 'market' | 'pvp' | 'completed'

export type ActivityRowProps = GenericRowProps & {
  variant?: ActivityRowVariant
}

export function ActivityRow({ variant = 'normal', as = 'li', className, ...rest }: ActivityRowProps) {
  return (
    <GenericRow
      as={as}
      className={classNames('ui-activity-row', `ui-activity-${variant}`, className)}
      {...rest}
    />
  )
}
