import { apiRequest } from './client'
import type { InventoryResponse } from './types'

export function fetchInventory(): Promise<InventoryResponse> {
  return apiRequest<InventoryResponse>('/api/v1/inventory')
}

export function equipItem(itemId: string): Promise<InventoryResponse> {
  return apiRequest<InventoryResponse>(`/api/v1/inventory/${itemId}/equip`, {
    method: 'POST',
  })
}

export function unequipItem(itemId: string): Promise<InventoryResponse> {
  return apiRequest<InventoryResponse>(`/api/v1/inventory/${itemId}/unequip`, {
    method: 'POST',
  })
}

export function useItem(itemId: string): Promise<InventoryResponse> {
  return apiRequest<InventoryResponse>(`/api/v1/inventory/${itemId}/use`, {
    method: 'POST',
  })
}
