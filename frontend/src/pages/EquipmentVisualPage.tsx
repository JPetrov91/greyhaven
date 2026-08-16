import { DevVisualShell } from '../dev/DevVisualShell'
import { EquipmentVisualWorkspace } from '../dev/EquipmentVisualWorkspace'
import { MainShellChat } from '../dev/MainShellVisualViews'

export function EquipmentVisualPage() {
  return (
    <DevVisualShell activeNav="equipment" testId="equipment-visual" label="Equipment workspace">
      <EquipmentVisualWorkspace />
      <MainShellChat />
    </DevVisualShell>
  )
}
