package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementResult
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetPublisher
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenAccountManagementWidgetService(
    private val properties: TelegramWidgetProperties,
    private val gateway: AccountManagementGateway,
    private val publisher: AccountManagementWidgetPublisher,
) : OpenAccountManagementWidgetUseCase, AccountManagementWidgetSnapshotQuery {
    override val enabled: Boolean get() = properties.enabled

    override fun execute(telegramId: Long, page: Int): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val result = load(telegramId, page)) {
            is AccountManagementWidgetSnapshotResult.Available -> publisher.publish(telegramId, result.view)
            AccountManagementWidgetSnapshotResult.AccountRequired ->
                TelegramDeliveryResult.PermanentFailure("account-required")
            AccountManagementWidgetSnapshotResult.Forbidden ->
                TelegramDeliveryResult.PermanentFailure("management-forbidden")
            AccountManagementWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(telegramId: Long, page: Int): AccountManagementWidgetSnapshotResult {
        if (telegramId <= 0 || page !in 0..1000) return AccountManagementWidgetSnapshotResult.Unavailable
        return when (val result = gateway.getManagement(telegramId)) {
            is TelegramAccountManagementResult.Available -> {
                val data = result.snapshot
                val pages = maxOf(
                    1,
                    (maxOf(data.members.size, data.roles.size) + PAGE_SIZE - 1) / PAGE_SIZE,
                )
                val currentPage = page.coerceAtMost(pages - 1)
                AccountManagementWidgetSnapshotResult.Available(
                    AccountManagementWidgetView(
                        accountId = data.accountId,
                        nickname = WidgetTextLimiter.truncateUtf8(data.cristalixNickname, 36),
                        page = currentPage,
                        pages = pages,
                        totalMembers = data.members.size,
                        totalRoles = data.roles.size,
                        members = data.members.drop(currentPage * PAGE_SIZE).take(PAGE_SIZE).map {
                            AccountManagementWidgetMember(
                                name = WidgetTextLimiter.truncateUtf8(it.name, 34),
                                role = it.highestRoleName?.let { name -> WidgetTextLimiter.truncateUtf8(name, 26) },
                                owner = it.owner,
                            )
                        },
                        roles = data.roles.sortedByDescending { it.hierarchyRank }
                            .drop(currentPage * PAGE_SIZE).take(PAGE_SIZE).map {
                                AccountManagementWidgetRole(
                                    name = WidgetTextLimiter.truncateUtf8(it.name, 34),
                                    rank = it.hierarchyRank,
                                    permissionCount = it.permissions.size,
                                )
                            },
                    ),
                )
            }
            TelegramAccountManagementResult.ActiveAccountRequired -> AccountManagementWidgetSnapshotResult.AccountRequired
            TelegramAccountManagementResult.Forbidden -> AccountManagementWidgetSnapshotResult.Forbidden
            TelegramAccountManagementResult.Unavailable -> AccountManagementWidgetSnapshotResult.Unavailable
        }
    }

    private companion object {
        const val PAGE_SIZE = 3
    }
}
