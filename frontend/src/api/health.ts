type HealthPayload = {
  status?: string
}

export async function fetchServerHealth(): Promise<boolean> {
  try {
    const response = await fetch('/actuator/health', { credentials: 'omit' })
    if (!response.ok) {
      return false
    }
    const payload = (await response.json()) as HealthPayload
    return payload.status === 'UP'
  } catch {
    return false
  }
}
