import { avatarImageUrl } from '../character/avatars'
import { classNames } from './classNames'

export const DEFAULT_AVATAR_URL = '/character/default-avatar.webp'

type Props = {
  className?: string
  avatarCode?: string | null
}

export function CharacterPortrait({ className, avatarCode }: Props) {
  return (
    <div className={classNames('portrait', className)} aria-hidden="true">
      <img src={avatarImageUrl(avatarCode)} alt="" />
    </div>
  )
}
