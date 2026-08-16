import type { ReactNode } from 'react'
import { MainShellActivity, MainShellLeftNav, MainShellTopBar } from './MainShellVisualViews'

type Props = {
  activeNav: 'home' | 'equipment' | 'inventory' | 'world'
  testId: string
  label: string
  layout?: 'shell' | 'combat'
  children: ReactNode
}

export function DevVisualShell({ activeNav, testId, label, layout = 'shell', children }: Props) {
  return (
    <section className="main-shell-visual surface-page" aria-label={label} data-testid={testId}>
      <MainShellTopBar />
      {layout === 'combat' ? (
        <div className="ms-combat-body">{children}</div>
      ) : (
        <div className="ms-body">
          <MainShellLeftNav activeId={activeNav} />
          <div className="ms-workspace">{children}</div>
          <MainShellActivity />
        </div>
      )}
    </section>
  )
}
