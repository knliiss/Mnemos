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
import { ErrorMascot, type ErrorMascotVariant } from "./ErrorMascot"

export type BackendErrorPayload = {
  code?: string | null
  message?: string | null
  statusCode?: number | null
  details?: Record<string, unknown> | null
  path?: string | null
  requestId?: string | null
}

type NoticeTone = "validation" | "access" | "conflict" | "not-found" | "waiting" | "internal"

type NoticePresentation = {
  tone: NoticeTone
  mascot: ErrorMascotVariant
  eyebrow: string
  title: string
  message: string
}

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

const directCopy: Record<string, Omit<NoticePresentation, "tone" | "mascot">> = {
  AUTHENTICATION_REQUIRED: {
    eyebrow: "СЕСІЯ",
    title: "Потрібно увійти знову",
    message: "Сесія завершилась або більше не дійсна. Онови авторизацію і повтори дію.",
  },
  ACCESS_DENIED: {
    eyebrow: "ДОСТУП",
    title: "Ця дія недоступна",
    message: "Для неї бракує системних прав або ролі.",
  },
  ACCOUNT_PERMISSION_DENIED: {
    eyebrow: "ПРАВА АККАУНТА",
    title: "Недостатньо прав",
    message: "У твоєї ролі немає дозволу на цю дію в аккаунті.",
  },
  ROLE_ACCESS_DENIED: {
    eyebrow: "ІЄРАРХІЯ РОЛЕЙ",
    title: "Роль вище твоєї",
    message: "Цю зміну блокує ієрархія ролей аккаунта.",
  },
  ACTIVE_ACCOUNT_NOT_SET: {
    eyebrow: "АККАУНТ",
    title: "Спочатку обери аккаунт",
    message: "Для цієї дії Mnemos потрібен активний Cristalix аккаунт.",
  },
  ACTIVE_ACCOUNT_REQUIRED: {
    eyebrow: "АККАУНТ",
    title: "Потрібен активний аккаунт",
    message: "Обери Cristalix аккаунт і повтори дію.",
  },
  ACCOUNT_NOT_ACTIVE: {
    eyebrow: "АККАУНТ",
    title: "Аккаунт неактивний",
    message: "Активуй його або переключись на інший аккаунт.",
  },
  ACCOUNT_MEMBER_LIMIT_REACHED: {
    eyebrow: "КОМАНДА",
    title: "У команді немає місця",
    message: "Ліміт учасників цього аккаунта вже досягнуто.",
  },
  ACCOUNT_MEMBER_ALREADY_EXISTS: {
    eyebrow: "КОМАНДА",
    title: "Користувач уже в команді",
    message: "Повторно додавати цього учасника не потрібно.",
  },
  ACCOUNT_OWNER_CANNOT_LEAVE: {
    eyebrow: "ВЛАСНИК",
    title: "Власник не може вийти",
    message: "Спочатку передай контроль або зміни структуру аккаунта.",
  },
  ACCOUNT_INVITE_UNAVAILABLE: {
    eyebrow: "ІНВАЙТ",
    title: "Запрошення вже недоступне",
    message: "Воно могло бути використане, відкликане або прострочене.",
  },
  INVALID_ACCOUNT_INVITE_EXPIRATION: {
    eyebrow: "ІНВАЙТ",
    title: "Некоректний строк запрошення",
    message: "Зміни термін дії інвайту і спробуй ще раз.",
  },
  DAILY_REWARD_ALREADY_CLAIMED: {
    eyebrow: "ЩОДЕННА НАГОРОДА",
    title: "Сьогодні вже забрано",
    message: "Наступна нагорода стане доступною після нового дня.",
  },
  DAILY_STREAK_NOT_RECOVERABLE: {
    eyebrow: "СЕРІЯ",
    title: "Цю серію вже не відновити",
    message: "Поточний streak не відповідає умовам відновлення.",
  },
  DAILY_STREAK_RESTORE_LIMIT_REACHED: {
    eyebrow: "СЕРІЯ",
    title: "Відновлення закінчились",
    message: "Місячний ліміт відновлень streak уже використаний.",
  },
  BET_CONFLICT: {
    eyebrow: "СТАВКА",
    title: "Раунд уже змінився",
    message: "Онови стан раунду перед тим, як повторювати ставку.",
  },
  BET_NOT_FOUND: {
    eyebrow: "СТАВКА",
    title: "Раунд не знайдено",
    message: "Можливо, його вже закрили або він більше недоступний.",
  },
  PERSONALIZATION_ALREADY_OWNED: {
    eyebrow: "ПЕРСОНАЛІЗАЦІЯ",
    title: "Вона вже твоя",
    message: "Повторна покупка цієї персоналізації не потрібна.",
  },
  PERSONALIZATION_NOT_PURCHASABLE: {
    eyebrow: "ПЕРСОНАЛІЗАЦІЯ",
    title: "Зараз її не можна купити",
    message: "Статус або доступ цієї персоналізації змінився.",
  },
  PERSONALIZATION_PURCHASE_IN_PROGRESS: {
    eyebrow: "ПОКУПКА",
    title: "Покупка ще обробляється",
    message: "Не запускай її повторно — дочекайся результату операції.",
  },
  PURCHASE_IDEMPOTENCY_CONFLICT: {
    eyebrow: "ПОКУПКА",
    title: "Запит уже оброблявся",
    message: "Mnemos захистив покупку від повторного списання.",
  },
  PERSONALIZATION_NOT_OWNED: {
    eyebrow: "ПЕРСОНАЛІЗАЦІЯ",
    title: "Цього немає у твоїй колекції",
    message: "Придбай або отримай персоналізацію перед активацією.",
  },
  PERSONALIZATION_ACCESS_DENIED: {
    eyebrow: "CREATOR STUDIO",
    title: "Немає доступу до цієї роботи",
    message: "Редагувати або модерувати її може лише дозволена роль.",
  },
  BADGE_NOT_OWNED: {
    eyebrow: "БЕЙДЖ",
    title: "Бейдж ще не отримано",
    message: "Його не можна активувати, поки він не належить користувачу.",
  },
  SANCTION_TARGET_ACCESS_DENIED: {
    eyebrow: "МОДЕРАЦІЯ",
    title: "Цього користувача не можна санкціонувати",
    message: "Його системний рівень захищений від твоєї ролі.",
  },
  SELF_SANCTION: {
    eyebrow: "МОДЕРАЦІЯ",
    title: "Не можна санкціонувати себе",
    message: "Обери іншого користувача.",
  },
  SANCTION_DURATION_EXCEEDED: {
    eyebrow: "МОДЕРАЦІЯ",
    title: "Завеликий строк санкції",
    message: "Скороти тривалість до дозволеного для твоєї ролі максимуму.",
  },
  CRISTALIX_PLAYER_NOT_FOUND: {
    eyebrow: "CRISTALIX",
    title: "Гравця не знайдено",
    message: "Перевір nickname або спробуй інший запит.",
  },
  CRISTALIX_RATE_LIMIT_EXCEEDED: {
    eyebrow: "CRISTALIX",
    title: "Забагато запитів",
    message: "Cristalix просить трохи зачекати перед наступною спробою.",
  },
  CRISTALIX_UNAVAILABLE: {
    eyebrow: "CRISTALIX",
    title: "Cristalix тимчасово недоступний",
    message: "Mnemos не може отримати актуальні дані. Спробуй трохи пізніше.",
  },
  VALIDATION_FAILED: {
    eyebrow: "ПЕРЕВІРКА ДАНИХ",
    title: "Дещо треба виправити",
    message: "Перевір підсвічені поля та повтори дію.",
  },
  CONSTRAINT_VIOLATION: {
    eyebrow: "ПЕРЕВІРКА ДАНИХ",
    title: "Некоректні параметри",
    message: "Одне або кілька значень не відповідають дозволеним умовам.",
  },
  INVALID_REQUEST_BODY: {
    eyebrow: "ЗАПИТ",
    title: "Не вдалося прочитати дані",
    message: "Спробуй ще раз. Якщо помилка повторюється — онови сторінку.",
  },
  INTERNAL_SERVER_ERROR: {
    eyebrow: "MNEMOS",
    title: "Щось пішло не за планом",
    message: "Це внутрішня помилка. Можеш повторити дію або спробувати трохи пізніше.",
  },
}

