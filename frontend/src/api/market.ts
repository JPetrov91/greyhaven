import { apiRequest } from './client'
import type { ItemType, MarketListingResponse, MarketListingsResponse } from './types'

export function fetchMarketListings(itemType?: ItemType | ''): Promise<MarketListingsResponse> {
  const params = new URLSearchParams()
  if (itemType) {
    params.set('itemType', itemType)
  }
  const query = params.toString()
  return apiRequest<MarketListingsResponse>(`/api/v1/market/listings${query ? `?${query}` : ''}`)
}

export function fetchOwnMarketListings(): Promise<MarketListingsResponse> {
  return apiRequest<MarketListingsResponse>('/api/v1/market/listings?mine=true')
}

export function createMarketListing(
  itemInstanceId: string,
  quantity: number,
  price: number,
): Promise<MarketListingResponse> {
  return apiRequest<MarketListingResponse>('/api/v1/market/listings', {
    method: 'POST',
    body: JSON.stringify({ itemInstanceId, quantity, price }),
  })
}

export function buyMarketListing(listingId: string): Promise<MarketListingResponse> {
  return apiRequest<MarketListingResponse>(`/api/v1/market/listings/${listingId}/buy`, {
    method: 'POST',
  })
}

export function cancelMarketListing(listingId: string): Promise<MarketListingResponse> {
  return apiRequest<MarketListingResponse>(`/api/v1/market/listings/${listingId}`, {
    method: 'DELETE',
  })
}
