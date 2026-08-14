import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'

type Toast = {
  id: number
  message: string
}

type ToastApi = {
  notify: (message: string) => void
}

const ToastContext = createContext<ToastApi>({ notify: () => undefined })

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const notify = useCallback((message: string) => {
    const id = Date.now() + Math.random()
    setToasts((current) => [...current, { id, message }])
    window.setTimeout(() => {
      setToasts((current) => current.filter((toast) => toast.id !== id))
    }, 2800)
  }, [])

  const value = useMemo(() => ({ notify }), [notify])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-region" aria-live="polite" aria-relevant="additions" data-testid="toast-region">
        {toasts.map((toast) => (
          <p key={toast.id} className="toast">
            {toast.message}
          </p>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast(): ToastApi {
  return useContext(ToastContext)
}
