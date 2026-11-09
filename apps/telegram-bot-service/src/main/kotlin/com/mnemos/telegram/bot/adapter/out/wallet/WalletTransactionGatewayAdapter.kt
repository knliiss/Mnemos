package com.mnemos.telegram.bot.adapter.out.wallet

import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.application.widget.wallet.WalletTransactionEntry
import com.mnemos.telegram.bot.application.widget.wallet.WalletTransactionPage
import com.mnemos.telegram.bot.application.widget.wallet.WalletTransactionResult
import com.mnemos.telegram.bot.application.widget.wallet.port.out.WalletTransactionGateway
import feign.FeignException
import org.springframework.stereotype.Component

@Component
class WalletTransactionGatewayAdapter(
    private val client: WalletServiceClient,
) : WalletTransactionGateway {
    override fun list(
        telegramId: Long,
        page: Int,
    ): WalletTransactionResult {
        if (page !in 0..MAX_PAGE) return WalletTransactionResult.Unavailable

        return try {
            val response = client.getActiveAccountTransactions(telegramId, page, PAGE_SIZE)
                ?: return WalletTransactionResult.AccountRequired
            WalletTransactionResult.Available(
                WalletTransactionPage(
                    accountId = response.accountId,
                    entries =
                        response.items.map { item ->
                            WalletTransactionEntry(
                                label = WidgetTextLimiter.truncateUtf8(
                                    item.type.name.replace('_', ' ') + " · " + item.source.name.replace('_', ' '),
                                    48,
                                ),
                                amount = item.amount,
                                currency = item.currency.name,
                                createdAt = item.createdAt.toString().take(16).replace('T', ' '),
                            )
                        },
                    page = response.page,
                    totalPages = response.totalPages,
                    totalItems = response.totalItems,
                ),
            )
        } catch (exception: FeignException) {
            when (exception.status()) {
                403 -> WalletTransactionResult.Forbidden
                404 -> WalletTransactionResult.AccountRequired
                else -> WalletTransactionResult.Unavailable
            }
        } catch (_: RuntimeException) {
            WalletTransactionResult.Unavailable
        }
    }

    private companion object {
        const val PAGE_SIZE = 4
        const val MAX_PAGE = 1_000
    }
}
