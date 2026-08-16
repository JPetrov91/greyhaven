import type { InputHTMLAttributes } from 'react'
import { controlClassName, controlInvalid, type ControlStateProps } from './controlState'

type Props = InputHTMLAttributes<HTMLInputElement> & ControlStateProps

export function TextInput({ error = false, className, type = 'text', ...rest }: Props) {
  return (
    <input
      {...rest}
      type={type}
      aria-invalid={controlInvalid(error, rest['aria-invalid'])}
      className={controlClassName('ui-input', error && 'ui-control-error', className)}
    />
  )
}
