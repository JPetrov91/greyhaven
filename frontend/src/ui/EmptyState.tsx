type Props = {
  children: string
  testId?: string
}

export function EmptyState({ children, testId }: Props) {
  return (
    <p className="muted ui-state" data-testid={testId}>
      {children}
    </p>
  )
}
