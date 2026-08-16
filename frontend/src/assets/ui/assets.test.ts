import { existsSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const root = dirname(fileURLToPath(import.meta.url))

describe('UI asset pack', () => {
  it('keeps the reusable material and ornament files', () => {
    for (const relative of [
      'materials/ui-material-page.webp',
      'materials/ui-material-dark.webp',
      'materials/ui-material-panel.webp',
      'materials/ui-material-raised.webp',
      'materials/ui-material-inset.webp',
      'materials/ui-bronze-edge.png',
      'materials/ui-panel-rim.png',
      'materials/ui-divider-bronze.png',
      'ornaments/section-divider.svg',
      'ornaments/corner-accent.svg',
      'ornaments/small-diamond.svg',
      'ornaments/tiny-pip.svg',
      'ornaments/hairline-fade.svg',
      'ornaments/frame-corners.png',
      'ornaments/frame-corners.svg',
    ]) {
      expect(existsSync(join(root, relative))).toBe(true)
    }
  })
})
