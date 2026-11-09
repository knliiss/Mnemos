package com.mnemos.telegram.bot.application.widget.accounts

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountStatus
import com.mnemos.telegram.bot.application.account.port.out.AccountBalanceGateway
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.accounts.port.out.AccountWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenAccountWidgetService(
    private val properties: TelegramWidgetProperties,
    private val accounts: AccountGateway,
    private val balances: AccountBalanceGateway,
    private val publisher: AccountWidgetPublisher,
) : OpenAccountWidgetUseCase,
    AccountWidgetSnapshotQuery {
    override val enabled: Boolean
        get() = properties.enabled

    override fun execute(
        telegramId: Long,
        page: Int,
    ): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")

        return when (val snapshot = load(telegramId, page)) {
            is AccountWidgetSnapshotResult.Available -> publisher.publish(telegramId, snapshot.view)
            AccountWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(
        telegramId: Long,
        page: Int,
    ): AccountWidgetSnapshotResult {
        if (page !in 0..1_000) return AccountWidgetSnapshotResult.Unavailable

        val context =
            when (val result = accounts.getContext(telegramId)) {
                is TelegramAccountContextResult.Available -> result.context
                TelegramAccountContextResult.RegistrationRequired -> null
                TelegramAccountContextResult.Unavailable -> return AccountWidgetSnapshotResult.Unavailable
            }

        val all = context?.accounts.orEmpty()
        val pages = if (all.isEmpty()) 1 else (all.size - 1) / PAGE_SIZE + 1
        val actualPage = page.coerceAtMost(pages - 1)
        val activeId = context?.activeAccountId
        val balance =
            if (activeId == null) {
                null
            } else {
                (balances.getBalances(telegramId) as? TelegramAccountBalanceResult.Available)?.balance
            }

        return AccountWidgetSnapshotResult.Available(
            AccountWidgetView(
                activeAccountId = activeId,
                accounts =
                    all.drop(actualPage * PAGE_SIZE).take(PAGE_SIZE).map { account ->
                        AccountWidgetEntry(
                            accountId = account.accountId,
                            nickname = account.cristalixNickname.take(MAX_NICKNAME_LENGTH),
                            owner = account.owner,
                            memberCount = account.memberCount,
                            maxMembers = account.maxMembers,
                            active = account.accountId == activeId,
                            enabled = account.status == TelegramAccountStatus.ACTIVE,
                        )
                    },
                totalAccounts = all.size,
                page = actualPage,
                totalPages = pages,
                coin = balance?.coin,
                prime = balance?.prime,
            ),
        )
    }

    private companion object {
        const val PAGE_SIZE = 4
        const val MAX_NICKNAME_LENGTH = 24
    }
}
