import { classNames } from './classNames'
import { GenericRow, type GenericRowProps } from './GenericRow'
import type { ActivityRowVariant } from './ActivityRow'

export type NotificationRowProps = GenericRowProps & {
  variant?: ActivityRowVariant
  unread?: boolean
}

export function NotificationRow({
  variant = 'normal',
  unread = false,
  as = 'li',
  interactive = true,
  className,
  ...rest
}: NotificationRowProps) {
  return (
    <GenericRow
      as={as}
      interactive={interactive}
      className={classNames(
        'ui-notification-row',
        `ui-activity-${variant}`,
        unread && 'ui-notification-unread',
        className,
      )}
      {...rest}
    />
  )
}
