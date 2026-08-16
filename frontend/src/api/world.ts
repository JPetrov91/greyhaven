import { apiRequest } from './client'
import type {
  DestinationsResponse,
  LocationResponse,
  NearbyCharactersResponse,
  QuestBoardResponse,
} from './types'

export function fetchCurrentLocation(): Promise<LocationResponse> {
  return apiRequest<LocationResponse>('/api/v1/world/location')
}

export function fetchDestinations(): Promise<DestinationsResponse> {
  return apiRequest<DestinationsResponse>('/api/v1/world/destinations')
}

export function fetchNearbyCharacters(): Promise<NearbyCharactersResponse> {
  return apiRequest<NearbyCharactersResponse>('/api/v1/world/nearby')
}

export function fetchQuestBoard(locationCode: string): Promise<QuestBoardResponse> {
  return apiRequest<QuestBoardResponse>(`/api/v1/world/locations/${locationCode}/quest-board`)
}

export function moveToLocation(destinationLocationId: string): Promise<LocationResponse> {
  return apiRequest<LocationResponse>('/api/v1/world/move', {
    method: 'POST',
    body: JSON.stringify({ destinationLocationId }),
  })
}
