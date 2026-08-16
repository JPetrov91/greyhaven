import type { ButtonHTMLAttributes } from 'react'
import { classNames } from './classNames'

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger'

export type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant
  loading?: boolean
}

export function Button({
  variant = 'primary',
  className,
  type = 'button',
  loading = false,
  disabled,
  ...rest
}: ButtonProps) {
  return (
    <button
      {...rest}
      type={type}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      className={classNames('btn', `btn-${variant}`, loading && 'btn-loading', className)}
    />
  )
}
