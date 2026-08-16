import { CombatVisualWorkspace } from '../dev/CombatVisualWorkspace'
import { DevVisualShell } from '../dev/DevVisualShell'

export function CombatVisualPage() {
  return (
    <DevVisualShell activeNav="home" testId="combat-visual" label="Combat workspace" layout="combat">
      <CombatVisualWorkspace />
    </DevVisualShell>
  )
}
