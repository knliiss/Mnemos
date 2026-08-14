package com.mnemos.reward.adapter.out.messaging

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.reward.application.daily.port.out.RewardWallet
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Component
class RewardWalletAdapter(
    private val publisher: IntegrationEventPublisher,
) : RewardWallet {
    override fun creditDailyCoin(
        accountId: AccountId,
        amount: Long,
        rewardDate: LocalDate,
    ) {
        publish(
            owner = WalletOwner(type = WalletOwnerType.ACCOUNT, id = accountId.id),
            currency = Currency.COIN,
            amount = amount,
            source = TransactionSource.DAILY_REWARD,
            sourceReferenceId = UuidCreator.getNameBasedSha1("reward:daily:account:${accountId.id}:$rewardDate"),
            discriminator = "account:${accountId.id}:coin:$rewardDate",
        )
    }

    override fun creditDailyPrime(
        userId: UserId,
        amount: Long,
        rewardDate: LocalDate,
    ) {
        publish(
            owner = WalletOwner(type = WalletOwnerType.USER, id = userId.id),
            currency = Currency.PRIME,
            amount = amount,
            source = TransactionSource.DAILY_REWARD,
            sourceReferenceId = UuidCreator.getNameBasedSha1("reward:daily:user:${userId.id}:$rewardDate"),
            discriminator = "user:${userId.id}:prime:$rewardDate",
        )
    }

    override fun creditItemDrop(
        accountId: AccountId,
        amount: Long,
        eventId: UUID,
    ) {
        publish(
            owner = WalletOwner(type = WalletOwnerType.ACCOUNT, id = accountId.id),
            currency = Currency.COIN,
            amount = amount,
            source = TransactionSource.ITEM_DROP_REWARD,
            sourceReferenceId = eventId,
            discriminator = "account:${accountId.id}:item-drop:$eventId",
        )
    }

    private fun publish(
        owner: WalletOwner,
        currency: Currency,
        amount: Long,
        source: TransactionSource,
        sourceReferenceId: UUID,
        discriminator: String,
    ) {
        val requestId = UuidCreator.getNameBasedSha1("reward:${source.name}:$discriminator")
        val command =
            WalletCreateTransactionCommand(
                requestId = requestId,
                owner = owner,
                currency = currency,
                type = TransactionType.CREDIT,
                source = source,
                sourceReferenceId = sourceReferenceId,
                amount = amount,
            )

        publisher.publish(
            IntegrationEvent(
                type = WalletCommandType.CREATE_TRANSACTION,
                aggregateId = requestId,
                occurredAt = Instant.now(),
                payload = command,
            ),
        )
    }
}
