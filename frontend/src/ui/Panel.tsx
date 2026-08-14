import type { HTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'

type Props = HTMLAttributes<HTMLElement> & {
  as?: 'section' | 'aside' | 'div'
  title?: ReactNode
  actions?: ReactNode
}

export function Panel({ as: Tag = 'section', title, actions, className, children, ...rest }: Props) {
  return (
    <Tag className={classNames('panel', className)} {...rest}>
      {title || actions ? (
        <div className="panel-header">
          {title ? <h2>{title}</h2> : <span />}
          {actions}
        </div>
      ) : null}
      {children}
    </Tag>
  )
}
