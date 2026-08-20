import type {
  BetItemType,
  BoosterType,
  DungeonType,
  EventBetRoundStatus,
  EventKind,
  GlobalEventType,
  PersonalizationStatus,
  PersonalizationType,
  RolePermission,
  SanctionType,
  TransactionSource,
} from "./model"

export const eventLabels: Record<EventKind, string> = {
  ITEM_DROP: "Дроп",
  BOOSTER: "Бустер",
  GLOBAL: "Глобальна",
  RAID: "Рейд",
}

export const personalizationLabels: Record<PersonalizationType, string> = {
  NOTIFICATION_PACK: "Сповіщення",
  NICK_STYLE: "Нік",
  PROFILE_THEME: "Тема",
  AVATAR_FRAME: "Рамка",
  PROFILE_EFFECT: "Ефект",
}

export const personalizationStatusLabels: Record<PersonalizationStatus, string> = {
  DRAFT: "Чернетка",
  PRIVATE: "Приватна",
  IN_REVIEW: "Перевірка",
  REJECTED: "Відхилено",
  APPROVED: "Схвалено",
  PUBLISHED: "Опубліковано",
  UNLISTED: "Приховано",
}

export const betStatusLabels: Record<EventBetRoundStatus, string> = {
  OPEN: "Відкрито",
  LOCKED: "Закрито",
  AWAITING_RESULT: "Очікує результат",
  AWAITING_ADMIN_CONFIRMATION: "Очікує підтвердження",
  SETTLED: "Завершено",
}

export const boosterLabels: Record<BoosterType, string> = {
  LUCK: "Удача",
  MONEY: "Монети",
  DAMAGE: "Шкода",
  POWER: "Сила",
}

export const dungeonLabels: Record<DungeonType, string> = {
  EASY: "Легкий",
  MEDIUM: "Середній",
  HARD: "Складний",
  EXPERT: "Експертний",
}

export const globalEventLabels: Record<GlobalEventType, string> = {
  DARKNESS: "Темрява",
  MOON: "Місяць",
  ECLIPSE: "Затемнення",
  EXPLOSION: "Вибух",
}

export const itemTypeLabels: Record<BetItemType, string> = {
  SWORD: "Меч",
  AURA: "Аура",
  PET: "Пет",
  BOOK: "Книга",
  JEWELRY: "Прикраса",
}

export const sanctionLabels: Record<SanctionType, string> = {
  BAN: "Бан",
  NOTIFICATION_SUSPENSION: "Сповіщення",
}

export const transactionSourceLabels: Record<TransactionSource, string> = {
  DAILY_REWARD: "Щоденна нагорода",
  ITEM_DROP_REWARD: "Нагорода за дроп",
  BET_PLACE: "Ставка",
  BET_WIN: "Виграш",
  BET_REFUND: "Повернення ставки",
  PERSONALIZATION_PURCHASE: "Персоналізація",
  CREATOR_REVENUE: "Дохід автора",
  SPOTLIGHT_PURCHASE: "Алерт дропа",
  PRIME_PURCHASE: "PRIME",
  ADMIN_ADJUSTMENT: "Коригування",
}

export const permissionLabels: Record<RolePermission, string> = {
  VIEW_ACCOUNT_SETTINGS: "Перегляд",
  EDIT_ACCOUNT_SETTINGS: "Редагування",
  MANAGE_ACCOUNT_INVITES: "Керування інвайтами",
  VIEW_MEMBERS: "Перегляд",
  INVITE_MEMBERS: "Запрошувати",
  REMOVE_MEMBERS: "Видаляти",
  ASSIGN_MEMBER_ROLES: "Призначати ролі",
  KICK_MEMBERS: "Кікати",
  VIEW_ROLES: "Перегляд",
  CREATE_ROLES: "Створення",
  EDIT_ROLE_DETAILS: "Назва й колір",
  EDIT_ROLE_PERMISSIONS: "Дозволи",
  DELETE_ROLES: "Видалення",
  MANAGE_ROLE_HIERARCHY: "Порядок",
  MANAGE_ROLES: "Керування",
  VIEW_WALLET: "Перегляд",
  VIEW_TRANSACTION_HISTORY: "Історія",
  SPEND_WALLET_FUNDS: "Витрати",
  CLAIM_DAILY_REWARD: "Щоденна нагорода",
  PUBLISH_COMMENTS: "Коментарі",
  PUBLISH_REACTIONS: "Реакції",
  MODERATE_COMMENTS: "Модерація коментарів",
  VIEW_BETS: "Перегляд",
  PLACE_BETS: "Ставити",
  MANAGE_BETS: "Керування",
  VIEW_BET_HISTORY: "Історія",
  VIEW_PERSONALIZATIONS: "Перегляд",
  APPLY_PERSONALIZATIONS: "Активувати",
  BUY_PERSONALIZATIONS: "Купувати",
  MANAGE_PERSONALIZATIONS: "Керування",
}
