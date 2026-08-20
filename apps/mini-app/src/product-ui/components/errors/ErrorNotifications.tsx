import { AnimatePresence, motion } from "framer-motion"
import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react"
import { useTranslation } from "react-i18next"
import {
  resolveErrorPresentation,
  type BackendErrorPayload,
  type NoticePresentation,
} from "./errorPresentation"
import { ErrorMascot } from "./ErrorMascot"

type ErrorNotice = {
  id: number
  payload: BackendErrorPayload
  presentation: NoticePresentation
  persistent?: boolean
}

type ErrorNotificationApi = {
  showBackendError: (payload: BackendErrorPayload, options?: { persistent?: boolean }) => number
  dismissError: (id: number) => void
  clearErrors: () => void
}

type ProviderProps = {
  children: ReactNode
  hasTabbar: boolean
}

const ErrorNotificationContext = createContext<ErrorNotificationApi | null>(null)

export function ErrorNotificationProvider({ children, hasTabbar }: ProviderProps) {
  const [notices, setNotices] = useState<ErrorNotice[]>([])
  const nextId = useRef(1)
  const previewInitialized = useRef(false)

  const dismissError = useCallback((id: number) => {
    setNotices((current) => current.filter((notice) => notice.id !== id))
  }, [])

  const clearErrors = useCallback(() => setNotices([]), [])

  const showBackendError = useCallback(
    (payload: BackendErrorPayload, options?: { persistent?: boolean }) => {
      const id = nextId.current++
      const notice: ErrorNotice = {
        id,
        payload,
        presentation: resolveErrorPresentation(payload),
        persistent: options?.persistent,
      }

      setNotices((current) => [...current.slice(-1), notice])
      return id
    },
    [],
  )

  useEffect(() => {
    if (previewInitialized.current) {
      return
    }
    previewInitialized.current = true

    const params = new URLSearchParams(window.location.search)
    const preview = params.get("error")
    if (!preview) {
      return
    }

    const numericStatus = Number(params.get("status"))
    const statusCode =
      Number.isFinite(numericStatus) && numericStatus > 0 ? numericStatus : undefined

    showBackendError(
      {
        code: preview,
        statusCode,
        requestId: params.get("requestId") ?? "0198F6CE-DEMO-ERROR",
      },
      { persistent: true },
    )
  }, [showBackendError])

  const api = useMemo<ErrorNotificationApi>(
    () => ({
      showBackendError,
      dismissError,
      clearErrors,
    }),
    [clearErrors, dismissError, showBackendError],
  )

  return (
    <ErrorNotificationContext.Provider value={api}>
      {children}
      <ErrorNoticeViewport notices={notices} hasTabbar={hasTabbar} onDismiss={dismissError} />
    </ErrorNotificationContext.Provider>
  )
}

export function useErrorNotifications() {
  const value = useContext(ErrorNotificationContext)
  if (!value) {
    throw new Error("useErrorNotifications must be used inside ErrorNotificationProvider")
  }

  return value
}

function ErrorNoticeViewport({
  notices,
  hasTabbar,
  onDismiss,
}: {
  notices: ErrorNotice[]
  hasTabbar: boolean
  onDismiss: (id: number) => void
}) {
  return (
    <section
      className={`error-notice-viewport ${hasTabbar ? "has-tabbar" : ""}`}
      aria-live="assertive"
      aria-atomic="false"
    >
      <AnimatePresence initial={false}>
        {notices.map((notice) => (
          <ErrorNoticeCard key={notice.id} notice={notice} onDismiss={() => onDismiss(notice.id)} />
        ))}
      </AnimatePresence>
    </section>
  )
}

function ErrorNoticeCard({ notice, onDismiss }: { notice: ErrorNotice; onDismiss: () => void }) {
  const { t } = useTranslation()

  useEffect(() => {
    if (notice.persistent) {
      return
    }

    const duration = notice.presentation.tone === "internal" ? 9000 : 6200
    const timeout = window.setTimeout(onDismiss, duration)

    return () => window.clearTimeout(timeout)
  }, [notice.persistent, notice.presentation.tone, onDismiss])

  const { payload, presentation } = notice
  const requestId = payload.requestId?.trim()
  const showRequestId = presentation.tone === "internal" && requestId

  return (
    <motion.article
      className={`error-notice error-notice--${presentation.tone}`}
      initial={{ opacity: 0, y: -14, scale: 0.975 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, y: -9, scale: 0.985 }}
      transition={{ duration: 0.25, ease: [0.22, 1, 0.36, 1] }}
      role="alert"
    >
      <span className="error-notice__mascot">
        <ErrorMascot variant={presentation.mascot} />
      </span>

      <span className="error-notice__copy">
        <small>{presentation.eyebrow}</small>
        <strong>{presentation.title}</strong>
        <span>{presentation.message}</span>
        {showRequestId ? <em>ID · {requestId.slice(-8)}</em> : null}
      </span>

      <button
        type="button"
        className="error-notice__close"
        onClick={onDismiss}
        aria-label={t("errorNotifications.close")}
      >
        <i />
        <i />
      </button>

      <span className="error-notice__signal" aria-hidden="true">
        <i />
        <i />
        <i />
      </span>
    </motion.article>
  )
}
