/**
 * Detects a lost HTTP session (as opposed to bad login credentials).
 * Only UNAUTHENTICATED should clear client auth state.
 */
export function isSessionExpiredUnauthorized(
  status: number,
  code: string | undefined,
): boolean {
  return status === 401 && code === 'UNAUTHENTICATED'
}

type UnauthorizedHandler = () => void

let unauthorizedHandler: UnauthorizedHandler | null = null

export function setUnauthorizedHandler(handler: UnauthorizedHandler | null): void {
  unauthorizedHandler = handler
}

export function notifySessionExpired(status: number, code: string | undefined): void {
  if (isSessionExpiredUnauthorized(status, code)) {
    unauthorizedHandler?.()
  }
}
