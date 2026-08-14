import { Button } from './Button'

type Props = {
  children: string
  onRetry?: () => void
  testId?: string
}

export function ErrorState({ children, onRetry, testId }: Props) {
  return (
    <div className="ui-state">
      <p className="form-error" role="alert" data-testid={testId}>
        {children}
      </p>
      {onRetry ? (
        <Button type="button" onClick={onRetry}>
          Retry
        </Button>
      ) : null}
    </div>
  )
}
