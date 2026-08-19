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

type NoticeDefinition = Omit<NoticePresentation, "mascot"> & {
  mascot?: ErrorMascotVariant
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

const errorDefinitions: Record<string, NoticeDefinition> = {
  AUTHENTICATION_REQUIRED: {
    tone: "access",
    eyebrow: "СЕСІЯ",
    title: "Увійди знову",
    message: "Сесія завершилась.",
  },
  INVALID_AUTHENTICATION_PRINCIPAL: {
    tone: "access",
    eyebrow: "СЕСІЯ",
    title: "Не вдалося підтвердити користувача",
    message: "Онови авторизацію.",
  },
  ACCESS_DENIED: {
    tone: "access",
    eyebrow: "ДОСТУП",
    title: "Дія недоступна",
    message: "Для неї бракує прав.",
  },
  ACCOUNT_PERMISSION_DENIED: {
    tone: "access",
    eyebrow: "АККАУНТ",
    title: "Недостатньо прав",
    message: "Ця дія не дозволена твоїй ролі.",
  },
  ROLE_ACCESS_DENIED: {
    tone: "access",
    eyebrow: "РОЛІ",
    title: "Недостатньо прав",
    message: "Твоя роль нижче в ієрархії.",
  },
  ACCOUNT_OWNER_REQUIRED: {
    tone: "access",
    eyebrow: "АККАУНТ",
    title: "Потрібен власник",
    message: "Ця дія доступна лише власнику.",
  },
  ACCOUNT_MEMBER_KICK_FORBIDDEN: {
    tone: "access",
    eyebrow: "КОМАНДА",
    title: "Учасника не можна виключити",
    message: "Перевір ролі та свої права.",
  },
  PERSONALIZATION_ACCESS_DENIED: {
    tone: "access",
    eyebrow: "CREATOR",
    title: "Немає доступу",
    message: "Цю роботу не можна змінювати.",
  },
  SANCTION_TARGET_ACCESS_DENIED: {
    tone: "access",
    eyebrow: "МОДЕРАЦІЯ",
    title: "Санкція недоступна",
    message: "Рівень користувача вищий за твій.",
  },
  ACTIVE_ACCOUNT_NOT_SET: {
    tone: "access",
    eyebrow: "АККАУНТ",
    title: "Обери аккаунт",
    message: "Для цієї дії потрібен активний аккаунт.",
  },
  ACTIVE_ACCOUNT_REQUIRED: {
    tone: "access",
    eyebrow: "АККАУНТ",
    title: "Обери аккаунт",
    message: "Потрібен активний Cristalix аккаунт.",
  },
  ACCOUNT_NOT_ACTIVE: {
    tone: "access",
    eyebrow: "АККАУНТ",
    title: "Аккаунт неактивний",
    message: "Активуй його або обери інший.",
  },

  USER_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "КОРИСТУВАЧ",
    title: "Користувача не знайдено",
    message: "Перевір запит.",
  },
  ACCOUNT_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "АККАУНТ",
    title: "Аккаунт не знайдено",
    message: "Він міг бути видалений.",
  },
  ACCOUNT_MEMBER_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "КОМАНДА",
    title: "Учасника не знайдено",
    message: "Склад команди вже міг змінитися.",
  },
  ACTIVE_ACCOUNT_MEMBERSHIP_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "АККАУНТ",
    title: "Участь не знайдена",
    message: "Перевір активний аккаунт.",
  },
  ACCOUNT_INVITE_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "ІНВАЙТ",
    title: "Запрошення не знайдено",
    message: "Перевір код або створи нове.",
  },
  ROLE_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "РОЛІ",
    title: "Роль не знайдено",
    message: "Список ролей уже міг змінитися.",
  },
  ROLE_ASSIGNMENT_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "РОЛІ",
    title: "Призначення не знайдено",
    message: "Онови список учасників.",
  },
  BET_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "СТАВКА",
    title: "Раунд не знайдено",
    message: "Він міг бути закритий.",
  },
  PERSONALIZATION_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "ПЕРСОНАЛІЗАЦІЯ",
    title: "Роботу не знайдено",
    message: "Вона могла бути видалена.",
  },
  BADGE_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "БЕЙДЖ",
    title: "Бейдж не знайдено",
    message: "Онови список бейджів.",
  },
  SANCTION_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "МОДЕРАЦІЯ",
    title: "Санкцію не знайдено",
    message: "Її стан уже міг змінитися.",
  },
  SUBSCRIPTION_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "ПІДПИСКИ",
    title: "Підписку не знайдено",
    message: "Онови список підписок.",
  },
  CRISTALIX_PLAYER_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "CRISTALIX",
    title: "Гравця не знайдено",
    message: "Перевір nickname.",
  },
  COLLECTOR_CREDENTIAL_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "COLLECTOR",
    title: "Ключ не знайдено",
    message: "Credential міг бути відкликаний.",
  },
  ENDPOINT_NOT_FOUND: {
    tone: "not-found",
    eyebrow: "MNEMOS",
    title: "Адресу не знайдено",
    message: "Онови сторінку.",
  },

  USER_IDENTITY_CONFLICT: {
    tone: "conflict",
    eyebrow: "КОРИСТУВАЧ",
    title: "Профіль уже повʼязаний",
    message: "Дані користувача конфліктують.",
  },
  ACCOUNT_IDENTITY_CONFLICT: {
    tone: "conflict",
    eyebrow: "АККАУНТ",
    title: "Аккаунт уже привʼязаний",
    message: "Використай інший Cristalix профіль.",
  },
  ACCOUNT_DELETION_UNAVAILABLE: {
    tone: "conflict",
    eyebrow: "АККАУНТ",
    title: "Зараз не можна видалити",
    message: "Стан аккаунта блокує видалення.",
  },
  ACCOUNT_MEMBER_LIMIT_REACHED: {
    tone: "conflict",
    eyebrow: "КОМАНДА",
    title: "Команда заповнена",
    message: "Досягнуто ліміт учасників.",
  },
  ACCOUNT_MEMBER_ALREADY_EXISTS: {
    tone: "conflict",
    eyebrow: "КОМАНДА",
    title: "Учасник уже доданий",
    message: "Повторна дія не потрібна.",
  },
  ACCOUNT_OWNER_CANNOT_LEAVE: {
    tone: "conflict",
    eyebrow: "АККАУНТ",
    title: "Власник не може вийти",
    message: "Спочатку передай контроль.",
  },
  ACCOUNT_INVITE_UNAVAILABLE: {
    tone: "conflict",
    eyebrow: "ІНВАЙТ",
    title: "Запрошення недоступне",
    message: "Воно вже використане або прострочене.",
  },
  ROLE_ALREADY_ASSIGNED: {
    tone: "conflict",
    eyebrow: "РОЛІ",
    title: "Роль уже призначена",
    message: "Повторна дія не потрібна.",
  },
  ROLE_HIERARCHY_VIOLATION: {
    tone: "conflict",
    eyebrow: "РОЛІ",
    title: "Порядок ролей конфліктує",
    message: "Зміни позицію ролі.",
  },
  ROLE_HIERARCHY_INCONSISTENT: {
    tone: "conflict",
    eyebrow: "РОЛІ",
    title: "Ієрархія вже змінилась",
    message: "Онови ролі перед редагуванням.",
  },
  DAILY_REWARD_ALREADY_CLAIMED: {
    tone: "conflict",
    eyebrow: "НАГОРОДА",
    title: "Сьогодні вже забрано",
    message: "Повернись завтра.",
  },
  DAILY_STREAK_NOT_RECOVERABLE: {
    tone: "conflict",
    eyebrow: "СЕРІЯ",
    title: "Серію не відновити",
    message: "Вона не відповідає умовам відновлення.",
  },
  DAILY_STREAK_RESTORE_LIMIT_REACHED: {
    tone: "conflict",
    eyebrow: "СЕРІЯ",
    title: "Відновлення закінчились",
    message: "Місячний ліміт уже використаний.",
  },
  BET_CONFLICT: {
    tone: "conflict",
    eyebrow: "СТАВКА",
    title: "Раунд уже змінився",
    message: "Онови дані перед ставкою.",
  },
  PERSONALIZATION_ALREADY_OWNED: {
    tone: "conflict",
    eyebrow: "ПЕРСОНАЛІЗАЦІЯ",
    title: "Вона вже твоя",
    message: "Повторна покупка не потрібна.",
  },
  PERSONALIZATION_NOT_PURCHASABLE: {
    tone: "conflict",
    eyebrow: "ПЕРСОНАЛІЗАЦІЯ",
    title: "Зараз не продається",
    message: "Статус персоналізації змінився.",
  },
  PURCHASE_IDEMPOTENCY_CONFLICT: {
    tone: "conflict",
    eyebrow: "ПОКУПКА",
    title: "Запит уже оброблявся",
    message: "Повторного списання не буде.",
  },
  PERSONALIZATION_NOT_OWNED: {
    tone: "conflict",
    eyebrow: "ПЕРСОНАЛІЗАЦІЯ",
    title: "Її немає в колекції",
    message: "Спочатку отримай персоналізацію.",
  },
  PERSONALIZATION_OWNERSHIP_CONFLICT: {
    tone: "conflict",
    eyebrow: "ПЕРСОНАЛІЗАЦІЯ",
    title: "Колекція вже змінилась",
    message: "Онови список.",
  },
  BADGE_ALREADY_EXISTS: {
    tone: "conflict",
    eyebrow: "БЕЙДЖ",
    title: "Такий бейдж уже є",
    message: "Відкрий існуючий або зміни дані.",
  },
  BADGE_NOT_OWNED: {
    tone: "conflict",
    eyebrow: "БЕЙДЖ",
    title: "Бейдж ще не твій",
    message: "Його не можна активувати.",
  },
  SANCTION_CONFLICT: {
    tone: "conflict",
    eyebrow: "МОДЕРАЦІЯ",
    title: "Стан уже змінився",
    message: "Онови дані користувача.",
  },
  SELF_SANCTION: {
    tone: "conflict",
    eyebrow: "МОДЕРАЦІЯ",
    title: "Не можна санкціонувати себе",
    message: "Обери іншого користувача.",
  },
  WALLET_ADJUSTMENT_IDEMPOTENCY_CONFLICT: {
    tone: "conflict",
    eyebrow: "ГАМАНЕЦЬ",
    title: "Корекцію вже оброблено",
    message: "Повторна операція заблокована.",
  },

  VALIDATION_FAILED: {
    tone: "validation",
    eyebrow: "ДАНІ",
    title: "Перевір поля",
    message: "Деякі значення некоректні.",
  },
  CONSTRAINT_VIOLATION: {
    tone: "validation",
    eyebrow: "ДАНІ",
    title: "Некоректні параметри",
    message: "Перевір введені значення.",
  },
  INVALID_REQUEST_BODY: {
    tone: "validation",
    eyebrow: "ЗАПИТ",
    title: "Не вдалося прочитати дані",
    message: "Повтори дію.",
  },
  INVALID_PARAMETER: {
    tone: "validation",
    eyebrow: "ЗАПИТ",
    title: "Некоректний параметр",
    message: "Перевір значення.",
  },
  MISSING_REQUEST_PARAMETER: {
    tone: "validation",
    eyebrow: "ЗАПИТ",
    title: "Бракує параметра",
    message: "Онови форму та повтори дію.",
  },
  MISSING_REQUEST_HEADER: {
    tone: "validation",
    eyebrow: "ЗАПИТ",
    title: "Запит неповний",
    message: "Онови сторінку.",
  },
  INVALID_ROLE_HIERARCHY: {
    tone: "validation",
    eyebrow: "РОЛІ",
    title: "Некоректний порядок ролей",
    message: "Перевір позиції в ієрархії.",
  },
  INVALID_ACCOUNT_INVITE_EXPIRATION: {
    tone: "validation",
    eyebrow: "ІНВАЙТ",
    title: "Некоректний строк",
    message: "Зміни термін дії запрошення.",
  },
  SANCTION_DURATION_EXCEEDED: {
    tone: "validation",
    eyebrow: "МОДЕРАЦІЯ",
    title: "Завеликий строк",
    message: "Скороти тривалість санкції.",
  },
  METHOD_NOT_ALLOWED: {
    tone: "validation",
    eyebrow: "MNEMOS",
    title: "Дія не підтримується",
    message: "Онови сторінку.",
  },
  UNSUPPORTED_MEDIA_TYPE: {
    tone: "validation",
    eyebrow: "MNEMOS",
    title: "Формат не підтримується",
    message: "Повтори дію після оновлення.",
  },
  NOT_ACCEPTABLE: {
    tone: "validation",
    eyebrow: "MNEMOS",
    title: "Відповідь недоступна",
    message: "Онови сторінку.",
  },

  PERSONALIZATION_PURCHASE_IN_PROGRESS: {
    tone: "waiting",
    eyebrow: "ПОКУПКА",
    title: "Покупка обробляється",
    message: "Не запускай її повторно.",
  },
  ACCOUNT_UNAVAILABLE: {
    tone: "waiting",
    eyebrow: "АККАУНТ",
    title: "Аккаунт недоступний",
    message: "Спробуй трохи пізніше.",
  },
  CRISTALIX_RATE_LIMIT_EXCEEDED: {
    tone: "waiting",
    eyebrow: "CRISTALIX",
    title: "Забагато запитів",
    message: "Зачекай перед наступною спробою.",
  },
  CRISTALIX_UNAVAILABLE: {
    tone: "waiting",
    eyebrow: "CRISTALIX",
    title: "Сервіс недоступний",
    message: "Спробуй трохи пізніше.",
  },
  CRISTALIX_AUTHORIZATION_FAILED: {
    tone: "waiting",
    eyebrow: "CRISTALIX",
    title: "Звʼязок не авторизовано",
    message: "Mnemos повторить запит пізніше.",
  },
  CRISTALIX_INVALID_RESPONSE: {
    tone: "waiting",
    eyebrow: "CRISTALIX",
    title: "Отримано некоректну відповідь",
    message: "Спробуй трохи пізніше.",
  },
  REALTIME_CONNECTION_LIMIT_EXCEEDED: {
    tone: "waiting",
    eyebrow: "REALTIME",
    title: "Забагато підключень",
    message: "Закрий зайве підключення або зачекай.",
  },
  REALTIME_CONNECTION_CAPACITY_UNAVAILABLE: {
    tone: "waiting",
    eyebrow: "REALTIME",
    title: "Realtime тимчасово зайнятий",
    message: "Підключення відновиться пізніше.",
  },

  INTERNAL_SERVER_ERROR: {
    tone: "internal",
    eyebrow: "MNEMOS",
    title: "Щось пішло не так",
    message: "Повтори дію трохи пізніше.",
  },
}

