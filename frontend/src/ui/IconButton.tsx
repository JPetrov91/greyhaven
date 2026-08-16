import { classNames } from './classNames'
import { Button, type ButtonProps } from './Button'

type Props = Omit<ButtonProps, 'aria-label'> & {
  label: string
}

export function IconButton({ label, className, variant = 'secondary', ...rest }: Props) {
  return (
    <Button
      {...rest}
      variant={variant}
      aria-label={label}
      className={classNames('btn-icon', className)}
    />
  )
}
