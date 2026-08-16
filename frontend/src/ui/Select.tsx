import type { SelectHTMLAttributes } from 'react'
import { controlClassName, controlInvalid, type ControlStateProps } from './controlState'

type Props = SelectHTMLAttributes<HTMLSelectElement> & ControlStateProps

export function Select({ error = false, className, children, ...rest }: Props) {
  return (
    <select
      {...rest}
      aria-invalid={controlInvalid(error, rest['aria-invalid'])}
      className={controlClassName('ui-select', error && 'ui-control-error', className)}
    >
      {children}
    </select>
  )
}