function normalizeCode(code?: string | null) {
  return code?.trim().toUpperCase() || null
}

function inferTone(payload: BackendErrorPayload): NoticeTone {
  const code = normalizeCode(payload.code)
  const status = payload.statusCode ?? null

  if (code) {
    if (
      code.includes("ACCESS_DENIED") ||
      code.includes("PERMISSION_DENIED") ||
      code.includes("OWNER_REQUIRED") ||
      code.includes("UNAUTHORIZED") ||
      code === "AUTHENTICATION_REQUIRED" ||
      code === "ACCOUNT_NOT_ACTIVE"
    ) return "access"

    if (code.includes("NOT_FOUND")) return "not-found"

    if (
      code.includes("RATE_LIMIT") ||
      code.includes("IN_PROGRESS") ||
      code.includes("UNAVAILABLE") ||
      code.includes("CAPACITY")
    ) return "waiting"

    if (
      code.includes("CONFLICT") ||
      code.includes("ALREADY") ||
      code.includes("LIMIT_REACHED") ||
      code.includes("NOT_OWNED") ||
      code.includes("NOT_PURCHASABLE") ||
      code.includes("NOT_RECOVERABLE") ||
      code.includes("CANNOT_LEAVE")
    ) return "conflict"

    if (
      code.includes("INVALID") ||
      code.includes("MISSING") ||
      code.includes("VIOLATION") ||
      code.includes("EXCEEDED")
    ) return "validation"
  }

  if (status === 401 || status === 403) return "access"
  if (status === 404) return "not-found"
  if (status === 409) return "conflict"
  if (status === 429 || status === 502 || status === 503 || status === 504) return "waiting"
  if (status === 400 || status === 405 || status === 406 || status === 415 || status === 422) return "validation"

  return "internal"
}

