import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './components/AppShell'
import { GameLayout } from './components/GameLayout'
import { GuestOnly } from './auth/GuestOnly'
import { RequireAuth } from './auth/RequireAuth'
import { CreateCharacterPage } from './pages/CreateCharacterPage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<Navigate to="/game" replace />} />

        <Route element={<GuestOnly />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Route>

        <Route element={<RequireAuth />}>
          <Route path="/create-character" element={<CreateCharacterPage />} />
          <Route path="/game" element={<GameLayout />} />
        </Route>

        <Route path="*" element={<Navigate to="/game" replace />} />
      </Route>
    </Routes>
  )
}
