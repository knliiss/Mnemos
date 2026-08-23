export const errorCodesUk = {
  AUTHENTICATION_REQUIRED: {
    title: "Увійди знову",
    message: "Сесія завершилась.",
  },
  INVALID_AUTHENTICATION_PRINCIPAL: {
    title: "Не вдалося підтвердити користувача",
    message: "Онови авторизацію.",
  },
  ACCESS_DENIED: {
    title: "Дія недоступна",
    message: "Для неї бракує прав.",
  },
  ACCOUNT_PERMISSION_DENIED: {
    title: "Недостатньо прав",
    message: "Ця дія не дозволена твоїй ролі.",
  },
  ROLE_ACCESS_DENIED: {
    title: "Недостатньо прав",
    message: "Твоя роль нижче в ієрархії.",
  },
  ACCOUNT_OWNER_REQUIRED: {
    title: "Потрібен власник",
    message: "Ця дія доступна лише власнику.",
  },
  ACCOUNT_MEMBER_KICK_FORBIDDEN: {
    title: "Учасника не можна виключити",
    message: "Перевір ролі та свої права.",
  },
  PERSONALIZATION_ACCESS_DENIED: {
    title: "Немає доступу",
    message: "Цю роботу не можна змінювати.",
  },
  SANCTION_TARGET_ACCESS_DENIED: {
    title: "Санкція недоступна",
    message: "Рівень користувача вищий за твій.",
  },
  ACTIVE_ACCOUNT_NOT_SET: {
    title: "Обери акаунт",
    message: "Для цієї дії потрібен активний акаунт.",
  },
  ACTIVE_ACCOUNT_REQUIRED: {
    title: "Обери акаунт",
    message: "Потрібен активний Cristalix акаунт.",
  },
  ACCOUNT_NOT_ACTIVE: {
    title: "Акаунт неактивний",
    message: "Активуй його або обери інший.",
  },
  USER_NOT_FOUND: {
    title: "Користувача не знайдено",
    message: "Перевір запит.",
  },
  ACCOUNT_NOT_FOUND: {
    title: "Акаунт не знайдено",
    message: "Він міг бути видалений.",
  },
  ACCOUNT_MEMBER_NOT_FOUND: {
    title: "Учасника не знайдено",
    message: "Склад команди вже міг змінитися.",
  },
  ACTIVE_ACCOUNT_MEMBERSHIP_NOT_FOUND: {
    title: "Участь не знайдена",
    message: "Перевір активний акаунт.",
  },
  ACCOUNT_INVITE_NOT_FOUND: {
    title: "Запрошення не знайдено",
    message: "Перевір код або створи нове.",
  },
  ROLE_NOT_FOUND: {
    title: "Роль не знайдено",
    message: "Список ролей уже міг змінитися.",
  },
  ROLE_ASSIGNMENT_NOT_FOUND: {
    title: "Призначення не знайдено",
    message: "Онови список учасників.",
  },
  BET_NOT_FOUND: {
    title: "Раунд не знайдено",
    message: "Він міг бути закритий.",
  },
  PERSONALIZATION_NOT_FOUND: {
    title: "Роботу не знайдено",
    message: "Вона могла бути видалена.",
  },
  BADGE_NOT_FOUND: {
    title: "Бейдж не знайдено",
    message: "Онови список бейджів.",
  },
  SANCTION_NOT_FOUND: {
    title: "Санкцію не знайдено",
    message: "Її стан уже міг змінитися.",
  },
  SUBSCRIPTION_NOT_FOUND: {
    title: "Підписку не знайдено",
    message: "Онови список підписок.",
  },
  CRISTALIX_PLAYER_NOT_FOUND: {
    title: "Гравця не знайдено",
    message: "Перевір nickname.",
  },
  COLLECTOR_CREDENTIAL_NOT_FOUND: {
    title: "Ключ не знайдено",
    message: "Credential міг бути відкликаний.",
  },
  ENDPOINT_NOT_FOUND: {
    title: "Адресу не знайдено",
    message: "Онови сторінку.",
  },
  USER_IDENTITY_CONFLICT: {
    title: "Профіль уже повʼязаний",
    message: "Дані користувача конфліктують.",
  },
  ACCOUNT_IDENTITY_CONFLICT: {
    title: "Акаунт уже привʼязаний",
    message: "Використай інший Cristalix профіль.",
  },
  ACCOUNT_DELETION_UNAVAILABLE: {
    title: "Зараз не можна видалити",
    message: "Стан акаунта блокує видалення.",
  },
  ACCOUNT_MEMBER_LIMIT_REACHED: {
    title: "Команда заповнена",
    message: "Досягнуто ліміт учасників.",
  },
  ACCOUNT_MEMBER_ALREADY_EXISTS: {
    title: "Учасник уже доданий",
    message: "Повторна дія не потрібна.",
  },
  ACCOUNT_OWNER_CANNOT_LEAVE: {
    title: "Власник не може вийти",
    message: "Спочатку передай контроль.",
  },
  ACCOUNT_INVITE_UNAVAILABLE: {
    title: "Запрошення недоступне",
    message: "Воно вже використане або прострочене.",
  },
  ROLE_ALREADY_ASSIGNED: {
    title: "Роль уже призначена",
    message: "Повторна дія не потрібна.",
  },
  ROLE_HIERARCHY_VIOLATION: {
    title: "Порядок ролей конфліктує",
    message: "Зміни позицію ролі.",
  },
  ROLE_HIERARCHY_INCONSISTENT: {
    title: "Ієрархія вже змінилась",
    message: "Онови ролі перед редагуванням.",
  },
  DAILY_REWARD_ALREADY_CLAIMED: {
    title: "Сьогодні вже забрано",
    message: "Повернись завтра.",
  },
  DAILY_STREAK_NOT_RECOVERABLE: {
    title: "Серію не відновити",
    message: "Вона не відповідає умовам відновлення.",
  },
  DAILY_STREAK_RESTORE_LIMIT_REACHED: {
    title: "Відновлення закінчились",
    message: "Місячний ліміт уже використаний.",
  },
  BET_CONFLICT: {
    title: "Раунд уже змінився",
    message: "Онови дані перед ставкою.",
  },
  PERSONALIZATION_ALREADY_OWNED: {
    title: "Вона вже твоя",
    message: "Повторна покупка не потрібна.",
  },
  PERSONALIZATION_NOT_PURCHASABLE: {
    title: "Зараз не продається",
    message: "Статус персоналізації змінився.",
  },
  PURCHASE_IDEMPOTENCY_CONFLICT: {
    title: "Запит уже оброблявся",
    message: "Повторного списання не буде.",
  },
  PERSONALIZATION_NOT_OWNED: {
    title: "Її немає в колекції",
    message: "Спочатку отримай персоналізацію.",
  },
  PERSONALIZATION_OWNERSHIP_CONFLICT: {
    title: "Колекція вже змінилась",
    message: "Онови список.",
  },
  BADGE_ALREADY_EXISTS: {
    title: "Такий бейдж уже є",
    message: "Відкрий існуючий або зміни дані.",
  },
  BADGE_NOT_OWNED: {
    title: "Бейдж ще не твій",
    message: "Його не можна активувати.",
  },
  SANCTION_CONFLICT: {
    title: "Стан уже змінився",
    message: "Онови дані користувача.",
  },
  SELF_SANCTION: {
    title: "Не можна санкціонувати себе",
    message: "Обери іншого користувача.",
  },
  WALLET_ADJUSTMENT_IDEMPOTENCY_CONFLICT: {
    title: "Корекцію вже оброблено",
    message: "Повторна операція заблокована.",
  },
  VALIDATION_FAILED: {
    title: "Перевір поля",
    message: "Деякі значення некоректні.",
  },
  CONSTRAINT_VIOLATION: {
    title: "Некоректні параметри",
    message: "Перевір введені значення.",
  },
  INVALID_REQUEST_BODY: {
    title: "Не вдалося прочитати дані",
    message: "Повтори дію.",
  },
  INVALID_PARAMETER: {
    title: "Некоректний параметр",
    message: "Перевір значення.",
  },
  MISSING_REQUEST_PARAMETER: {
    title: "Бракує параметра",
    message: "Онови форму та повтори дію.",
  },
  MISSING_REQUEST_HEADER: {
    title: "Запит неповний",
    message: "Онови сторінку.",
  },
  INVALID_ROLE_HIERARCHY: {
    title: "Некоректний порядок ролей",
    message: "Перевір позиції в ієрархії.",
  },
  INVALID_ACCOUNT_INVITE_EXPIRATION: {
    title: "Некоректний строк",
    message: "Зміни термін дії запрошення.",
  },
  SANCTION_DURATION_EXCEEDED: {
    title: "Завеликий строк",
    message: "Скороти тривалість санкції.",
  },
  METHOD_NOT_ALLOWED: {
    title: "Дія не підтримується",
    message: "Онови сторінку.",
  },
  UNSUPPORTED_MEDIA_TYPE: {
    title: "Формат не підтримується",
    message: "Повтори дію після оновлення.",
  },
  NOT_ACCEPTABLE: {
    title: "Відповідь недоступна",
    message: "Онови сторінку.",
  },
  PERSONALIZATION_PURCHASE_IN_PROGRESS: {
    title: "Покупка обробляється",
    message: "Не запускай її повторно.",
  },
  ACCOUNT_UNAVAILABLE: {
    title: "Акаунт недоступний",
    message: "Спробуй трохи пізніше.",
  },
  CRISTALIX_RATE_LIMIT_EXCEEDED: {
    title: "Забагато запитів",
    message: "Зачекай перед наступною спробою.",
  },
  CRISTALIX_UNAVAILABLE: {
    title: "Сервіс недоступний",
    message: "Спробуй трохи пізніше.",
  },
  CRISTALIX_AUTHORIZATION_FAILED: {
    title: "Звʼязок не авторизовано",
    message: "Mnemos повторить запит пізніше.",
  },
  CRISTALIX_INVALID_RESPONSE: {
    title: "Отримано некоректну відповідь",
    message: "Спробуй трохи пізніше.",
  },
  REALTIME_CONNECTION_LIMIT_EXCEEDED: {
    title: "Забагато підключень",
    message: "Закрий зайве підключення або зачекай.",
  },
  REALTIME_CONNECTION_CAPACITY_UNAVAILABLE: {
    title: "Realtime тимчасово зайнятий",
    message: "Підключення відновиться пізніше.",
  },
  INTERNAL_SERVER_ERROR: {
    title: "Щось пішло не так",
    message: "Повтори дію трохи пізніше.",
  },
} as const
