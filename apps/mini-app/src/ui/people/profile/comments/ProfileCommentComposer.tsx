import { AnimatePresence, motion } from "framer-motion"
import type { FormEvent, RefObject } from "react"
import { useTranslation } from "react-i18next"
import { Glyph } from "../../../shared/MnemosIcon"
import type { ReplyTarget } from "../model"
import { ProfileCommentAvatar } from "./ProfileCommentAvatar"
import type { ProfileCommentEditingTarget } from "./useProfileCommentComposer"

type ProfileCommentComposerProps = {
  activeAccountNickname: string
  draft: string
  replyingTo: ReplyTarget | null
  editing: ProfileCommentEditingTarget | null
  inputRef: RefObject<HTMLInputElement | null>
  busy: boolean
  onDraftChange(value: string): void
  onReset(): void
  onSubmit(): Promise<void>
}

export function ProfileCommentComposer({
  activeAccountNickname,
  draft,
  replyingTo,
  editing,
  inputRef,
  busy,
  onDraftChange,
  onReset,
  onSubmit,
}: ProfileCommentComposerProps) {
  const { t } = useTranslation()

  const submit = (event: FormEvent) => {
    event.preventDefault()
    void onSubmit()
  }

  return (
    <div className="public-profile-comments-sheet__composer-wrap">
      <AnimatePresence initial={false}>
        {replyingTo || editing ? (
          <motion.div
            className="public-profile-reply-context"
            initial={{ opacity: 0, y: 4 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 4 }}
          >
            <span>
              {editing ? t("people.profile.edit") : t("people.profile.replyingTo")}{" "}
              <strong>@{editing?.comment.author ?? replyingTo?.author}</strong>
            </span>
            <button
              type="button"
              onClick={onReset}
              aria-label={editing ? t("people.profile.edit") : t("people.profile.cancelReply")}
            >
              <Glyph name="close" size={12} />
            </button>
          </motion.div>
        ) : null}
      </AnimatePresence>

      <form className="public-profile-composer" onSubmit={submit}>
        <ProfileCommentAvatar author={activeAccountNickname} compact />

        <input
          ref={inputRef}
          value={draft}
          maxLength={500}
          disabled={busy}
          onChange={(event) => onDraftChange(event.target.value)}
          placeholder={
            editing
              ? t("people.profile.edit")
              : replyingTo
                ? t("people.profile.replyPlaceholder", {
                    author: replyingTo.author,
                  })
                : t("people.profile.writePlaceholder")
          }
        />

        <motion.button
          type="submit"
          whileTap={{ scale: 0.94 }}
          disabled={!draft.trim() || busy}
          aria-label={editing ? t("people.profile.save") : t("people.profile.send")}
        >
          <Glyph name={editing ? "check" : "send"} size={15} />
        </motion.button>
      </form>
    </div>
  )
}
