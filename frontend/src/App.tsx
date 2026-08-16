import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './components/AppShell'
import { GameLayout } from './components/GameLayout'
import { GuestOnly } from './auth/GuestOnly'
import { RequireAuth } from './auth/RequireAuth'
import {
  DEV_UI_COMBAT_PATH,
  DEV_UI_EQUIPMENT_PATH,
  DEV_UI_INVENTORY_PATH,
  DEV_UI_LOCATIONS_PATH,
  DEV_UI_MAIN_SHELL_PATH,
  DEV_UI_PATH,
  isDevUiEnabled,
} from './dev/devUi'
import { CombatVisualPage } from './pages/CombatVisualPage'
import { CreateCharacterPage } from './pages/CreateCharacterPage'
import { EquipmentVisualPage } from './pages/EquipmentVisualPage'
import { InventoryVisualPage } from './pages/InventoryVisualPage'
import { LocationsVisualPage } from './pages/LocationsVisualPage'
import { LoginPage } from './pages/LoginPage'
import { MainShellVisualPage } from './pages/MainShellVisualPage'
import { RegisterPage } from './pages/RegisterPage'
import { UiShowcasePage } from './pages/UiShowcasePage'

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
          <Route path="/characters" element={<CreateCharacterPage />} />
          <Route path="/create-character" element={<Navigate to="/characters" replace />} />
          <Route path="/game" element={<GameLayout />} />
        </Route>

        {isDevUiEnabled() ? (
          <>
            <Route path={DEV_UI_PATH} element={<UiShowcasePage />} />
            <Route path={DEV_UI_MAIN_SHELL_PATH} element={<MainShellVisualPage />} />
            <Route path={DEV_UI_EQUIPMENT_PATH} element={<EquipmentVisualPage />} />
            <Route path={DEV_UI_INVENTORY_PATH} element={<InventoryVisualPage />} />
            <Route path={DEV_UI_LOCATIONS_PATH} element={<LocationsVisualPage />} />
            <Route path={DEV_UI_COMBAT_PATH} element={<CombatVisualPage />} />
          </>
        ) : null}

        <Route path="*" element={<Navigate to="/game" replace />} />
      </Route>
    </Routes>
  )
}
