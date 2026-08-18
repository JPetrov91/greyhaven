import { useEffect, useState } from 'react'
import './chapter1Prologue.css'

const BEATS = [
  {
    kicker: 'Chapter 1',
    title: 'The Open Gates',
    lines: [
      'Dusk was already settling on Greyhaven.',
      'Last light hung on the old teeth of the wall when a single traveler reached the gate. The cloak was road-dust. The pack was worn. Nothing about them asked to be remembered.',
    ],
  },
  {
    lines: [
      'The watch saw them only when they stopped at the grate.',
      '“Late,” one said. “Name.”',
      '“{name}.”',
      'A finger moved down a list that did not have them. The man shrugged. The watch was thin. Lists were a habit, like opening the gates at dawn.',
    ],
  },
  {
    lines: [
      '“Business.”',
      '“Work.”',
      'They looked at each other a moment. Then the watch stepped aside.',
      '“Welcome to Greyhaven. Try not to find trouble before the work finds you.”',
    ],
  },
  {
    lines: [
      '{name} went under the arch. The gate came down hard behind.',
      'Ahead, past narrow streets and tavern light, the Square was already waiting.',
    ],
  },
] as const

type Props = {
  name: string
  onFinished: () => void
}

function fillName(text: string, name: string): string {
  return text.replaceAll('{name}', name)
}

export function Chapter1Prologue({ name, onFinished }: Props) {
  const [beat, setBeat] = useState(0)
  const last = beat >= BEATS.length - 1
  const current = BEATS[beat]

  function advance() {
    if (last) {
      onFinished()
      return
    }
    setBeat((index) => index + 1)
  }

  useEffect(() => {
    function onKey(event: KeyboardEvent) {
      if (event.key === ' ' || event.key === 'Enter') {
        event.preventDefault()
        advance()
      }
      if (event.key === 'Escape') {
        onFinished()
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  })

  return (
    <section className="chapter1-prologue" data-testid="chapter1-prologue" aria-label="Chapter 1">
      <button type="button" className="chapter1-skip" data-testid="chapter1-skip" onClick={onFinished}>
        Skip
      </button>
      <div className="chapter1-body" data-testid="chapter1-beat" onClick={advance}>
        {'kicker' in current && current.kicker ? (
          <p className="chapter1-kicker" data-testid="chapter1-kicker">
            {current.kicker}
          </p>
        ) : null}
        {'title' in current && current.title ? (
          <h1 className="chapter1-title" data-testid="chapter1-title">
            {current.title}
          </h1>
        ) : null}
        {current.lines.map((line) => (
          <p key={line}>{fillName(line, name)}</p>
        ))}
      </div>
      <button
        type="button"
        className="chapter1-continue"
        data-testid={last ? 'chapter1-enter' : 'chapter1-continue'}
        onClick={advance}
      >
        {last ? 'Enter the Square' : 'Continue'}
      </button>
    </section>
  )
}
