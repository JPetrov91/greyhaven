import { DevVisualShell } from '../dev/DevVisualShell'
import {
  MainShellCharacterOverview,
  MainShellChat,
  MainShellEquipmentPreview,
  MainShellEvents,
  MainShellExpeditions,
  MainShellGuild,
  MainShellLocationHero,
  MainShellObjectives,
} from '../dev/MainShellVisualViews'

export function MainShellVisualPage() {
  return (
    <DevVisualShell activeNav="home" testId="main-shell-visual" label="Game workspace">
      <MainShellLocationHero />
      <div className="ms-row">
        <MainShellCharacterOverview />
        <MainShellEquipmentPreview />
        <MainShellExpeditions />
      </div>
      <div className="ms-row">
        <MainShellObjectives />
        <MainShellEvents />
        <MainShellGuild />
      </div>
      <MainShellChat />
    </DevVisualShell>
  )
}
