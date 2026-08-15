import { createContext, useContext, useEffect, type ReactNode } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import {
  fetchMe,
  loginAccount,
  logoutAccount,
  registerAccount,
} from '../api/auth'
import type { MeResponse } from '../api/types'
import { setUnauthorizedHandler } from './sessionExpiry'

type AuthContextValue = {
  me: MeResponse | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<MeResponse>
  register: (email: string, password: string) => Promise<MeResponse>
  logout: () => Promise<void>
  refreshMe: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()

  useEffect(() => {
    setUnauthorizedHandler(() => {
      // Use null (not undefined/removeQueries) so the observed ['me'] query stays
      // settled as "guest" instead of refetching /api/v1/me in a 401 loop.
      queryClient.setQueryData(['me'], null)
      queryClient.removeQueries({ queryKey: ['character'] })
      queryClient.removeQueries({ queryKey: ['location'] })
      queryClient.removeQueries({ queryKey: ['destinations'] })
      queryClient.removeQueries({ queryKey: ['nearby-characters'] })
      queryClient.removeQueries({ queryKey: ['inventory'] })
    })
    return () => setUnauthorizedHandler(null)
  }, [queryClient])

  const meQuery = useQuery({
    queryKey: ['me'],
    queryFn: fetchMe,
    retry: false,
  })

  const me = meQuery.data ?? null

  async function login(email: string, password: string): Promise<MeResponse> {
    const response = await loginAccount(email, password)
    queryClient.setQueryData(['me'], response)
    await queryClient.invalidateQueries({ queryKey: ['character'] })
    await queryClient.invalidateQueries({ queryKey: ['location'] })
    await queryClient.invalidateQueries({ queryKey: ['destinations'] })
    await queryClient.invalidateQueries({ queryKey: ['nearby-characters'] })
    await queryClient.invalidateQueries({ queryKey: ['inventory'] })
    await queryClient.invalidateQueries({ queryKey: ['masteries'] })
    await queryClient.invalidateQueries({ queryKey: ['techniques'] })
    return response
  }

  async function register(email: string, password: string): Promise<MeResponse> {
    const response = await registerAccount(email, password)
    queryClient.setQueryData(['me'], response)
    await queryClient.invalidateQueries({ queryKey: ['character'] })
    await queryClient.invalidateQueries({ queryKey: ['location'] })
    await queryClient.invalidateQueries({ queryKey: ['destinations'] })
    await queryClient.invalidateQueries({ queryKey: ['nearby-characters'] })
    await queryClient.invalidateQueries({ queryKey: ['inventory'] })
    await queryClient.invalidateQueries({ queryKey: ['masteries'] })
    await queryClient.invalidateQueries({ queryKey: ['techniques'] })
    return response
  }

  async function logout(): Promise<void> {
    try {
      await logoutAccount()
    } finally {
      queryClient.setQueryData(['me'], null)
      queryClient.removeQueries({ queryKey: ['character'] })
      queryClient.removeQueries({ queryKey: ['location'] })
      queryClient.removeQueries({ queryKey: ['destinations'] })
      queryClient.removeQueries({ queryKey: ['nearby-characters'] })
      queryClient.removeQueries({ queryKey: ['inventory'] })
      queryClient.removeQueries({ queryKey: ['masteries'] })
      queryClient.removeQueries({ queryKey: ['techniques'] })
    }
  }

  async function refreshMe(): Promise<void> {
    // Force a network read — cached register/login payloads can still lack an active character.
    await queryClient.fetchQuery({
      queryKey: ['me'],
      queryFn: fetchMe,
      staleTime: 0,
    })
  }

  const value: AuthContextValue = {
    me,
    isLoading: meQuery.isLoading,
    isAuthenticated: me !== null,
    login,
    register,
    logout,
    refreshMe,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
