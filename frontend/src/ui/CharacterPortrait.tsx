import { classNames } from './classNames'

export const DEFAULT_AVATAR_URL = '/character/default-avatar.webp'

type Props = {
  className?: string
}

export function CharacterPortrait({ className }: Props) {
  return (
    <div className={classNames('portrait', className)} aria-hidden="true">
      <img src={DEFAULT_AVATAR_URL} alt="" />
    </div>
  )
}
