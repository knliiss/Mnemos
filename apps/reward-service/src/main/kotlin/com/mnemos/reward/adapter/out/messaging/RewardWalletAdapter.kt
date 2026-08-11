package com.mnemos.reward.adapter.out.messaging

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.reward.application.daily.port.out.RewardWallet
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Component
class RewardWalletAdapter(
    private val publisher: IntegrationEventPublisher,
) : RewardWallet {
    override fun creditDaily(
        accountId: AccountId,
        currency: Currency,
        amount: Long,
        rewardDate: LocalDate,
    ) {
        val sourceReferenceId = UuidCreator.getNameBasedSha1("reward:daily:${accountId.id}:$rewardDate")
        publish(accountId, currency, amount, TransactionSource.DAILY_REWARD, sourceReferenceId, "${currency.name.lowercase()}:$rewardDate")
    }

    override fun creditItemDrop(
        accountId: AccountId,
        amount: Long,
        eventId: UUID,
    ) {
        publish(accountId, Currency.COIN, amount, TransactionSource.ITEM_DROP_REWARD, eventId, eventId.toString())
    }

    private fun publish(
        accountId: AccountId,
        currency: Currency,
        amount: Long,
        source: TransactionSource,
        sourceReferenceId: UUID,
        discriminator: String,
    ) {
        val requestId = UuidCreator.getNameBasedSha1("reward:${accountId.id}:${source.name}:$discriminator")
        val command =
            WalletCreateTransactionCommand(requestId, accountId.id, currency, TransactionType.CREDIT, source, sourceReferenceId, amount)
        publisher.publish(IntegrationEvent(WalletCommandType.CREATE_TRANSACTION, requestId, Instant.now(), command))
    }
}
