package com.mnemos.telegram.bot.application.widget.bets

import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.TelegramBetHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRaceHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacesResult
import com.mnemos.telegram.bot.application.bet.port.out.BetGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.bets.port.out.BetWidgetPublisher
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenBetWidgetService(
    private val properties: TelegramWidgetProperties,
    private val accounts: AccountGateway,
    private val bets: BetGateway,
    private val publisher: BetWidgetPublisher,
) : OpenBetWidgetUseCase,
    BetWidgetSnapshotQuery {
    override val enabled: Boolean
        get() = properties.enabled

    override fun execute(
        telegramId: Long,
        mode: BetMenuMode,
    ): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val snapshot = load(telegramId, mode)) {
            is BetWidgetSnapshotResult.Available -> publisher.publish(telegramId, snapshot.view)
            BetWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(
        telegramId: Long,
        mode: BetMenuMode,
    ): BetWidgetSnapshotResult {
        val accountResult = accounts.getContext(telegramId)
        if (accountResult == TelegramAccountContextResult.Unavailable) return BetWidgetSnapshotResult.Unavailable
        val accountId = (accountResult as? TelegramAccountContextResult.Available)?.context?.activeAccountId

        val items =
            when (mode) {
                BetMenuMode.ROUNDS -> {
                    when (val result = bets.listRounds(telegramId)) {
                        is TelegramBetRoundsResult.Available ->
                            result.rounds.map { round ->
                                BetWidgetEntry(
                                    title = round.eventType,
                                    details = "YES " + round.yesOdds.toPlainString() + " · NO " + round.noOdds.toPlainString(),
                                    status = round.status + (round.myBet?.let { " · " + it.confirmedAmount + " COIN" } ?: ""),
                                )
                            }
                        TelegramBetRoundsResult.AccountRequired -> emptyList()
                        TelegramBetRoundsResult.Unavailable -> return BetWidgetSnapshotResult.Unavailable
                    }
                }

                BetMenuMode.RACES -> {
                    when (val result = bets.listItemRaces(telegramId)) {
                        is TelegramItemRacesResult.Available ->
                            result.races.map { race ->
                                BetWidgetEntry(
                                    title = race.itemName,
                                    details = "Вхід: " + race.entryAmount + " COIN",
                                    status = race.status + (race.myPosition?.let { " · " + it.confirmedAmount + " COIN" } ?: ""),
                                )
                            }
                        TelegramItemRacesResult.AccountRequired -> emptyList()
                        TelegramItemRacesResult.Unavailable -> return BetWidgetSnapshotResult.Unavailable
                    }
                }

                BetMenuMode.HISTORY -> {
                    val events = bets.eventHistory(telegramId)
                    val races = bets.itemRaceHistory(telegramId)
                    if (events == TelegramBetHistoryResult.Unavailable || races == TelegramItemRaceHistoryResult.Unavailable) {
                        return BetWidgetSnapshotResult.Unavailable
                    }
                    val eventEntries =
                        (events as? TelegramBetHistoryResult.Available)?.bets.orEmpty().map { bet ->
                            BetWidgetEntry(
                                title = "Ставка " + bet.side.name,
                                details = bet.confirmedAmount.toString() + " COIN",
                                status = bet.status,
                            )
                        }
                    val raceEntries =
                        (races as? TelegramItemRaceHistoryResult.Available)?.positions.orEmpty().map { position ->
                            BetWidgetEntry(
                                title = position.accountNickname,
                                details = position.confirmedAmount.toString() + " COIN",
                                status = position.status,
                            )
                        }
                    eventEntries + raceEntries
                }
            }

        return BetWidgetSnapshotResult.Available(
            BetWidgetView(
                accountId = accountId,
                mode = mode,
                totalItems = items.size,
                entries =
                    items.take(MAX_VISIBLE).map { item ->
                        BetWidgetEntry(
                            title = WidgetTextLimiter.truncateUtf8(item.title, 32),
                            details = WidgetTextLimiter.truncateUtf8(item.details, 44),
                            status = WidgetTextLimiter.truncateUtf8(item.status, 24),
                        )
                    },
            ),
        )
    }

    private companion object {
        const val MAX_VISIBLE = 4
    }
}
