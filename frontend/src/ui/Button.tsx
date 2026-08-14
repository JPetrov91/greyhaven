import type { ButtonHTMLAttributes } from 'react'
import { classNames } from './classNames'

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger'

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: Variant
}

export function Button({ variant = 'primary', className, type = 'button', ...rest }: Props) {
  return <button type={type} className={classNames('btn', `btn-${variant}`, className)} {...rest} />
}
