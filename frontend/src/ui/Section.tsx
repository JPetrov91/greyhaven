import type { HTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'
import { Divider, type DividerVariant } from './Divider'
import { SectionHeader } from './SectionHeader'

type Props = Omit<HTMLAttributes<HTMLElement>, 'title'> & {
  as?: 'section' | 'div'
  title?: ReactNode
  actions?: ReactNode
  /** Default line when a header is present. Ornament variants are opt-in only. */
  divider?: boolean | DividerVariant
  /** Tiny heading pip. Opt-in — never automatic. */
  accent?: boolean
}

export function Section({
  as: Tag = 'section',
  title,
  actions,
  divider = true,
  accent = false,
  className,
  children,
  ...rest
}: Props) {
  const hasHeader = Boolean(title || actions)
  const dividerVariant: DividerVariant | null =
    divider === false ? null : divider === true ? (hasHeader ? 'line' : null) : divider

  return (
    <Tag className={classNames('ui-section', className)} {...rest}>
      {hasHeader ? (
        <SectionHeader actions={actions} accent={accent}>
          {title}
        </SectionHeader>
      ) : null}
      {dividerVariant ? <Divider variant={dividerVariant} /> : null}
      <div className="ui-section-body">{children}</div>
    </Tag>
  )
}
