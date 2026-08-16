import type { HTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'

type Props = HTMLAttributes<HTMLDivElement> & {
  actions?: ReactNode
}

export function PanelHeader({ actions, className, children, ...rest }: Props) {
  return (
    <div className={classNames('panel-header', className)} {...rest}>
      {children ? <h2 className="type-panel-heading">{children}</h2> : <span />}
      {actions}
    </div>
  )
}
