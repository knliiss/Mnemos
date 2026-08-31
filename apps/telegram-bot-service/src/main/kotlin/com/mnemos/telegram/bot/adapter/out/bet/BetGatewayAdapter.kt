package com.mnemos.telegram.bot.adapter.out.bet

import com.mnemos.telegram.bot.adapter.out.bet.BetApiMapper.toModel
import com.mnemos.telegram.bot.application.bet.model.TelegramBetHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetMutationResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import com.mnemos.telegram.bot.application.bet.port.out.BetGateway
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramBetSide as ContractBetSide
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramIncreaseEventBetRequest
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramPlaceEventBetRequest
import feign.FeignException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BetGatewayAdapter(
    private val client: BetServiceClient,
) : BetGateway {
    override fun listRounds(telegramId: Long): TelegramBetRoundsResult =
        try {
            TelegramBetRoundsResult.Available(
                client.listRounds(telegramId).map { round -> round.toModel() },
            )
        } catch (exception: FeignException) {
            if (exception.status() == 404) {
                TelegramBetRoundsResult.AccountRequired
            } else {
                TelegramBetRoundsResult.Unavailable
            }
        } catch (_: RuntimeException) {
            TelegramBetRoundsResult.Unavailable
        }

    override fun history(telegramId: Long): TelegramBetHistoryResult =
        try {
            TelegramBetHistoryResult.Available(
                client.history(telegramId).map { bet -> bet.toModel() },
            )
        } catch (exception: FeignException) {
            if (exception.status() == 404) {
                TelegramBetHistoryResult.AccountRequired
            } else {
                TelegramBetHistoryResult.Unavailable
            }
        } catch (_: RuntimeException) {
            TelegramBetHistoryResult.Unavailable
        }

    override fun place(
        telegramId: Long,
        roundId: UUID,
        side: TelegramBetSide,
        amount: Long,
        idempotencyKey: UUID,
    ): TelegramBetMutationResult =
        mutate {
            client.place(
                telegramId = telegramId,
                roundId = roundId,
                request =
                    TelegramPlaceEventBetRequest(
                        side = ContractBetSide.valueOf(side.name),
                        amount = amount,
                        idempotencyKey = idempotencyKey,
                    ),
            )
        }

    override fun increase(
        telegramId: Long,
        roundId: UUID,
        amount: Long,
        idempotencyKey: UUID,
    ): TelegramBetMutationResult =
        mutate {
            client.increase(
                telegramId = telegramId,
                roundId = roundId,
                request =
                    TelegramIncreaseEventBetRequest(
                        amount = amount,
                        idempotencyKey = idempotencyKey,
                    ),
            )
        }

    private fun mutate(request: () -> Any): TelegramBetMutationResult =
        try {
            request()
            TelegramBetMutationResult.ACCEPTED
        } catch (exception: FeignException) {
            when (exception.status()) {
                404 -> TelegramBetMutationResult.ACCOUNT_REQUIRED
                400, 403, 409 -> TelegramBetMutationResult.INVALID
                else -> TelegramBetMutationResult.UNAVAILABLE
            }
        } catch (_: RuntimeException) {
            TelegramBetMutationResult.UNAVAILABLE
        }
}
