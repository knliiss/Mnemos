type ModerationDirectoryCopy = {
  searchMode: string
  usersMode: string
  accountsMode: string
  userSearchPlaceholder: string
  accountSearchPlaceholder: string
  userDirectory: string
  accountDirectory: string
  createdOrder: string
  newest: string
  oldest: string
  pagination: string
  previous: string
  next: string
  pageOf: (page: number, total: number) => string
  activateAccount: string
  deactivateAccount: string
  changingAccountStatus: string
}

const copyByLanguage: Record<"en" | "ru" | "uk", ModerationDirectoryCopy> = {
  en: {
    searchMode: "Directory mode",
    usersMode: "Users",
    accountsMode: "Accounts",
    userSearchPlaceholder: "Username, Telegram ID or User ID",
    accountSearchPlaceholder: "Cristalix nickname, Account ID or UUID",
    userDirectory: "Mnemos users",
    accountDirectory: "Cristalix accounts",
    createdOrder: "Created date order",
    newest: "Newest",
    oldest: "Oldest",
    pagination: "Directory pages",
    previous: "Previous",
    next: "Next",
    pageOf: (page, total) => `Page ${page} of ${total}`,
    activateAccount: "Activate",
    deactivateAccount: "Deactivate",
    changingAccountStatus: "Saving…",
  },
  ru: {
    searchMode: "Режим каталога",
    usersMode: "Пользователи",
    accountsMode: "Аккаунты",
    userSearchPlaceholder: "Username, Telegram ID или User ID",
    accountSearchPlaceholder: "Cristalix nickname, Account ID или UUID",
    userDirectory: "Пользователи Mnemos",
    accountDirectory: "Cristalix аккаунты",
    createdOrder: "Сортировка по дате создания",
    newest: "Новые",
    oldest: "Старые",
    pagination: "Страницы каталога",
    previous: "Назад",
    next: "Дальше",
    pageOf: (page, total) => `Страница ${page} из ${total}`,
    activateAccount: "Активировать",
    deactivateAccount: "Деактивировать",
    changingAccountStatus: "Сохраняем…",
  },
  uk: {
    searchMode: "Режим каталогу",
    usersMode: "Користувачі",
    accountsMode: "Аккаунти",
    userSearchPlaceholder: "Username, Telegram ID або User ID",
    accountSearchPlaceholder: "Cristalix nickname, Account ID або UUID",
    userDirectory: "Користувачі Mnemos",
    accountDirectory: "Cristalix аккаунти",
    createdOrder: "Сортування за датою створення",
    newest: "Нові",
    oldest: "Старі",
    pagination: "Сторінки каталогу",
    previous: "Назад",
    next: "Далі",
    pageOf: (page, total) => `Сторінка ${page} з ${total}`,
    activateAccount: "Активувати",
    deactivateAccount: "Деактивувати",
    changingAccountStatus: "Зберігаємо…",
  },
}

export function resolveModerationDirectoryCopy(language: string): ModerationDirectoryCopy {
  const normalizedLanguage = language.toLowerCase().split("-")[0]

  if (normalizedLanguage === "ru" || normalizedLanguage === "uk") {
    return copyByLanguage[normalizedLanguage]
  }

  return copyByLanguage.en
}
