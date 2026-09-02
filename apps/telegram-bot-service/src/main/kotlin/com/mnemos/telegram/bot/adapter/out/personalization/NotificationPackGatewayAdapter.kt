package com.mnemos.telegram.bot.adapter.out.personalization

import com.mnemos.personalization.contracts.internal.v1.PurchaseInternalTelegramNotificationPackRequest
import com.mnemos.personalization.contracts.internal.v1.UpdateInternalTelegramNotificationPackSelectionRequest
import com.mnemos.telegram.bot.adapter.out.personalization.TelegramNotificationPackContractMapper.toModel
import com.mnemos.telegram.bot.adapter.out.settings.PersonalizationServiceClient
import com.mnemos.telegram.bot.application.pack.model.NotificationPackApplyResult
import com.mnemos.telegram.bot.application.pack.model.NotificationPackDetailResult
import com.mnemos.telegram.bot.application.pack.model.NotificationPackPurchaseResult
import com.mnemos.telegram.bot.application.pack.model.NotificationPackStoreResult
import com.mnemos.telegram.bot.application.pack.model.OwnedNotificationPacksResult
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackStorePage
import com.mnemos.telegram.bot.application.pack.model.TelegramOwnedNotificationPack
import com.mnemos.telegram.bot.application.pack.port.out.NotificationPackGateway
import feign.FeignException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class NotificationPackGatewayAdapter(
    private val client: PersonalizationServiceClient,
) : NotificationPackGateway {
    override fun store(
        telegramId: Long,
        page: Int,
        size: Int,
    ): NotificationPackStoreResult =
        try {
            client
                .getNotificationPackStore(
                    telegramId = telegramId,
                    page = page,
                    size = size,
                )?.let { response ->
                    NotificationPackStoreResult.Available(
                        TelegramNotificationPackStorePage(
                            packs = response.packs.map { pack -> pack.toModel() },
                            page = response.page,
                            totalItems = response.totalItems,
                            totalPages = response.totalPages,
                            hasNextPage = response.hasNextPage,
                        ),
                    )
                } ?: NotificationPackStoreResult.RegistrationRequired
        } catch (_: RuntimeException) {
            NotificationPackStoreResult.Unavailable
        }

    override fun detail(
        telegramId: Long,
        personalizationId: UUID,
    ): NotificationPackDetailResult =
        try {
            client
                .getNotificationPackDetail(
                    telegramId = telegramId,
                    personalizationId = personalizationId,
                )?.let { response ->
                    NotificationPackDetailResult.Available(response.toModel())
                } ?: NotificationPackDetailResult.NotFound
        } catch (exception: FeignException) {
            if (exception.status() in 400..499) {
                NotificationPackDetailResult.NotFound
            } else {
                NotificationPackDetailResult.Unavailable
            }
        } catch (_: RuntimeException) {
            NotificationPackDetailResult.Unavailable
        }

    override fun owned(telegramId: Long): OwnedNotificationPacksResult =
        try {
            client
                .getNotificationPacks(telegramId)
                ?.let { response ->
                    OwnedNotificationPacksResult.Available(
                        response.packs.map { pack ->
                            TelegramOwnedNotificationPack(
                                personalizationId = pack.personalizationId,
                                name = pack.name,
                                active = pack.active,
                            )
                        },
                    )
                } ?: OwnedNotificationPacksResult.RegistrationRequired
        } catch (_: RuntimeException) {
            OwnedNotificationPacksResult.Unavailable
        }

    override fun purchase(
        telegramId: Long,
        personalizationId: UUID,
        idempotencyKey: UUID,
    ): NotificationPackPurchaseResult =
        try {
            client
                .purchaseNotificationPack(
                    telegramId = telegramId,
                    personalizationId = personalizationId,
                    request = PurchaseInternalTelegramNotificationPackRequest(idempotencyKey),
                )?.let { response -> NotificationPackPurchaseResult.Accepted(response.operationId) }
                ?: NotificationPackPurchaseResult.Refresh
        } catch (exception: FeignException) {
            if (exception.status() in 400..499) {
                NotificationPackPurchaseResult.Refresh
            } else {
                NotificationPackPurchaseResult.Unavailable
            }
        } catch (_: RuntimeException) {
            NotificationPackPurchaseResult.Unavailable
        }

    override fun apply(
        telegramId: Long,
        personalizationId: UUID,
    ): NotificationPackApplyResult =
        try {
            client
                .updateNotificationPackSelection(
                    telegramId = telegramId,
                    request = UpdateInternalTelegramNotificationPackSelectionRequest(personalizationId),
                )?.let { NotificationPackApplyResult.Applied }
                ?: NotificationPackApplyResult.Refresh
        } catch (exception: FeignException) {
            if (exception.status() in 400..499) {
                NotificationPackApplyResult.Refresh
            } else {
                NotificationPackApplyResult.Unavailable
            }
        } catch (_: RuntimeException) {
            NotificationPackApplyResult.Unavailable
        }
}
