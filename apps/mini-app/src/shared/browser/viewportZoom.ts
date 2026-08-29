const zoomKeyboardKeys = new Set(["+", "-", "=", "0", "_", "Add", "Subtract"])
const nonPassiveListenerOptions: AddEventListenerOptions = { passive: false }

function preventGestureZoom(event: Event): void {
  event.preventDefault()
}

function preventMultiTouchZoom(event: TouchEvent): void {
  if (event.touches.length > 1) {
    event.preventDefault()
  }
}

function preventModifierWheelZoom(event: WheelEvent): void {
  if (event.ctrlKey || event.metaKey) {
    event.preventDefault()
  }
}

function preventKeyboardZoom(event: KeyboardEvent): void {
  if ((event.ctrlKey || event.metaKey) && zoomKeyboardKeys.has(event.key)) {
    event.preventDefault()
  }
}

export function installViewportZoomLock(): () => void {
  document.addEventListener("gesturestart", preventGestureZoom, nonPassiveListenerOptions)
  document.addEventListener("gesturechange", preventGestureZoom, nonPassiveListenerOptions)
  document.addEventListener("gestureend", preventGestureZoom, nonPassiveListenerOptions)
  document.addEventListener("touchmove", preventMultiTouchZoom, nonPassiveListenerOptions)
  window.addEventListener("wheel", preventModifierWheelZoom, nonPassiveListenerOptions)
  window.addEventListener("keydown", preventKeyboardZoom)

  return () => {
    document.removeEventListener("gesturestart", preventGestureZoom)
    document.removeEventListener("gesturechange", preventGestureZoom)
    document.removeEventListener("gestureend", preventGestureZoom)
    document.removeEventListener("touchmove", preventMultiTouchZoom)
    window.removeEventListener("wheel", preventModifierWheelZoom)
    window.removeEventListener("keydown", preventKeyboardZoom)
  }
}