function fallbackFor(tone: NoticeTone): NoticePresentation {
  switch (tone) {
    case "validation":
      return {
        tone,
        mascot: tone,
        eyebrow: "ДАНІ",
        title: "Перевір значення",
        message: "Запит містить некоректні дані.",
      }
    case "access":
      return {
        tone,
        mascot: tone,
        eyebrow: "ДОСТУП",
        title: "Дія недоступна",
        message: "Для неї бракує прав.",
      }
    case "conflict":
      return {
        tone,
        mascot: tone,
        eyebrow: "СТАН",
        title: "Дані вже змінились",
        message: "Онови сторінку та повтори дію.",
      }
    case "not-found":
      return {
        tone,
        mascot: tone,
        eyebrow: "НЕ ЗНАЙДЕНО",
        title: "Обʼєкт недоступний",
        message: "Він міг бути видалений.",
      }
    case "waiting":
      return {
        tone,
        mascot: tone,
        eyebrow: "ТИМЧАСОВО",
        title: "Спробуй трохи пізніше",
        message: "Сервіс або операція зараз зайняті.",
      }
    case "internal":
      return {
        tone,
        mascot: tone,
        eyebrow: "MNEMOS",
        title: "Невідома помилка",
        message: "Повтори дію трохи пізніше.",
      }
  }
}

export function resolveErrorPresentation(payload: BackendErrorPayload): NoticePresentation {
  const code = normalizeCode(payload.code)
  const definition = code ? errorDefinitions[code] : undefined

  if (definition) {
    return {
      ...definition,
      mascot: definition.mascot ?? definition.tone,
    }
  }

  return fallbackFor(inferTone(payload))
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

      <button className="error-notice__close" onClick={onDismiss} aria-label="Закрити повідомлення">
        <i />
        <i />
      </button>

      <span className="error-notice__signal" aria-hidden="true"><i /><i /><i /></span>
    </motion.article>
  )
}
