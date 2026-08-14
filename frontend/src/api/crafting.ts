import { apiRequest } from './client'
import type {
  CraftingJobResponse,
  ProfessionResponse,
  RecipeResponse,
  SalvageResponse,
} from './types'

export function fetchProfessions(): Promise<ProfessionResponse[]> {
  return apiRequest<ProfessionResponse[]>('/api/v1/crafting/professions')
}

export function fetchRecipes(): Promise<RecipeResponse[]> {
  return apiRequest<RecipeResponse[]>('/api/v1/crafting/recipes')
}

export async function fetchCurrentCraftingJob(): Promise<CraftingJobResponse | null> {
  const body = await apiRequest<CraftingJobResponse | undefined>('/api/v1/crafting/jobs/current')
  return body ?? null
}

export function startCraftingJob(recipeCode: string): Promise<CraftingJobResponse> {
  return apiRequest<CraftingJobResponse>('/api/v1/crafting/jobs', {
    method: 'POST',
    body: JSON.stringify({ recipeCode }),
  })
}

export function claimCraftingJob(jobId: string): Promise<CraftingJobResponse> {
  return apiRequest<CraftingJobResponse>(`/api/v1/crafting/jobs/${jobId}/claim`, {
    method: 'POST',
  })
}

export function salvageItem(itemId: string): Promise<SalvageResponse> {
  return apiRequest<SalvageResponse>(`/api/v1/items/${itemId}/salvage`, {
    method: 'POST',
  })
}
