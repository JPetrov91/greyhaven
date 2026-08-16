import type { InputHTMLAttributes } from 'react'
import { classNames } from './classNames'
import { controlClassName, controlInvalid, type ControlStateProps } from './controlState'

type Props = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & ControlStateProps

export function SearchInput({ error = false, className, disabled, ...rest }: Props) {
  return (
    <span className={classNames('ui-search', disabled && 'ui-search-disabled', error && 'ui-search-error')}>
      <span className="ui-search-icon" aria-hidden="true" />
      <input
        {...rest}
        type="search"
        disabled={disabled}
        aria-invalid={controlInvalid(error, rest['aria-invalid'])}
        className={controlClassName('ui-input', 'ui-search-input', error && 'ui-control-error', className)}
      />
    </span>
  )
}
