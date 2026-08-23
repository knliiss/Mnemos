export const errorCodesRu = {
  AUTHENTICATION_REQUIRED: {
    title: "Войдите снова",
    message: "Сессия завершилась.",
  },
  INVALID_AUTHENTICATION_PRINCIPAL: {
    title: "Не удалось подтвердить пользователя",
    message: "Обновите авторизацию.",
  },
  ACCESS_DENIED: {
    title: "Действие недоступно",
    message: "Для него недостаточно прав.",
  },
  ACCOUNT_PERMISSION_DENIED: {
    title: "Недостаточно прав",
    message: "Это действие не разрешено вашей роли.",
  },
  ROLE_ACCESS_DENIED: {
    title: "Недостаточно прав",
    message: "Ваша роль ниже в иерархии.",
  },
  ACCOUNT_OWNER_REQUIRED: {
    title: "Требуется владелец",
    message: "Это действие доступно только владельцу.",
  },
  ACCOUNT_MEMBER_KICK_FORBIDDEN: {
    title: "Участника нельзя исключить",
    message: "Проверьте роли и свои права.",
  },
  PERSONALIZATION_ACCESS_DENIED: {
    title: "Нет доступа",
    message: "Эту работу нельзя изменить.",
  },
  SANCTION_TARGET_ACCESS_DENIED: {
    title: "Санкция недоступна",
    message: "Уровень пользователя выше вашего.",
  },
  ACTIVE_ACCOUNT_NOT_SET: {
    title: "Выберите аккаунт",
    message: "Для этого действия нужен активный аккаунт.",
  },
  ACTIVE_ACCOUNT_REQUIRED: {
    title: "Выберите аккаунт",
    message: "Нужен активный Cristalix аккаунт.",
  },
  ACCOUNT_NOT_ACTIVE: {
    title: "Аккаунт неактивен",
    message: "Активируйте его или выберите другой.",
  },
  USER_NOT_FOUND: {
    title: "Пользователь не найден",
    message: "Проверьте запрос.",
  },
  ACCOUNT_NOT_FOUND: {
    title: "Аккаунт не найден",
    message: "Возможно, он был удалён.",
  },
  ACCOUNT_MEMBER_NOT_FOUND: {
    title: "Участник не найден",
    message: "Состав команды уже мог измениться.",
  },
  ACTIVE_ACCOUNT_MEMBERSHIP_NOT_FOUND: {
    title: "Участие не найдено",
    message: "Проверьте активный аккаунт.",
  },
  ACCOUNT_INVITE_NOT_FOUND: {
    title: "Приглашение не найдено",
    message: "Проверьте код или создайте новое.",
  },
  ROLE_NOT_FOUND: {
    title: "Роль не найдена",
    message: "Список ролей уже мог измениться.",
  },
  ROLE_ASSIGNMENT_NOT_FOUND: {
    title: "Назначение не найдено",
    message: "Обновите список участников.",
  },
  BET_NOT_FOUND: {
    title: "Раунд не найден",
    message: "Возможно, он уже закрыт.",
  },
  PERSONALIZATION_NOT_FOUND: {
    title: "Работа не найдена",
    message: "Возможно, она была удалена.",
  },
  BADGE_NOT_FOUND: {
    title: "Бейдж не найден",
    message: "Обновите список бейджей.",
  },
  SANCTION_NOT_FOUND: {
    title: "Санкция не найдена",
    message: "Её состояние уже могло измениться.",
  },
  SUBSCRIPTION_NOT_FOUND: {
    title: "Подписка не найдена",
    message: "Обновите список подписок.",
  },
  CRISTALIX_PLAYER_NOT_FOUND: {
    title: "Игрок не найден",
    message: "Проверьте nickname.",
  },
  COLLECTOR_CREDENTIAL_NOT_FOUND: {
    title: "Ключ не найден",
    message: "Credential мог быть отозван.",
  },
  ENDPOINT_NOT_FOUND: {
    title: "Адрес не найден",
    message: "Обновите страницу.",
  },
  USER_IDENTITY_CONFLICT: {
    title: "Профиль уже связан",
    message: "Данные пользователя конфликтуют.",
  },
  ACCOUNT_IDENTITY_CONFLICT: {
    title: "Аккаунт уже привязан",
    message: "Используйте другой Cristalix профиль.",
  },
  ACCOUNT_DELETION_UNAVAILABLE: {
    title: "Сейчас нельзя удалить",
    message: "Состояние аккаунта блокирует удаление.",
  },
  ACCOUNT_MEMBER_LIMIT_REACHED: {
    title: "Команда заполнена",
    message: "Достигнут лимит участников.",
  },
  ACCOUNT_MEMBER_ALREADY_EXISTS: {
    title: "Участник уже добавлен",
    message: "Повторное действие не требуется.",
  },
  ACCOUNT_OWNER_CANNOT_LEAVE: {
    title: "Владелец не может выйти",
    message: "Сначала передайте управление.",
  },
  ACCOUNT_INVITE_UNAVAILABLE: {
    title: "Приглашение недоступно",
    message: "Оно уже использовано или просрочено.",
  },
  ROLE_ALREADY_ASSIGNED: {
    title: "Роль уже назначена",
    message: "Повторное действие не требуется.",
  },
  ROLE_HIERARCHY_VIOLATION: {
    title: "Порядок ролей конфликтует",
    message: "Измените позицию роли.",
  },
  ROLE_HIERARCHY_INCONSISTENT: {
    title: "Иерархия уже изменилась",
    message: "Обновите роли перед редактированием.",
  },
  DAILY_REWARD_ALREADY_CLAIMED: {
    title: "Сегодня уже получено",
    message: "Возвращайтесь завтра.",
  },
  DAILY_STREAK_NOT_RECOVERABLE: {
    title: "Серию нельзя восстановить",
    message: "Она не соответствует условиям восстановления.",
  },
  DAILY_STREAK_RESTORE_LIMIT_REACHED: {
    title: "Восстановления закончились",
    message: "Месячный лимит уже использован.",
  },
  BET_CONFLICT: {
    title: "Раунд уже изменился",
    message: "Обновите данные перед ставкой.",
  },
  PERSONALIZATION_ALREADY_OWNED: {
    title: "Она уже ваша",
    message: "Повторная покупка не требуется.",
  },
  PERSONALIZATION_NOT_PURCHASABLE: {
    title: "Сейчас не продаётся",
    message: "Статус персонализации изменился.",
  },
  PURCHASE_IDEMPOTENCY_CONFLICT: {
    title: "Запрос уже обрабатывался",
    message: "Повторного списания не будет.",
  },
  PERSONALIZATION_NOT_OWNED: {
    title: "Её нет в коллекции",
    message: "Сначала получите персонализацию.",
  },
  PERSONALIZATION_OWNERSHIP_CONFLICT: {
    title: "Коллекция уже изменилась",
    message: "Обновите список.",
  },
  BADGE_ALREADY_EXISTS: {
    title: "Такой бейдж уже есть",
    message: "Откройте существующий или измените данные.",
  },
  BADGE_NOT_OWNED: {
    title: "Бейдж ещё не ваш",
    message: "Его нельзя активировать.",
  },
  SANCTION_CONFLICT: {
    title: "Состояние уже изменилось",
    message: "Обновите данные пользователя.",
  },
  SELF_SANCTION: {
    title: "Нельзя применить санкцию к себе",
    message: "Выберите другого пользователя.",
  },
  WALLET_ADJUSTMENT_IDEMPOTENCY_CONFLICT: {
    title: "Коррекция уже обработана",
    message: "Повторная операция заблокирована.",
  },
  VALIDATION_FAILED: {
    title: "Проверьте поля",
    message: "Некоторые значения некорректны.",
  },
  CONSTRAINT_VIOLATION: {
    title: "Некорректные параметры",
    message: "Проверьте введённые значения.",
  },
  INVALID_REQUEST_BODY: {
    title: "Не удалось прочитать данные",
    message: "Повторите действие.",
  },
  INVALID_PARAMETER: {
    title: "Некорректный параметр",
    message: "Проверьте значение.",
  },
  MISSING_REQUEST_PARAMETER: {
    title: "Не хватает параметра",
    message: "Обновите форму и повторите действие.",
  },
  MISSING_REQUEST_HEADER: {
    title: "Запрос неполный",
    message: "Обновите страницу.",
  },
  INVALID_ROLE_HIERARCHY: {
    title: "Некорректный порядок ролей",
    message: "Проверьте позиции в иерархии.",
  },
  INVALID_ACCOUNT_INVITE_EXPIRATION: {
    title: "Некорректный срок",
    message: "Измените срок действия приглашения.",
  },
  SANCTION_DURATION_EXCEEDED: {
    title: "Слишком большой срок",
    message: "Сократите длительность санкции.",
  },
  METHOD_NOT_ALLOWED: {
    title: "Действие не поддерживается",
    message: "Обновите страницу.",
  },
  UNSUPPORTED_MEDIA_TYPE: {
    title: "Формат не поддерживается",
    message: "Повторите действие после обновления.",
  },
  NOT_ACCEPTABLE: {
    title: "Ответ недоступен",
    message: "Обновите страницу.",
  },
  PERSONALIZATION_PURCHASE_IN_PROGRESS: {
    title: "Покупка обрабатывается",
    message: "Не запускайте её повторно.",
  },
  ACCOUNT_UNAVAILABLE: {
    title: "Аккаунт недоступен",
    message: "Попробуйте немного позже.",
  },
  CRISTALIX_RATE_LIMIT_EXCEEDED: {
    title: "Слишком много запросов",
    message: "Подождите перед следующей попыткой.",
  },
  CRISTALIX_UNAVAILABLE: {
    title: "Сервис недоступен",
    message: "Попробуйте немного позже.",
  },
  CRISTALIX_AUTHORIZATION_FAILED: {
    title: "Соединение не авторизовано",
    message: "Mnemos повторит запрос позже.",
  },
  CRISTALIX_INVALID_RESPONSE: {
    title: "Получен некорректный ответ",
    message: "Попробуйте немного позже.",
  },
  REALTIME_CONNECTION_LIMIT_EXCEEDED: {
    title: "Слишком много подключений",
    message: "Закройте лишнее подключение или подождите.",
  },
  REALTIME_CONNECTION_CAPACITY_UNAVAILABLE: {
    title: "Realtime временно занят",
    message: "Подключение восстановится позже.",
  },
  INTERNAL_SERVER_ERROR: {
    title: "Что-то пошло не так",
    message: "Повторите действие немного позже.",
  },
} as const
