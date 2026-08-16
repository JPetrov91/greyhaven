import { classNames } from './classNames'
import { GenericRow, type GenericRowProps } from './GenericRow'

export type CompactDataRowProps = GenericRowProps

export function CompactDataRow({ as = 'li', interactive = true, className, ...rest }: CompactDataRowProps) {
  return <GenericRow as={as} interactive={interactive} className={classNames('ui-compact-row', className)} {...rest} />
}
