import { useEffect, useId, useRef, useState, type KeyboardEvent, type ReactNode } from 'react'
import { classNames } from './classNames'
import { controlClassName, controlInvalid, type ControlStateProps } from './controlState'
import { Floating } from './Floating'

export type DropdownOption = {
  value: string
  label: ReactNode
  disabled?: boolean
}

type Props = ControlStateProps & {
  value: string
  onChange: (value: string) => void
  options: DropdownOption[]
  placeholder?: string
  disabled?: boolean
  id?: string
  className?: string
  testId?: string
  'aria-label'?: string
  'aria-labelledby'?: string
}

export function Dropdown({
  value,
  onChange,
  options,
  placeholder = 'Select',
  disabled = false,
  error = false,
  id,
  className,
  testId,
  'aria-label': ariaLabel,
  'aria-labelledby': ariaLabelledBy,
}: Props) {
  const listId = useId()
  const buttonId = id ?? `${listId}-trigger`
  const anchorRef = useRef<HTMLDivElement>(null)
  const [open, setOpen] = useState(false)
  const [active, setActive] = useState(value)
  const selected = options.find((option) => option.value === value)

  useEffect(() => {
    if (open) {
      setActive(value || options.find((option) => !option.disabled)?.value || '')
    }
  }, [open, options, value])

  useEffect(() => {
    if (!open) {
      return
    }
    function onPointerDown(event: PointerEvent) {
      if (!anchorRef.current?.contains(event.target as Node)) {
        const panel = document.getElementById(listId)
        if (panel && panel.contains(event.target as Node)) {
          return
        }
        setOpen(false)
      }
    }
    document.addEventListener('pointerdown', onPointerDown)
    return () => document.removeEventListener('pointerdown', onPointerDown)
  }, [listId, open])

  const enabled = options.filter((option) => !option.disabled)

  function move(delta: number) {
    if (enabled.length === 0) {
      return
    }
    const index = Math.max(0, enabled.findIndex((option) => option.value === active))
    const next = enabled[(index + delta + enabled.length) % enabled.length]
    setActive(next.value)
  }

  function choose(next: string) {
    onChange(next)
    setOpen(false)
  }

  function onKeyDown(event: KeyboardEvent<HTMLButtonElement>) {
    if (disabled) {
      return
    }
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault()
      if (!open) {
        setOpen(true)
        return
      }
      move(event.key === 'ArrowDown' ? 1 : -1)
    } else if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      if (!open) {
        setOpen(true)
        return
      }
      const current = options.find((option) => option.value === active && !option.disabled)
      if (current) {
        choose(current.value)
      }
    } else if (event.key === 'Escape' && open) {
      event.preventDefault()
      setOpen(false)
    }
  }

  return (
    <div ref={anchorRef} className={classNames('ui-dropdown', className)}>
      <button
        type="button"
        id={buttonId}
        disabled={disabled}
        aria-invalid={controlInvalid(error, undefined)}
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-controls={listId}
        aria-label={ariaLabel}
        aria-labelledby={ariaLabelledBy}
        data-testid={testId}
        className={controlClassName('ui-select', 'ui-dropdown-trigger', error && 'ui-control-error')}
        onClick={() => {
          if (!disabled) {
            setOpen((current) => !current)
          }
        }}
        onKeyDown={onKeyDown}
      >
        <span className={classNames(!selected && 'ui-dropdown-placeholder')}>{selected?.label ?? placeholder}</span>
      </button>
      <Floating
        open={open}
        anchorRef={anchorRef}
        placement="bottom"
        layer="dropdown"
        role="listbox"
        id={listId}
        className="ui-dropdown-panel"
        matchAnchor
      >
        {options.map((option) => (
          <div
            key={option.value}
            role="option"
            aria-selected={option.value === value}
            aria-disabled={option.disabled || undefined}
            className={classNames(
              'ui-dropdown-option',
              option.value === value && 'ui-dropdown-option-selected',
              option.value === active && 'ui-dropdown-option-active',
              option.disabled && 'ui-dropdown-option-disabled',
            )}
            onMouseEnter={() => {
              if (!option.disabled) {
                setActive(option.value)
              }
            }}
            onMouseDown={(event) => {
              event.preventDefault()
              if (!option.disabled) {
                choose(option.value)
              }
            }}
            onClick={() => {
              if (!option.disabled) {
                choose(option.value)
              }
            }}
          >
            {option.label}
          </div>
        ))}
      </Floating>
    </div>
  )
}
