/** UI chrome glyph contract. Painted /icons/* webp is content art, not this scale. */

export const ICON_GRID = 24
export const ICON_STROKE = 1.6
export const ORNAMENT_STROKE = 1

export const ICON_SIZES = ['sm', 'md', 'lg', 'xl'] as const
export type IconSize = (typeof ICON_SIZES)[number]

export const ICON_STATES = ['default', 'disabled', 'active'] as const
export type IconState = (typeof ICON_STATES)[number]

export const ICON_WELL_SIZES = ['md', 'lg'] as const
export type IconWellSize = (typeof ICON_WELL_SIZES)[number]

export const ORNAMENT_NAMES = ['divider', 'corner', 'diamond', 'accent'] as const
export type OrnamentName = (typeof ORNAMENT_NAMES)[number]

export const ORNAMENT_CORNERS = ['tl', 'tr', 'bl', 'br'] as const
export type OrnamentCorner = (typeof ORNAMENT_CORNERS)[number]
