import type { HTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'
import { PanelHeader } from './PanelHeader'

export type PanelVariant = 'page' | 'base' | 'raised' | 'inset' | 'interactive' | 'selected' | 'floating'

type Props = Omit<HTMLAttributes<HTMLElement>, 'title'> & {
  as?: 'section' | 'aside' | 'div'
  title?: ReactNode
  actions?: ReactNode
  variant?: PanelVariant
}

export function Panel({
  as: Tag = 'section',
  title,
  actions,
  variant = 'base',
  className,
  children,
  ...rest
}: Props) {
  return (
    <Tag className={classNames('panel', `surface-${variant}`, className)} {...rest}>
      {title || actions ? <PanelHeader actions={actions}>{title}</PanelHeader> : null}
      <div className="panel-body">{children}</div>
    </Tag>
  )
}
