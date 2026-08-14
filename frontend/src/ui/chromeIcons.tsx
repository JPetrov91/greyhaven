type ChromeIconName = 'trophy' | 'mail' | 'pack' | 'friends'

export function ChromeIcon({ name }: { name: ChromeIconName }) {
  return (
    <svg className="chrome-icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      {name === 'trophy' ? (
        <path
          d="M7 5h10v3a5 5 0 0 1-4 4.9V15h3v2H8v-2h3v-2.1A5 5 0 0 1 7 8V5Zm-2 1h2v2a3 3 0 0 1-2-2.6V6Zm12 0h2v.4A3 3 0 0 1 17 8V6Z"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.6"
          strokeLinejoin="round"
        />
      ) : null}
      {name === 'mail' ? (
        <path
          d="M4 7h16v10H4Zm0 0 8 6 8-6"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.6"
          strokeLinejoin="round"
        />
      ) : null}
      {name === 'pack' ? (
        <path
          d="M8 8V6.5A4 4 0 0 1 16 6.5V8h2.5v12H5.5V8Zm2 0h4V6.8a2 2 0 0 0-4 0Z"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.6"
          strokeLinejoin="round"
        />
      ) : null}
      {name === 'friends' ? (
        <>
          <circle cx="9" cy="8" r="2.4" fill="none" stroke="currentColor" strokeWidth="1.6" />
          <path d="M4.5 18c.6-3 2.4-4.5 4.5-4.5S13 15 13.6 18" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
          <circle cx="16.2" cy="8.4" r="2" fill="none" stroke="currentColor" strokeWidth="1.6" />
          <path d="M15 13.6c1.7.2 3.2 1.4 3.8 4.4" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
        </>
      ) : null}
    </svg>
  )
}
