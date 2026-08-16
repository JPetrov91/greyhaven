import type { CSSProperties } from 'react'
import { classNames } from './classNames'

export type ProgressTone = 'xp' | 'health' | 'stamina' | 'durability'
export type ProgressDensity = 'default' | 'vital' | 'compact' | 'hairline'
export type ProgressValuePlacement = 'below' | 'overlay' | 'beside'

export type ProgressBarProps = {
  value: number
  max?: number
  label: string
  testId?: string
  className?: string
  tone?: ProgressTone
  density?: ProgressDensity
  showValue?: boolean
  valueText?: string
  valuePlacement?: ProgressValuePlacement
  segments?: number
}

export function ProgressBar({
  value,
  max = 100,
  label,
  testId,
  className,
  tone = 'xp',
  density = 'default',
  showValue = false,
  valueText,
  valuePlacement = 'below',
  segments = 0,
}: ProgressBarProps) {
  const safeMax = max > 0 ? max : 1
  const clamped = Math.min(safeMax, Math.max(0, value))
  const tickCount = segments > 1 ? Math.floor(segments) : 0
  const framed = showValue || tickCount > 0 || valuePlacement !== 'below'
  const valueNode = showValue ? (
    <span className="ui-meter-value" aria-hidden="true">
      {valueText ?? `${clamped} / ${safeMax}`}
    </span>
  ) : null

  const bar = (
    <progress
      className={classNames(
        'progress-bar',
        `progress-${tone}`,
        density !== 'default' && `progress-${density}`,
        className,
      )}
      max={safeMax}
      value={clamped}
      aria-label={label}
      data-testid={testId}
    />
  )

  if (!framed) {
    return bar
  }

  const frameStyle = tickCount > 0 ? ({ '--meter-segments': String(tickCount) } as CSSProperties) : undefined
  const ticks = tickCount > 0 ? <span className="progress-segments" aria-hidden="true" /> : null

  if (valuePlacement === 'overlay') {
    return (
      <div className="ui-meter ui-meter-overlay" style={frameStyle}>
        {bar}
        {ticks}
        {valueNode}
      </div>
    )
  }

  if (valuePlacement === 'beside') {
    return (
      <div className="ui-meter ui-meter-beside" style={frameStyle}>
        <div className="ui-meter-track">
          {bar}
          {ticks}
        </div>
        {valueNode}
      </div>
    )
  }

  return (
    <div className="ui-meter" style={frameStyle}>
      <div className="ui-meter-track">
        {bar}
        {ticks}
      </div>
      {valueNode}
    </div>
  )
}
