package com.mnemos.telegram.bot.application.widget.wallet

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.port.out.AccountBalanceGateway
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.wallet.port.out.WalletTransactionGateway
import com.mnemos.telegram.bot.application.widget.wallet.port.out.WalletWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenWalletWidgetService(
    private val properties: TelegramWidgetProperties,
    private val accounts: AccountGateway,
    private val balances: AccountBalanceGateway,
    private val transactions: WalletTransactionGateway,
    private val publisher: WalletWidgetPublisher,
) : OpenWalletWidgetUseCase,
    WalletWidgetSnapshotQuery {
    override val enabled: Boolean
        get() = properties.enabled

    override fun execute(
        telegramId: Long,
        page: Int,
    ): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val snapshot = load(telegramId, page)) {
            is WalletWidgetSnapshotResult.Available -> publisher.publish(telegramId, snapshot.view)
            WalletWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(
        telegramId: Long,
        page: Int,
    ): WalletWidgetSnapshotResult {
        if (page !in 0..MAX_PAGE) return WalletWidgetSnapshotResult.Unavailable

        val context =
            when (val result = accounts.getContext(telegramId)) {
                is TelegramAccountContextResult.Available -> result.context
                TelegramAccountContextResult.RegistrationRequired -> null
                TelegramAccountContextResult.Unavailable -> return WalletWidgetSnapshotResult.Unavailable
            }
        val accountId = context?.activeAccountId
        if (accountId == null) {
            return WalletWidgetSnapshotResult.Available(
                WalletWidgetView(null, null, null, 0, 1, 0, WalletHistoryStatus.ACCOUNT_REQUIRED, emptyList()),
            )
        }

        val balance = (balances.getBalances(telegramId) as? TelegramAccountBalanceResult.Available)?.balance
        val result = transactions.list(telegramId, page)
        val historyStatus =
            when (result) {
                is WalletTransactionResult.Available -> WalletHistoryStatus.AVAILABLE
                WalletTransactionResult.AccountRequired -> WalletHistoryStatus.ACCOUNT_REQUIRED
                WalletTransactionResult.Forbidden -> WalletHistoryStatus.FORBIDDEN
                WalletTransactionResult.Unavailable -> WalletHistoryStatus.UNAVAILABLE
            }
        val history = (result as? WalletTransactionResult.Available)?.data
        if (history != null && history.accountId != accountId) {
            return WalletWidgetSnapshotResult.Unavailable
        }
        val latest = accounts.getContext(telegramId) as? TelegramAccountContextResult.Available
            ?: return WalletWidgetSnapshotResult.Unavailable
        if (latest.context.activeAccountId != accountId) {
            return WalletWidgetSnapshotResult.Unavailable
        }

        return WalletWidgetSnapshotResult.Available(
            WalletWidgetView(
                accountId = accountId,
                coin = balance?.coin,
                prime = balance?.prime,
                page = history?.page ?: 0,
                totalPages = history?.totalPages?.coerceAtLeast(1) ?: 1,
                totalTransactions = history?.totalItems ?: 0,
                historyStatus = historyStatus,
                entries = history?.entries.orEmpty(),
            ),
        )
    }

    private companion object {
        const val MAX_PAGE = 1_000
    }
}
