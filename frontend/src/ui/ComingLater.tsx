import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { classNames } from './classNames'

export const COMING_LATER_LABEL = 'Coming later'

export function ComingLaterButton({
  className,
  children,
  ...rest
}: ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      type="button"
      disabled
      aria-disabled="true"
      title={COMING_LATER_LABEL}
      className={classNames('coming-later', className)}
      {...rest}
    >
      {children}
      <span className="visually-hidden">{COMING_LATER_LABEL}</span>
    </button>
  )
}

export function ComingLaterChip({ children, testId }: { children: ReactNode; testId?: string }) {
  return (
    <span className="currency-chip currency-chip-locked" title={COMING_LATER_LABEL} data-testid={testId} aria-disabled="true">
      {children}
      <span className="coming-later-hint">{COMING_LATER_LABEL}</span>
    </span>
  )
}
