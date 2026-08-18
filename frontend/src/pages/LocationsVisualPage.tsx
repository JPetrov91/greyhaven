import { DevVisualShell } from '../dev/DevVisualShell'
import { LocationsVisualWorkspace } from '../dev/LocationsVisualWorkspace'

export function LocationsVisualPage() {
  return (
    <DevVisualShell activeNav="world" testId="locations-visual" label="Locations workspace">
      <LocationsVisualWorkspace />
    </DevVisualShell>
  )
}
