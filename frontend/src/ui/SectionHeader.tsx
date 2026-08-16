import type { HTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'
import { Ornament } from './Ornament'

type Props = HTMLAttributes<HTMLDivElement> & {
  as?: 'h2' | 'h3'
  actions?: ReactNode
  /** Tiny pip beside the label. Opt-in — never a section default. */
  accent?: boolean
}

export function SectionHeader({
  as: Heading = 'h3',
  actions,
  accent = false,
  className,
  children,
  ...rest
}: Props) {
  return (
    <div className={classNames('ui-section-header', className)} {...rest}>
      {children ? (
        <span className="ui-section-heading-row">
          {accent ? <Ornament name="accent" /> : null}
          <Heading className="type-section-heading">{children}</Heading>
        </span>
      ) : (
        <span />
      )}
      {actions}
    </div>
  )
}
