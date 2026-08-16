import type { AriaAttributes } from 'react'
import { classNames } from './classNames'

export type ControlStateProps = {
  error?: boolean
}

export function controlClassName(
  ...parts: Array<string | false | null | undefined>
): string {
  return classNames('ui-control', ...parts)
}

export function controlInvalid(error: boolean | undefined, ariaInvalid: AriaAttributes['aria-invalid']) {
  if (ariaInvalid !== undefined) {
    return ariaInvalid
  }
  return error || undefined
}
