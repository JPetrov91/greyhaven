import { DevVisualShell } from '../dev/DevVisualShell'
import { InventoryVisualWorkspace } from '../dev/InventoryVisualWorkspace'
import { MainShellChat } from '../dev/MainShellVisualViews'

export function InventoryVisualPage() {
  return (
    <DevVisualShell activeNav="inventory" testId="inventory-visual" label="Inventory workspace">
      <InventoryVisualWorkspace />
      <MainShellChat />
    </DevVisualShell>
  )
}
