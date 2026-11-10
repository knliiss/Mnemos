package com.mnemos.social.adapter.out.persistence.comment

import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.comment.telegram.TelegramCommentClaim
import com.mnemos.social.application.comment.telegram.TelegramCommentReceiptPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JdbcTelegramCommentReceiptAdapter(
    private val jdbc: JdbcTemplate,
) : TelegramCommentReceiptPort {
    override fun claim(
        operationId: UUID,
        telegramId: Long,
        actor: SocialActor,
        targetAccountId: UUID,
        payloadHash: String,
    ): TelegramCommentClaim {
        val inserted = jdbc.update(
            """
            INSERT INTO telegram_comment_receipts
                (operation_id, telegram_id, actor_user_id, actor_account_id, target_account_id, request_hash)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (operation_id) DO NOTHING
            """.trimIndent(),
            operationId,
            telegramId,
            actor.userId,
            actor.accountId,
            targetAccountId,
            payloadHash,
        )
        if (inserted == 1) return TelegramCommentClaim.Claimed

        val existing = jdbc.query(
            """
            SELECT telegram_id, actor_user_id, actor_account_id, target_account_id, request_hash, comment_id
            FROM telegram_comment_receipts
            WHERE operation_id = ?
            """.trimIndent(),
            { rs, _ ->
                Receipt(
                    telegramId = rs.getLong("telegram_id"),
                    actorUserId = rs.getObject("actor_user_id", UUID::class.java),
                    actorAccountId = rs.getObject("actor_account_id", UUID::class.java),
                    targetAccountId = rs.getObject("target_account_id", UUID::class.java),
                    payloadHash = rs.getString("request_hash").trim(),
                    commentId = rs.getObject("comment_id", UUID::class.java),
                )
            },
            operationId,
        ).singleOrNull() ?: return TelegramCommentClaim.Conflict

        if (existing.telegramId != telegramId ||
            existing.actorUserId != actor.userId ||
            existing.actorAccountId != actor.accountId ||
            existing.targetAccountId != targetAccountId ||
            existing.payloadHash != payloadHash
        ) return TelegramCommentClaim.Conflict
        return existing.commentId?.let(TelegramCommentClaim::Replayed) ?: TelegramCommentClaim.Conflict
    }

    override fun complete(operationId: UUID, commentId: UUID) {
        check(
            jdbc.update(
                "UPDATE telegram_comment_receipts SET comment_id = ? WHERE operation_id = ? AND comment_id IS NULL",
                commentId,
                operationId,
            ) == 1,
        ) { "Cannot complete Telegram comment receipt" }
    }

    private data class Receipt(
        val telegramId: Long,
        val actorUserId: UUID,
        val actorAccountId: UUID,
        val targetAccountId: UUID,
        val payloadHash: String,
        val commentId: UUID?,
    )
}
