import type { HTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'

type Tone = 'safe' | 'danger' | 'neutral' | 'upgrade' | 'downgrade' | 'mixed'

type Props = HTMLAttributes<HTMLSpanElement> & {
  tone?: Tone
  icon?: ReactNode
  meta?: ReactNode
}

export function StatusBadge({ tone = 'neutral', className, icon, meta, children, ...rest }: Props) {
  const effect = Boolean(icon || meta)
  return (
    <span className={classNames('status-badge', `status-${tone}`, effect && 'status-badge-effect', className)} {...rest}>
      {icon ? (
        <span className="status-badge-icon" aria-hidden="true">
          {icon}
        </span>
      ) : null}
      <span className="status-badge-label">{children}</span>
      {meta ? <span className="status-badge-meta">{meta}</span> : null}
    </span>
  )
}
