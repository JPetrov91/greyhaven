import { useEffect, useRef } from 'react'
import { classNames } from './classNames'

type Tab<T extends string> = {
  id: T
  label: string
}

type Props<T extends string> = {
  tabs: Tab<T>[]
  value: T
  onChange: (id: T) => void
  label: string
  /** Filters use a toggle group; tabs are for mutually exclusive panels. */
  kind?: 'tabs' | 'filters'
  testId?: string
}

export function Tabs<T extends string>({
  tabs,
  value,
  onChange,
  label,
  kind = 'tabs',
  testId,
}: Props<T>) {
  const listRef = useRef<HTMLDivElement>(null)
  const movedByKeyboard = useRef(false)
  const filters = kind === 'filters'

  useEffect(() => {
    if (!movedByKeyboard.current) {
      return
    }
    movedByKeyboard.current = false
    const selected = filters
      ? listRef.current?.querySelector('[aria-pressed="true"]')
      : listRef.current?.querySelector('[role="tab"][aria-selected="true"]')
    if (selected instanceof HTMLElement) {
      selected.focus()
    }
  }, [value, filters])

  function move(direction: 1 | -1) {
    const index = tabs.findIndex((entry) => entry.id === value)
    const next = tabs[(index + direction + tabs.length) % tabs.length]
    if (next) {
      movedByKeyboard.current = true
      onChange(next.id)
    }
  }

  return (
    <div
      ref={listRef}
      className="tabs"
      role={filters ? 'group' : 'tablist'}
      aria-label={label}
      data-testid={testId}
    >
      {tabs.map((tab) => {
        const selected = tab.id === value
        return (
          <button
            key={tab.id}
            type="button"
            role={filters ? undefined : 'tab'}
            aria-selected={filters ? undefined : selected}
            aria-pressed={filters ? selected : undefined}
            tabIndex={filters || selected ? 0 : -1}
            className={classNames('tab', selected && 'tab-active')}
            onClick={() => onChange(tab.id)}
            onKeyDown={(event) => {
              if (event.key === 'ArrowRight') {
                event.preventDefault()
                move(1)
              } else if (event.key === 'ArrowLeft') {
                event.preventDefault()
                move(-1)
              }
            }}
          >
            {tab.label}
          </button>
        )
      })}
    </div>
  )
}
