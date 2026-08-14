import { useEffect, useId, useRef, type ReactNode } from 'react'
import { Button } from './Button'

type Props = {
  open: boolean
  title: string
  children: ReactNode
  confirmLabel: string
  cancelLabel?: string
  confirmTestId?: string
  onConfirm: () => void
  onCancel: () => void
  danger?: boolean
}

export function Dialog({
  open,
  title,
  children,
  confirmLabel,
  cancelLabel = 'Cancel',
  confirmTestId,
  onConfirm,
  onCancel,
  danger = false,
}: Props) {
  const ref = useRef<HTMLDialogElement>(null)
  const titleId = useId()

  useEffect(() => {
    const node = ref.current
    if (!node) {
      return
    }
    if (open && !node.open) {
      if (typeof node.showModal === 'function') {
        node.showModal()
      } else {
        node.setAttribute('open', '')
      }
    } else if (!open && node.open) {
      if (typeof node.close === 'function') {
        node.close()
      } else {
        node.removeAttribute('open')
      }
    }
  }, [open])

  return (
    <dialog
      ref={ref}
      className="ui-dialog"
      aria-labelledby={titleId}
      onClose={onCancel}
      onCancel={(event) => {
        event.preventDefault()
        onCancel()
      }}
    >
      <h3 id={titleId}>{title}</h3>
      <div className="ui-dialog-body">{children}</div>
      <div className="ui-dialog-actions">
        <Button type="button" variant="ghost" onClick={onCancel}>
          {cancelLabel}
        </Button>
        <Button
          type="button"
          variant={danger ? 'danger' : 'primary'}
          data-testid={confirmTestId}
          onClick={onConfirm}
        >
          {confirmLabel}
        </Button>
      </div>
    </dialog>
  )
}
