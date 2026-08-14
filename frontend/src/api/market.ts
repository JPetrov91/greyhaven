import { apiRequest } from './client'
import type {
  ItemType,
  MarketListingResponse,
  MarketListingsResponse,
  MerchantListResponse,
  MerchantPurchaseResponse,
  MerchantResponse,
  MerchantSaleResponse,
} from './types'

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

export function fetchMerchants(): Promise<MerchantListResponse> {
  return apiRequest<MerchantListResponse>('/api/v1/market/merchants')
}

export function fetchMerchant(merchantId: string): Promise<MerchantResponse> {
  return apiRequest<MerchantResponse>(`/api/v1/market/merchants/${merchantId}`)
}

export function buyMerchantItem(
  merchantId: string,
  itemDefinitionId: string,
  quantity: number,
): Promise<MerchantPurchaseResponse> {
  return apiRequest<MerchantPurchaseResponse>(`/api/v1/market/merchants/${merchantId}/purchases`, {
    method: 'POST',
    body: JSON.stringify({ itemDefinitionId, quantity }),
  })
}

export function sellToMerchant(itemInstanceId: string, quantity: number): Promise<MerchantSaleResponse> {
  return apiRequest<MerchantSaleResponse>('/api/v1/market/merchant-sales', {
    method: 'POST',
    body: JSON.stringify({ itemInstanceId, quantity }),
  })
}
