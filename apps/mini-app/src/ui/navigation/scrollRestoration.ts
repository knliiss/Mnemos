export function restoreScrollTop(element: HTMLElement, top: number) {
  const target = Math.max(0, top)
  let active = true
  let frame: number | null = null
  let timeout: number | null = null
  let observer: ResizeObserver | null = null
  let attempts = 0

  const cleanup = () => {
    if (!active) {
      return
    }

    active = false

    if (frame !== null) {
      window.cancelAnimationFrame(frame)
      frame = null
    }

    if (timeout !== null) {
      window.clearTimeout(timeout)
      timeout = null
    }

    observer?.disconnect()
    observer = null
  }

  const apply = () => {
    if (!active) {
      return true
    }

    const maxTop = Math.max(0, element.scrollHeight - element.clientHeight)
    element.scrollTo({
      top: Math.min(target, maxTop),
      behavior: "auto",
    })

    return Math.abs(element.scrollTop - target) <= 1
  }

  const tick = () => {
    if (!active) {
      return
    }

    attempts += 1
    if (apply()) {
      cleanup()
      return
    }

    if (attempts >= 90) {
      frame = null
      return
    }

    frame = window.requestAnimationFrame(tick)
  }

  frame = window.requestAnimationFrame(tick)

  if (typeof ResizeObserver !== "undefined") {
    observer = new ResizeObserver(() => {
      if (apply()) {
        cleanup()
      }
    })

    const content = element.querySelector<HTMLElement>(".route-content-frame")
    observer.observe(content ?? element)
  }

  timeout = window.setTimeout(cleanup, 5_000)

  return cleanup
}
