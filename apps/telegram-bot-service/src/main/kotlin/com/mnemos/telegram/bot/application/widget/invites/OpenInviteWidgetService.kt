package com.mnemos.telegram.bot.application.widget.invites

import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteListResult
import com.mnemos.telegram.bot.application.account.invite.port.out.AccountInviteGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.invites.port.out.InviteWidgetPublisher
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenInviteWidgetService(
    private val properties: TelegramWidgetProperties,
    private val gateway: AccountInviteGateway,
    private val publisher: InviteWidgetPublisher,
) : OpenInviteWidgetUseCase, InviteWidgetSnapshotQuery {
    override val enabled: Boolean get() = properties.enabled

    override fun execute(telegramId: Long, page: Int): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val snapshot = load(telegramId, page)) {
            is InviteWidgetSnapshotResult.Available -> publisher.publish(telegramId, snapshot.view)
            InviteWidgetSnapshotResult.AccountRequired ->
                TelegramDeliveryResult.PermanentFailure("active-account-required")
            InviteWidgetSnapshotResult.Forbidden -> TelegramDeliveryResult.PermanentFailure("invites-forbidden")
            InviteWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(telegramId: Long, page: Int): InviteWidgetSnapshotResult {
        if (page !in 0..1_000) return InviteWidgetSnapshotResult.Unavailable
        return when (val result = gateway.getInvites(telegramId)) {
            is TelegramAccountInviteListResult.Available -> {
                val snapshot = result.snapshot
                val ordered = snapshot.invites.sortedWith(
                    compareByDescending<com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInvite> { it.createdAt }
                        .thenBy { it.id },
                )
                val totalPages = maxOf(1, (ordered.size + PAGE_SIZE - 1) / PAGE_SIZE)
                val actualPage = page.coerceAtMost(totalPages - 1)
                InviteWidgetSnapshotResult.Available(
                    InviteWidgetView(
                        accountId = snapshot.accountId,
                        accountName = WidgetTextLimiter.truncateUtf8(snapshot.cristalixNickname, 36),
                        invites = ordered.drop(actualPage * PAGE_SIZE).take(PAGE_SIZE).map {
                            InviteWidgetItem(it.id, it.code, it.status, it.expiresAt)
                        },
                        totalInvites = ordered.size,
                        page = actualPage,
                        totalPages = totalPages,
                    ),
                )
            }
            TelegramAccountInviteListResult.ActiveAccountRequired -> InviteWidgetSnapshotResult.AccountRequired
            TelegramAccountInviteListResult.Forbidden -> InviteWidgetSnapshotResult.Forbidden
            TelegramAccountInviteListResult.Unavailable -> InviteWidgetSnapshotResult.Unavailable
        }
    }

    private companion object {
        const val PAGE_SIZE = 3
    }
}
