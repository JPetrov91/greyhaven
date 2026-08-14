export type CombatStatusIconName =
  | 'BLEED'
  | 'POISON'
  | 'STUN'
  | 'ARMOR_BREAK'
  | 'OFF_BALANCE'
  | 'GUARDED'
  | 'STUN_IMMUNITY'

export function CombatStatusIcon({ type }: { type: string }) {
  const name = type as CombatStatusIconName
  return (
    <svg className="combat-status-icon" viewBox="0 0 32 32" aria-hidden="true" focusable="false">
      {name === 'BLEED' ? (
        <>
          <path d="M16 4c4 6 8 10 8 15a8 8 0 1 1-16 0c0-5 4-9 8-15Z" fill="#7a1f1a" />
          <path d="M16 8c3 5 6 8 6 12a6 6 0 1 1-12 0c0-4 3-7 6-12Z" fill="#c45c4a" />
        </>
      ) : null}
      {name === 'POISON' ? (
        <>
          <path d="M12 6h8l2 8H10l2-8Z" fill="#3a5a28" />
          <circle cx="16" cy="22" r="7" fill="#5d8a3a" />
          <circle cx="14" cy="20" r="1.4" fill="#d7ecc4" />
        </>
      ) : null}
      {name === 'STUN' ? (
        <>
          <path d="M14 5 8 17h7l-2 10 11-14h-7l3-8Z" fill="#d4b05a" />
          <path d="M15 8 11 16h5l-1.4 6 7-9h-5Z" fill="#f3e2a0" />
        </>
      ) : null}
      {name === 'ARMOR_BREAK' ? (
        <>
          <path d="M8 8h16v4c0 7-4 12-8 14-4-2-8-7-8-14V8Z" fill="#5a4638" />
          <path d="M12 10 22 24M20 10 10 24" stroke="#c45c4a" strokeWidth="2.2" />
        </>
      ) : null}
      {name === 'OFF_BALANCE' ? (
        <>
          <circle cx="16" cy="16" r="11" fill="#3a342c" />
          <path d="M16 7v9l7 4" fill="none" stroke="#d4b05a" strokeWidth="2.2" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'GUARDED' ? (
        <>
          <path d="M8 7h16v5c0 8-5 13-8 15-3-2-8-7-8-15V7Z" fill="#3d5a48" />
          <path d="M12 16l3 3 6-7" fill="none" stroke="#9fd4a8" strokeWidth="2.2" strokeLinecap="round" />
        </>
      ) : null}
      {name === 'STUN_IMMUNITY' ? (
        <>
          <circle cx="16" cy="16" r="11" fill="#2c3a48" />
          <path d="M10 16h12M16 10v12" stroke="#9ec0e8" strokeWidth="2.2" />
        </>
      ) : null}
      {!isKnownStatus(name) ? (
        <rect x="5" y="5" width="22" height="22" rx="4" fill="#3a342c" />
      ) : null}
    </svg>
  )
}

function isKnownStatus(type: string): type is CombatStatusIconName {
  return (
    type === 'BLEED'
    || type === 'POISON'
    || type === 'STUN'
    || type === 'ARMOR_BREAK'
    || type === 'OFF_BALANCE'
    || type === 'GUARDED'
    || type === 'STUN_IMMUNITY'
  )
}
