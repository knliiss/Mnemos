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
  overviewTab: string
  sanctionsTab: string
  walletTab: string
  membersTab: string
  rolesTab: string
  transactionsTab: string
  personalizationsTab: string
  settingsTab: string
  userOverview: string
  userOverviewHint: string
  accountOverview: string
  accountOverviewHint: string
  accountSettings: string
  accountSettingsHint: string
  activateAccount: string
  deactivateAccount: string
  changingAccountStatus: string
  activeAccount: string
  deactivatedAccount: string
  deletedAccount: string
  manageAccount: string
  closeAccount: string
  accountMembers: string
  addMember: string
  addingMember: string
  userIdPlaceholder: string
  transferOwner: string
  removeMember: string
  owner: string
  irreversibleDelete: string
  deleteWarning: string
  confirmDelete: string
  cancel: string
  loadingAccount: string
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
    overviewTab: "Overview",
    sanctionsTab: "Access",
    walletTab: "Wallet",
    membersTab: "Members",
    rolesTab: "Roles",
    transactionsTab: "Transactions",
    personalizationsTab: "Personalizations",
    settingsTab: "Settings",
    userOverview: "User overview",
    userOverviewHint: "Identity, linked Cristalix accounts and current moderation state.",
    accountOverview: "Account overview",
    accountOverviewHint: "Identity, ownership, membership capacity and lifecycle state.",
    accountSettings: "Account settings",
    accountSettingsHint: "Lifecycle, ownership and irreversible account operations.",
    activateAccount: "Activate",
    deactivateAccount: "Deactivate",
    changingAccountStatus: "Saving…",
    activeAccount: "Active",
    deactivatedAccount: "Deactivated",
    deletedAccount: "Deleted",
    manageAccount: "Manage",
    closeAccount: "Close",
    accountMembers: "Account members",
    addMember: "Add member",
    addingMember: "Adding…",
    userIdPlaceholder: "Mnemos User ID",
    transferOwner: "Transfer owner",
    removeMember: "Remove",
    owner: "Owner",
    irreversibleDelete: "Delete account permanently",
    deleteWarning:
      "This creates an irreversible tombstone. The historical record remains in Mnemos and cannot be reactivated.",
    confirmDelete: "Confirm permanent deletion",
    cancel: "Cancel",
    loadingAccount: "Loading account…",
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
    overviewTab: "Обзор",
    sanctionsTab: "Доступ",
    walletTab: "Кошелёк",
    membersTab: "Участники",
    rolesTab: "Роли",
    transactionsTab: "Операции",
    personalizationsTab: "Персонализации",
    settingsTab: "Настройки",
    userOverview: "Обзор пользователя",
    userOverviewHint: "Профиль Mnemos, связанные Cristalix аккаунты и текущее состояние модерации.",
    accountOverview: "Обзор аккаунта",
    accountOverviewHint: "Идентификаторы, владелец, участники и текущее состояние аккаунта.",
    accountSettings: "Настройки аккаунта",
    accountSettingsHint: "Статус, передача владельца и необратимые операции вынесены отдельно.",
    activateAccount: "Активировать",
    deactivateAccount: "Деактивировать",
    changingAccountStatus: "Сохраняем…",
    activeAccount: "Активен",
    deactivatedAccount: "Деактивирован",
    deletedAccount: "Удалён",
    manageAccount: "Управление",
    closeAccount: "Закрыть",
    accountMembers: "Участники аккаунта",
    addMember: "Добавить участника",
    addingMember: "Добавляем…",
    userIdPlaceholder: "Mnemos User ID",
    transferOwner: "Передать владельца",
    removeMember: "Удалить",
    owner: "Владелец",
    irreversibleDelete: "Удалить аккаунт навсегда",
    deleteWarning:
      "Операция необратима. Историческая запись останется в Mnemos со статусом DELETED и больше не сможет быть активирована.",
    confirmDelete: "Подтвердить удаление навсегда",
    cancel: "Отмена",
    loadingAccount: "Загружаем аккаунт…",
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
    overviewTab: "Огляд",
    sanctionsTab: "Доступ",
    walletTab: "Гаманець",
    membersTab: "Учасники",
    rolesTab: "Ролі",
    transactionsTab: "Операції",
    personalizationsTab: "Персоналізації",
    settingsTab: "Налаштування",
    userOverview: "Огляд користувача",
    userOverviewHint: "Профіль Mnemos, пов'язані Cristalix аккаунти та поточний стан модерації.",
    accountOverview: "Огляд аккаунта",
    accountOverviewHint: "Ідентифікатори, власник, учасники та поточний стан аккаунта.",
    accountSettings: "Налаштування аккаунта",
    accountSettingsHint: "Статус, передавання власника та незворотні операції винесені окремо.",
    activateAccount: "Активувати",
    deactivateAccount: "Деактивувати",
    changingAccountStatus: "Зберігаємо…",
    activeAccount: "Активний",
    deactivatedAccount: "Деактивований",
    deletedAccount: "Видалений",
    manageAccount: "Керування",
    closeAccount: "Закрити",
    accountMembers: "Учасники аккаунта",
    addMember: "Додати учасника",
    addingMember: "Додаємо…",
    userIdPlaceholder: "Mnemos User ID",
    transferOwner: "Передати власника",
    removeMember: "Видалити",
    owner: "Власник",
    irreversibleDelete: "Видалити аккаунт назавжди",
    deleteWarning:
      "Операція незворотна. Історичний запис залишиться в Mnemos зі статусом DELETED і більше не зможе бути активований.",
    confirmDelete: "Підтвердити остаточне видалення",
    cancel: "Скасувати",
    loadingAccount: "Завантажуємо аккаунт…",
  },
}

export function resolveModerationDirectoryCopy(language: string): ModerationDirectoryCopy {
  const normalizedLanguage = language.toLowerCase().split("-")[0]

  if (normalizedLanguage === "ru" || normalizedLanguage === "uk") {
    return copyByLanguage[normalizedLanguage]
  }

  return copyByLanguage.en
}
