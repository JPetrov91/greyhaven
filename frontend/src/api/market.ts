import { apiRequest } from './client'
import type {
  ItemRarity,
  ItemType,
  MarketBuyOrderResponse,
  MarketBuyOrdersResponse,
  MarketListingResponse,
  MarketListingsResponse,
  MerchantListResponse,
  MerchantPurchaseResponse,
  MerchantResponse,
  MerchantSaleResponse,
  WeaponFamily,
} from './types'

export type MarketListingSort = 'CREATED_AT' | 'PRICE'
export type SortDirection = 'ASC' | 'DESC'

export type MarketListingQuery = {
  itemType?: ItemType | ''
  rarity?: ItemRarity | ''
  weaponFamily?: WeaponFamily | ''
  minLevel?: number | ''
  maxLevel?: number | ''
  minPrice?: number | ''
  maxPrice?: number | ''
  sort?: MarketListingSort
  direction?: SortDirection
  page?: number
  size?: number
  mine?: boolean
}

function listingQuery(query: MarketListingQuery): string {
  const params = new URLSearchParams()
  if (query.itemType) {
    params.set('itemType', query.itemType)
  }
  if (query.rarity) {
    params.set('rarity', query.rarity)
  }
  if (query.weaponFamily) {
    params.set('weaponFamily', query.weaponFamily)
  }
  if (query.minLevel !== undefined && query.minLevel !== '') {
    params.set('minLevel', String(query.minLevel))
  }
  if (query.maxLevel !== undefined && query.maxLevel !== '') {
    params.set('maxLevel', String(query.maxLevel))
  }
  if (query.minPrice !== undefined && query.minPrice !== '') {
    params.set('minPrice', String(query.minPrice))
  }
  if (query.maxPrice !== undefined && query.maxPrice !== '') {
    params.set('maxPrice', String(query.maxPrice))
  }
  if (query.sort) {
    params.set('sort', query.sort)
  }
  if (query.direction) {
    params.set('direction', query.direction)
  }
  if (query.page !== undefined) {
    params.set('page', String(query.page))
  }
  if (query.size !== undefined) {
    params.set('size', String(query.size))
  }
  if (query.mine) {
    params.set('mine', 'true')
  }
  const encoded = params.toString()
  return encoded ? `?${encoded}` : ''
}

export function fetchMarketListings(query: MarketListingQuery | ItemType | '' = {}): Promise<MarketListingsResponse> {
  const normalized: MarketListingQuery = typeof query === 'string' ? { itemType: query } : query
  return apiRequest<MarketListingsResponse>(`/api/v1/market/listings${listingQuery(normalized)}`)
}

export function fetchOwnMarketListings(): Promise<MarketListingsResponse> {
  return apiRequest<MarketListingsResponse>('/api/v1/market/listings?mine=true')
}

export function fetchMarketListingHistory(page = 0, size = 20): Promise<MarketListingsResponse> {
  return apiRequest<MarketListingsResponse>(`/api/v1/market/listings/history?page=${page}&size=${size}`)
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

export function fetchBuyOrders(mine = false, page = 0, size = 20): Promise<MarketBuyOrdersResponse> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  if (mine) {
    params.set('mine', 'true')
  }
  return apiRequest<MarketBuyOrdersResponse>(`/api/v1/market/buy-orders?${params.toString()}`)
}

export function createBuyOrder(
  itemDefinitionId: string,
  quantity: number,
  maxUnitPrice: number,
): Promise<MarketBuyOrderResponse> {
  return apiRequest<MarketBuyOrderResponse>('/api/v1/market/buy-orders', {
    method: 'POST',
    body: JSON.stringify({ itemDefinitionId, quantity, maxUnitPrice }),
  })
}

export function fulfillBuyOrder(
  orderId: string,
  itemInstanceId: string,
  quantity: number,
): Promise<MarketBuyOrderResponse> {
  return apiRequest<MarketBuyOrderResponse>(`/api/v1/market/buy-orders/${orderId}/fulfill`, {
    method: 'POST',
    body: JSON.stringify({ itemInstanceId, quantity }),
  })
}

export function cancelBuyOrder(orderId: string): Promise<MarketBuyOrderResponse> {
  return apiRequest<MarketBuyOrderResponse>(`/api/v1/market/buy-orders/${orderId}`, {
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
