import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type { AccountReactionType, PublicProfileResponse } from "@/shared/api/profileSocialApi"
import { CristalixHead } from "../../../components/profile/CristalixHead"
import { Glyph } from "../../../shared/MnemosIcon"
import {
  nicknameStyle,
  profileCardClassName,
  profileCardStyle,
  profileHeadClassName,
  profileHeadStyle,
  profileSkinUrl,
} from "../profilePresentation"
import "../public-profile-personalization.css"

type PublicProfileSummaryProps = {
  profile: PublicProfileResponse
  reaction: AccountReactionType | null
  dropsCount: number
  reactionBusy: boolean
  canReact: boolean
  onToggleReaction(reaction: AccountReactionType): Promise<void>
  onOpenComments(): void
}

export function PublicProfileSummary({
  profile,
  reaction,
  dropsCount,
  reactionBusy,
  canReact,
  onToggleReaction,
  onOpenComments,
}: PublicProfileSummaryProps) {
  const { t } = useTranslation()
  const reputation = profile.likes - profile.dislikes

  return (
    <motion.section
      className={profileCardClassName(profile)}
      style={profileCardStyle(profile)}
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
    >
      {profile.appearance.profileEffect ? (
        <span className="public-profile-effect" aria-hidden="true">
          <i />
          <i />
          <i />
          <i />
          <i />
          <i />
        </span>
      ) : null}

      <div className="public-profile-card__main">
        <div className={profileHeadClassName(profile)} style={profileHeadStyle(profile)}>
          <CristalixHead skinUrl={profileSkinUrl(profile.cristalixUuid)} size={78} />
          <span className="public-profile-presence">
            <i />
            CRISTALIX
          </span>
        </div>

        <div className="public-profile-identity">
          <small>{t("people.profile.accountCard")}</small>
          <strong style={nicknameStyle(profile)}>{profile.cristalixNickname}</strong>
          <span>{profile.cristalixUuid.slice(0, 8)}</span>
        </div>

        <div
          className={`public-profile-reactions ${canReact ? "" : "is-readonly"}`}
          aria-label={t("people.profile.reactionAria")}
        >
          <motion.button
            type="button"
            className={reaction === "LIKE" ? "is-active" : ""}
            whileTap={canReact ? { scale: 0.95 } : undefined}
            disabled={!canReact || reactionBusy}
            aria-label={reaction === "LIKE" ? t("people.profile.unlike") : t("people.profile.like")}
            onClick={() => void onToggleReaction("LIKE")}
          >
            <Glyph name="like" size={15} />
            <b>{profile.likes}</b>
          </motion.button>

          <motion.button
            type="button"
            className={reaction === "DISLIKE" ? "is-active is-negative" : ""}
            whileTap={canReact ? { scale: 0.95 } : undefined}
            disabled={!canReact || reactionBusy}
            aria-label={
              reaction === "DISLIKE"
                ? t("people.profile.removeDislike")
                : t("people.profile.dislike")
            }
            onClick={() => void onToggleReaction("DISLIKE")}
          >
            <Glyph name="dislike" size={15} />
            <b>{profile.dislikes}</b>
          </motion.button>
        </div>
      </div>

      <div className="public-profile-metrics">
        <article>
          <span className="public-profile-metric-icon">
            <Glyph name="events" size={14} />
          </span>
          <span>
            <small>{t("people.profile.drops")}</small>
            <strong>{dropsCount}</strong>
          </span>
        </article>

        <button type="button" onClick={onOpenComments}>
          <span className="public-profile-metric-icon">
            <Glyph name="chat" size={14} />
          </span>
          <span>
            <small>{t("people.profile.comments")}</small>
            <strong>{profile.comments}</strong>
          </span>
        </button>

        <article>
          <span className="public-profile-metric-icon is-reputation">
            <Glyph name="like" size={14} />
          </span>
          <span>
            <small>{t("people.profile.reputation")}</small>
            <strong>{reputation}</strong>
          </span>
        </article>
      </div>
    </motion.section>
  )
}
