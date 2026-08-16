export const DEV_UI_PATH = '/dev/ui'
export const DEV_UI_MAIN_SHELL_PATH = '/dev/ui/main-shell'
export const DEV_UI_EQUIPMENT_PATH = '/dev/ui/equipment'
export const DEV_UI_INVENTORY_PATH = '/dev/ui/inventory'
export const DEV_UI_LOCATIONS_PATH = '/dev/ui/locations'
export const DEV_UI_COMBAT_PATH = '/dev/ui/combat'

export function isDevUiEnabled(): boolean {
  return import.meta.env.DEV
}

export function isDevUiPath(pathname: string): boolean {
  return pathname === DEV_UI_PATH || pathname.startsWith(`${DEV_UI_PATH}/`)
}

export function isDevUiShowcasePath(pathname: string): boolean {
  return pathname === DEV_UI_PATH
}

export function isDevUiMainShellPath(pathname: string): boolean {
  return pathname === DEV_UI_MAIN_SHELL_PATH
}

export function isDevUiVisualShellPath(pathname: string): boolean {
  return (
    pathname === DEV_UI_MAIN_SHELL_PATH ||
    pathname === DEV_UI_EQUIPMENT_PATH ||
    pathname === DEV_UI_INVENTORY_PATH ||
    pathname === DEV_UI_LOCATIONS_PATH ||
    pathname === DEV_UI_COMBAT_PATH
  )
}

export function devUiNavPath(id: string): string {
  if (id === 'equipment') {
    return DEV_UI_EQUIPMENT_PATH
  }
  if (id === 'inventory') {
    return DEV_UI_INVENTORY_PATH
  }
  if (id === 'world') {
    return DEV_UI_LOCATIONS_PATH
  }
  return DEV_UI_MAIN_SHELL_PATH
}
