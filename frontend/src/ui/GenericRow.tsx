import type { HTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'

export type RowTone = 'default' | 'important' | 'secondary'

export type GenericRowProps = Omit<HTMLAttributes<HTMLElement>, 'title'> & {
  as?: 'div' | 'li' | 'article'
  icon?: ReactNode
  primary: ReactNode
  secondary?: ReactNode
  metadata?: ReactNode
  action?: ReactNode
  selected?: boolean
  interactive?: boolean
  tone?: RowTone
  testId?: string
}

export function GenericRow({
  as: Tag = 'div',
  icon,
  primary,
  secondary,
  metadata,
  action,
  selected = false,
  interactive = false,
  tone = 'default',
  testId,
  className,
  ...rest
}: GenericRowProps) {
  const hasTrail = metadata != null || action != null

  return (
    <Tag
      className={classNames(
        'ui-row',
        icon != null && 'ui-row-has-icon',
        hasTrail && 'ui-row-has-trail',
        selected && 'ui-row-selected',
        (interactive || selected) && 'ui-row-interactive',
        tone !== 'default' && `ui-row-tone-${tone}`,
        className,
      )}
      data-testid={testId}
      aria-selected={selected || undefined}
      {...rest}
    >
      {icon != null ? <span className="ui-row-icon">{icon}</span> : null}
      <span className="ui-row-body">
        <span className="ui-row-primary">{primary}</span>
        {secondary != null ? <span className="ui-row-secondary">{secondary}</span> : null}
      </span>
      {hasTrail ? (
        <span className="ui-row-trail">
          {metadata != null ? <span className="ui-row-meta">{metadata}</span> : null}
          {action != null ? <span className="ui-row-action">{action}</span> : null}
        </span>
      ) : null}
    </Tag>
  )
}
