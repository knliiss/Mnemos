package com.mnemos.personalization.application.ownership.usecase.notificationpack

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.personalization.application.account.port.out.PersonalizationTelegramUserResolver
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackStoreItem
import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackStorePage
import com.mnemos.personalization.application.personalization.usecase.listcatalog.ListPersonalizationsQuery
import com.mnemos.personalization.application.personalization.usecase.listcatalog.ListPersonalizationsUseCase
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TelegramNotificationPackStoreService(
    private val telegramUserResolver: PersonalizationTelegramUserResolver,
    private val catalog: ListPersonalizationsUseCase,
    private val ownershipRepository: OwnedPersonalizationRepository,
) {
    @Transactional(readOnly = true)
    fun list(
        telegramId: TelegramId,
        page: Int,
        size: Int,
    ): TelegramNotificationPackStorePage? {
        val userId = telegramUserResolver.findUserId(telegramId) ?: return null
        val owner = PersonalizationOwner.User(userId)
        val ownerships =
            ownershipRepository
                .findAll(owner)
                .filter { ownership -> ownership.type == PersonalizationType.NOTIFICATION_PACK }
                .associateBy { ownership -> ownership.personalizationId }
        val result =
            catalog.execute(
                ListPersonalizationsQuery(
                    type = PersonalizationType.NOTIFICATION_PACK,
                    currency = null,
                    page =
                        PageQuery(
                            page = page.coerceAtLeast(0),
                            size = size.coerceIn(1, MAX_PAGE_SIZE),
                        ),
                ),
            )

        return TelegramNotificationPackStorePage(
            items =
                result.items.map { personalization ->
                    val ownership = ownerships[personalization.id]

                    personalization.toStoreItem(
                        owned = ownership != null,
                        active = ownership?.active == true,
                    )
                },
            page = result.page,
            totalItems = result.totalItems,
            totalPages = result.totalPages,
            hasNextPage = result.hasNextPage,
        )
    }

    private fun Personalization.toStoreItem(
        owned: Boolean,
        active: Boolean,
    ): TelegramNotificationPackStoreItem {
        val notificationPack = content as PersonalizationContent.NotificationPack
        val packPrice = requireNotNull(price) {
            "Published notification pack requires a price"
        }
        val samples =
            notificationPack.templates.values
                .take(MAX_SAMPLES)
                .map { template ->
                    listOfNotNull(
                        template.emoji,
                        template.soloTitle,
                        template.soloBody,
                    ).joinToString(" ")
                }

        return TelegramNotificationPackStoreItem(
            personalizationId = id,
            name = name,
            author =
                if (origin == PersonalizationOrigin.SYSTEM) {
                    SYSTEM_AUTHOR
                } else {
                    COMMUNITY_AUTHOR
                },
            priceAmount = packPrice.amount,
            currency = packPrice.currency,
            owned = owned,
            active = active,
            samples = samples,
        )
    }

    private companion object {
        const val MAX_PAGE_SIZE = 8
        const val MAX_SAMPLES = 3
        const val SYSTEM_AUTHOR = "Mnemos"
        const val COMMUNITY_AUTHOR = "Автор сообщества"
    }
}
