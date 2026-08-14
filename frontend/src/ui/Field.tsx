import type { LabelHTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'

type Props = LabelHTMLAttributes<HTMLLabelElement> & {
  label: ReactNode
}

export function Field({ label, className, children, ...rest }: Props) {
  return (
    <label className={classNames('field', className)} {...rest}>
      {label}
      {children}
    </label>
  )
}
