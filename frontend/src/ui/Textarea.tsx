import type { TextareaHTMLAttributes } from 'react'
import { controlClassName, controlInvalid, type ControlStateProps } from './controlState'

type Props = TextareaHTMLAttributes<HTMLTextAreaElement> & ControlStateProps

export function Textarea({ error = false, className, rows = 4, ...rest }: Props) {
  return (
    <textarea
      {...rest}
      rows={rows}
      aria-invalid={controlInvalid(error, rest['aria-invalid'])}
      className={controlClassName('ui-textarea', error && 'ui-control-error', className)}
    />
  )
}
