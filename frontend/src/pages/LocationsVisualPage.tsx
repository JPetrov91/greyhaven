import { DevVisualShell } from '../dev/DevVisualShell'
import { LocationsVisualWorkspace } from '../dev/LocationsVisualWorkspace'
import { MainShellChat } from '../dev/MainShellVisualViews'

export function LocationsVisualPage() {
  return (
    <DevVisualShell activeNav="world" testId="locations-visual" label="Locations workspace">
      <LocationsVisualWorkspace />
      <MainShellChat />
    </DevVisualShell>
  )
}