const validationCodes = new Set([
  "VALIDATION_FAILED",
  "CONSTRAINT_VIOLATION",
  "INVALID_REQUEST_BODY",
  "INVALID_PARAMETER",
  "MISSING_REQUEST_PARAMETER",
  "MISSING_REQUEST_HEADER",
  "INVALID_ROLE_HIERARCHY",
  "INVALID_ACCOUNT_INVITE_EXPIRATION",
  "SANCTION_DURATION_EXCEEDED",
])

function normalizeCode(code?: string | null) {
  return code?.trim().toUpperCase() || null
}

function toneFor(payload: BackendErrorPayload): NoticeTone {
  const code = normalizeCode(payload.code)
  const status = payload.statusCode ?? null

  if (code && validationCodes.has(code)) return "validation"
  if (status === 401 || status === 403) return "access"
  if (status === 429) return "waiting"
  if (status === 404) return "not-found"
  if (status === 409) return "conflict"
  if (status && status >= 500) return "internal"

  if (!code) return status ? "internal" : "waiting"

  if (
    code.includes("ACCESS_DENIED") ||
    code.includes("PERMISSION_DENIED") ||
    code.includes("OWNER_REQUIRED") ||
    code === "ACCOUNT_NOT_ACTIVE" ||
    code === "AUTHENTICATION_REQUIRED"
  ) return "access"

  if (code.includes("NOT_FOUND")) return "not-found"
  if (code.includes("RATE_LIMIT") || code.includes("IN_PROGRESS") || code.includes("UNAVAILABLE")) return "waiting"
  if (
    code.includes("CONFLICT") ||
    code.includes("ALREADY") ||
    code.includes("LIMIT_REACHED") ||
    code.includes("NOT_PURCHASABLE") ||
    code.includes("NOT_RECOVERABLE") ||
    code.includes("CANNOT_LEAVE")
  ) return "conflict"

  if (code === "INTERNAL_SERVER_ERROR") return "internal"

  return "internal"
}

