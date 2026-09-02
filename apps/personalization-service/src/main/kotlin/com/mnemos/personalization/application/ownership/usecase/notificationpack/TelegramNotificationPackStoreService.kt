package com.mnemos.personalization.application.ownership.usecase.notificationpack

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.personalization.application.account.port.out.PersonalizationTelegramUserResolver
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackDetail
import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackStoreItem
import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackStorePage
import com.mnemos.personalization.application.personalization.usecase.getcatalog.GetPersonalizationUseCase
import com.mnemos.personalization.application.personalization.usecase.listcatalog.ListPersonalizationsQuery
import com.mnemos.personalization.application.personalization.usecase.listcatalog.ListPersonalizationsUseCase
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TelegramNotificationPackStoreService(
    private val telegramUserResolver: PersonalizationTelegramUserResolver,
    private val catalog: ListPersonalizationsUseCase,
    private val getPersonalization: GetPersonalizationUseCase,
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

    @Transactional(readOnly = true)
    fun detail(
        telegramId: TelegramId,
        personalizationId: PersonalizationId,
    ): TelegramNotificationPackDetail? {
        val userId = telegramUserResolver.findUserId(telegramId) ?: return null
        val personalization = getPersonalization.execute(personalizationId)

        if (personalization.type != PersonalizationType.NOTIFICATION_PACK) {
            return null
        }

        val owner = PersonalizationOwner.User(userId)
        val ownership = ownershipRepository.find(owner, personalizationId)

        return personalization.toDetail(
            owned = ownership != null,
            active = ownership?.active == true,
        )
    }

    private fun Personalization.toStoreItem(
        owned: Boolean,
        active: Boolean,
    ): TelegramNotificationPackStoreItem {
        val packPrice = requirePackPrice()

        return TelegramNotificationPackStoreItem(
            personalizationId = id,
            name = name,
            author = packAuthor(),
            priceAmount = packPrice.amount,
            currency = packPrice.currency,
            owned = owned,
            active = active,
        )
    }

    private fun Personalization.toDetail(
        owned: Boolean,
        active: Boolean,
    ): TelegramNotificationPackDetail {
        val notificationPack = content as PersonalizationContent.NotificationPack
        val packPrice = requirePackPrice()

        return TelegramNotificationPackDetail(
            personalizationId = id,
            name = name,
            author = packAuthor(),
            priceAmount = packPrice.amount,
            currency = packPrice.currency,
            owned = owned,
            active = active,
            content = notificationPack,
        )
    }

    private fun Personalization.requirePackPrice() =
        requireNotNull(price) {
            "Published notification pack requires a price"
        }

    private fun Personalization.packAuthor(): String =
        if (origin == PersonalizationOrigin.SYSTEM) {
            SYSTEM_AUTHOR
        } else {
            COMMUNITY_AUTHOR
        }

    private companion object {
        const val MAX_PAGE_SIZE = 8
        const val SYSTEM_AUTHOR = "Mnemos"
        const val COMMUNITY_AUTHOR = "Автор сообщества"
    }
}
