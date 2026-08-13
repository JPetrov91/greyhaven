import { apiRequest } from './client'
import type { ChatMessageResponse } from './types'

export function fetchChatMessages(): Promise<ChatMessageResponse[]> {
  return apiRequest<ChatMessageResponse[]>('/api/v1/chat/messages')
}

export function postChatMessage(body: string): Promise<ChatMessageResponse> {
  return apiRequest<ChatMessageResponse>('/api/v1/chat/messages', {
    method: 'POST',
    body: JSON.stringify({ body }),
  })
}

export function chatStreamUrl(afterMessageId?: string | null): string {
  if (!afterMessageId) {
    return '/api/v1/chat/stream'
  }
  return `/api/v1/chat/stream?after=${encodeURIComponent(afterMessageId)}`
}