function defaultCopy(tone: NoticeTone): Omit<NoticePresentation, "tone" | "mascot"> {
  switch (tone) {
    case "validation":
      return {
        eyebrow: "ПЕРЕВІРКА",
        title: "Перевір введені дані",
        message: "Mnemos не зміг виконати дію з поточними значеннями.",
      }
    case "access":
      return {
        eyebrow: "ДОСТУП",
        title: "Дія недоступна",
        message: "Для цього потрібні інші права, роль або активний аккаунт.",
      }
    case "conflict":
      return {
        eyebrow: "СТАН ЗМІНИВСЯ",
        title: "Цю дію вже не можна виконати так само",
        message: "Онови дані та спробуй ще раз.",
      }
    case "not-found":
      return {
        eyebrow: "НЕ ЗНАЙДЕНО",
        title: "Тут уже нічого немає",
        message: "Обʼєкт міг бути видалений, закритий або змінити адресу.",
      }
    case "waiting":
      return {
        eyebrow: "ТИМЧАСОВО",
        title: "Потрібно трохи зачекати",
        message: "Сервіс або операція зараз зайняті. Повтори спробу пізніше.",
      }
    case "internal":
      return {
        eyebrow: "MNEMOS",
        title: "Невідома помилка",
        message: "Не вдалося визначити причину. Повтори дію або спробуй пізніше.",
      }
  }
}

export function resolveErrorPresentation(payload: BackendErrorPayload): NoticePresentation {
  const code = normalizeCode(payload.code)
  const tone = toneFor(payload)
  const copy = code ? directCopy[code] : undefined

  return {
    tone,
    mascot: tone,
    ...(copy ?? defaultCopy(tone)),
  }
}

export function ErrorNotificationProvider({ children, hasTabbar }: ProviderProps) {
  const [notices, setNotices] = useState<ErrorNotice[]>([])
  const nextId = useRef(1)
  const previewInitialized = useRef(false)

  const dismissError = useCallback((id: number) => {
    setNotices((current) => current.filter((notice) => notice.id !== id))
  }, [])

  const clearErrors = useCallback(() => setNotices([]), [])

  const showBackendError = useCallback((payload: BackendErrorPayload, options?: { persistent?: boolean }) => {
    const id = nextId.current++
    const notice: ErrorNotice = {
      id,
      payload,
      presentation: resolveErrorPresentation(payload),
      persistent: options?.persistent,
    }

    setNotices((current) => [...current.slice(-1), notice])
    return id
  }, [])

  useEffect(() => {
    if (previewInitialized.current) return
    previewInitialized.current = true

    const params = new URLSearchParams(window.location.search)
    const preview = params.get("error")
    if (!preview) return

    const numericStatus = Number(params.get("status"))
    const statusCode = Number.isFinite(numericStatus) && numericStatus > 0 ? numericStatus : undefined

    showBackendError(
      {
        code: preview,
        statusCode,
        requestId: params.get("requestId") ?? "0198F6CE-DEMO-ERROR",
      },
      { persistent: true },
    )
  }, [showBackendError])

  const api = useMemo<ErrorNotificationApi>(() => ({
    showBackendError,
    dismissError,
    clearErrors,
  }), [clearErrors, dismissError, showBackendError])

  return (
    <ErrorNotificationContext.Provider value={api}>
      {children}
      <ErrorNoticeViewport
        notices={notices}
        hasTabbar={hasTabbar}
        onDismiss={dismissError}
      />
    </ErrorNotificationContext.Provider>
  )
}

export function useErrorNotifications() {
  const value = useContext(ErrorNotificationContext)
  if (!value) throw new Error("useErrorNotifications must be used inside ErrorNotificationProvider")

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
          <ErrorNoticeCard
            key={notice.id}
            notice={notice}
            onDismiss={() => onDismiss(notice.id)}
          />
        ))}
      </AnimatePresence>
    </section>
  )
}

function ErrorNoticeCard({ notice, onDismiss }: { notice: ErrorNotice; onDismiss: () => void }) {
  useEffect(() => {
    if (notice.persistent) return

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
      initial={{ opacity: 0, y: 18, scale: 0.965 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, y: 10, scale: 0.98 }}
      transition={{ duration: 0.28, ease: [0.22, 1, 0.36, 1] }}
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

      <button className="error-notice__close" onClick={onDismiss} aria-label="Закрити повідомлення">
        <i />
        <i />
      </button>

      <span className="error-notice__signal" aria-hidden="true"><i /><i /><i /></span>
    </motion.article>
  )
}
