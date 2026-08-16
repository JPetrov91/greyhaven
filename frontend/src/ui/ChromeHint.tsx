import { useState, type ReactElement } from 'react'
import { Tooltip } from './Tooltip'

type Props = {
  label: string
  children: ReactElement
}

export function ChromeHint({ label, children }: Props) {
  const [open, setOpen] = useState(false)

  return (
    <div
      className="chrome-hint"
      onMouseEnter={() => setOpen(true)}
      onMouseLeave={() => setOpen(false)}
      onFocusCapture={() => setOpen(true)}
      onBlurCapture={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget as Node | null)) {
          setOpen(false)
        }
      }}
    >
      <Tooltip content={label} open={open} density="compact" placement="bottom">
        {children}
      </Tooltip>
    </div>
  )
}
