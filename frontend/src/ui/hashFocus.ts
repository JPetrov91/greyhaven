export function focusSection(id: string): void {
  const el = document.getElementById(id)
  if (!el) {
    return
  }
  el.scrollIntoView({ block: 'start' })
  const heading = el.querySelector('h1, h2, h3')
  if (heading instanceof HTMLElement) {
    heading.tabIndex = -1
    heading.focus()
  }
}
