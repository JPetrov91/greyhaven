import { describe, expect, it } from 'vitest'
import {
  DEV_UI_COMBAT_PATH,
  DEV_UI_EQUIPMENT_PATH,
  DEV_UI_INVENTORY_PATH,
  DEV_UI_LOCATIONS_PATH,
  DEV_UI_MAIN_SHELL_PATH,
  DEV_UI_PATH,
  devUiNavPath,
  isDevUiEnabled,
  isDevUiMainShellPath,
  isDevUiPath,
  isDevUiShowcasePath,
  isDevUiVisualShellPath,
} from './devUi'

describe('dev UI route', () => {
  it('is gated to development and uses /dev/ui', () => {
    expect(DEV_UI_PATH).toBe('/dev/ui')
    expect(DEV_UI_MAIN_SHELL_PATH).toBe('/dev/ui/main-shell')
    expect(DEV_UI_EQUIPMENT_PATH).toBe('/dev/ui/equipment')
    expect(DEV_UI_INVENTORY_PATH).toBe('/dev/ui/inventory')
    expect(DEV_UI_LOCATIONS_PATH).toBe('/dev/ui/locations')
    expect(DEV_UI_COMBAT_PATH).toBe('/dev/ui/combat')
    expect(isDevUiPath('/dev/ui')).toBe(true)
    expect(isDevUiPath('/dev/ui/main-shell')).toBe(true)
    expect(isDevUiPath('/dev/ui/equipment')).toBe(true)
    expect(isDevUiShowcasePath('/dev/ui')).toBe(true)
    expect(isDevUiShowcasePath('/dev/ui/main-shell')).toBe(false)
    expect(isDevUiMainShellPath('/dev/ui/main-shell')).toBe(true)
    expect(isDevUiVisualShellPath('/dev/ui/equipment')).toBe(true)
    expect(isDevUiVisualShellPath('/dev/ui/inventory')).toBe(true)
    expect(isDevUiVisualShellPath('/dev/ui/locations')).toBe(true)
    expect(isDevUiVisualShellPath('/dev/ui/combat')).toBe(true)
    expect(isDevUiVisualShellPath('/dev/ui')).toBe(false)
    expect(devUiNavPath('equipment')).toBe(DEV_UI_EQUIPMENT_PATH)
    expect(devUiNavPath('inventory')).toBe(DEV_UI_INVENTORY_PATH)
    expect(devUiNavPath('world')).toBe(DEV_UI_LOCATIONS_PATH)
    expect(devUiNavPath('home')).toBe(DEV_UI_MAIN_SHELL_PATH)
    expect(isDevUiPath('/game')).toBe(false)
    expect(isDevUiEnabled()).toBe(import.meta.env.DEV)
  })
})

