import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type {
  AccountIdentityDirectoryResponse,
  UserIdentityDirectoryResponse,
} from "@/shared/api/moderationApi"
import type { Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import { resolveModerationDirectoryCopy } from "./moderationDirectoryTranslations"
import { useIdentityDirectory } from "./useIdentityDirectory"

type DirectoryMode = "USERS" | "ACCOUNTS"

type Props = {
  mode: DirectoryMode
  navigate: (route: Route) => void
  back: () => void
}

export function ModerationDirectory({ mode, navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const directory = useIdentityDirectory()
  const copy = resolveModerationDirectoryCopy(i18n.language)
  const dateFormatter = new Intl.DateTimeFormat(i18n.language, {
    dateStyle: "medium",
    timeStyle: "short",
  })

  if (directory.mode !== mode) {
    directory.setMode(mode)
  }

  const totalResults = directory.pagination.totalElements
  const title = mode === "USERS" ? copy.userDirectory : copy.accountDirectory
  const placeholder = mode === "USERS" ? copy.userSearchPlaceholder : copy.accountSearchPlaceholder

  return (
    <div className="screen moderation-screen">
      <PageHeader title={title} back={back} />

      <label className="staff-search">
        <Glyph name="search" size={19} />
        <input
          autoFocus
          value={directory.query}
          maxLength={64}
          onChange={(event) => directory.setQuery(event.target.value)}
          placeholder={placeholder}
        />
        {directory.query ? (
          <button
            type="button"
            aria-label={t("moderation.clear")}
            onClick={() => directory.setQuery("")}
          >
            <Glyph name="close" size={14} />
          </button>
        ) : null}
      </label>

      <div className="moderation-directory-toolbar">
        <span>{title}</span>

        <div className="moderation-directory-sort" aria-label={copy.createdOrder}>
          <button
            type="button"
            className={directory.order === "NEWEST" ? "is-active" : ""}
            onClick={() => directory.setOrder("NEWEST")}
          >
            {copy.newest}
          </button>
          <button
            type="button"
            className={directory.order === "OLDEST" ? "is-active" : ""}
            onClick={() => directory.setOrder("OLDEST")}
          >
            {copy.oldest}
          </button>
        </div>
      </div>

      {directory.loading ? <FeatureLoading /> : null}

      {!directory.loading && directory.error ? (
        <p className="moderation-empty">{t("common.loadErrorTitle")}</p>
      ) : null}

      {!directory.loading && !directory.error && mode === "USERS" ? (
        <UserDirectoryResults
          users={directory.userPage?.content ?? []}
          total={totalResults}
          formatDate={(value) => dateFormatter.format(new Date(value))}
          navigate={navigate}
        />
      ) : null}

      {!directory.loading && !directory.error && mode === "ACCOUNTS" ? (
        <AccountDirectoryResults
          accounts={directory.accountPage?.content ?? []}
          total={totalResults}
          formatDate={(value) => dateFormatter.format(new Date(value))}
          navigate={navigate}
        />
      ) : null}

      {!directory.loading && !directory.error && totalResults === 0 ? (
        <p className="moderation-empty">{t("moderation.nothingFound")}</p>
      ) : null}

      {!directory.loading && !directory.error && directory.pagination.totalPages > 1 ? (
        <nav className="moderation-directory-pagination" aria-label={copy.pagination}>
          <button
            type="button"
            disabled={!directory.pagination.canGoPrevious}
            onClick={directory.previousPage}
          >
            ‹ {copy.previous}
          </button>
          <span>{copy.pageOf(directory.pagination.page + 1, directory.pagination.totalPages)}</span>
          <button
            type="button"
            disabled={!directory.pagination.canGoNext}
            onClick={directory.nextPage}
          >
            {copy.next} ›
          </button>
        </nav>
      ) : null}
    </div>
  )
}

type UserResultsProps = {
  users: UserIdentityDirectoryResponse[]
  total: number
  formatDate: (value: string) => string
  navigate: (route: Route) => void
}

function UserDirectoryResults({ users, total, formatDate, navigate }: UserResultsProps) {
  const { t } = useTranslation()

  if (!users.length) {
    return null
  }

  return (
    <section className="staff-search-results moderation-directory-results">
      <header>
        <strong>{t("moderation.results")}</strong>
        <small>{total}</small>
      </header>

      <div>
        {users.map((user) => (
          <motion.button
            type="button"
            key={user.userId}
            whileTap={{ scale: 0.985 }}
            onClick={() => navigate({ id: "moderation-user", params: { userId: user.userId } })}
          >
            <span className="staff-search-results__avatar staff-search-results__avatar--mnemos">
              <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" aria-hidden="true" />
            </span>
            <span>
              <strong>@{user.username}</strong>
              <small>
                {user.telegramId} · {formatDate(user.createdAt)}
              </small>
            </span>
            <span className="staff-search-results__accounts">
              {t("moderation.accountCount", { count: user.accountCount })}
            </span>
            <b>›</b>
          </motion.button>
        ))}
      </div>
    </section>
  )
}

type AccountResultsProps = {
  accounts: AccountIdentityDirectoryResponse[]
  total: number
  formatDate: (value: string) => string
  navigate: (route: Route) => void
}

function AccountDirectoryResults({ accounts, total, formatDate, navigate }: AccountResultsProps) {
  const { t } = useTranslation()

  if (!accounts.length) {
    return null
  }

  return (
    <section className="staff-search-results moderation-directory-results">
      <header>
        <strong>{t("moderation.results")}</strong>
        <small>{total}</small>
      </header>

      <div>
        {accounts.map((account) => (
          <motion.button
            type="button"
            key={account.accountId}
            whileTap={{ scale: 0.985 }}
            onClick={() =>
              navigate({ id: "moderation-account", params: { accountId: account.accountId } })
            }
          >
            <span className="staff-search-results__avatar moderation-directory-account-mark">C</span>
            <span>
              <strong>{account.cristalixNickname}</strong>
              <small>
                @{account.ownerUsername} · {formatDate(account.createdAt)}
              </small>
            </span>
            <span
              className={
                account.status === "ACTIVE"
                  ? "moderation-directory-status is-active"
                  : "moderation-directory-status"
              }
            >
              {account.status === "ACTIVE" ? t("moderation.active") : t("moderation.inactive")}
            </span>
            <b>›</b>
          </motion.button>
        ))}
      </div>
    </section>
  )
}
