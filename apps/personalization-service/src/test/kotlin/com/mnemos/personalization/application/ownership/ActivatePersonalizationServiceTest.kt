package com.mnemos.personalization.application.ownership

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.personalization.application.account.port.out.PersonalizationAccountAccess
import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.ownership.exception.PersonalizationOwnershipConflictException
import com.mnemos.personalization.application.ownership.support.PersonalizationOwnershipSupport
import com.mnemos.personalization.application.ownership.usecase.activate.ActivatePersonalizationService
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ActivatePersonalizationServiceTest {
    private val now = Instant.parse("2026-08-15T19:20:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `concurrent creator claim conflict returns application conflict`() {
        val userId = UserId.generate()
        val personalization = publishedNotificationPack(userId)
        val ownershipRepository = ConflictingOwnedPersonalizationRepository()
        val service =
            createService(
                userId = userId,
                personalization = personalization,
                accountAccess = UnusedAccountAccess(),
                ownershipRepository = ownershipRepository,
            )

        assertThrows(PersonalizationOwnershipConflictException::class.java) {
            service.execute(personalization.id)
        }
    }

    @Test
    fun `creator cannot claim an unlisted personalization`() {
        val userId = UserId.generate()
        val personalization = publishedNotificationPack(userId)
        personalization.unlist(now.minusSeconds(20))
        val service =
            createService(
                userId = userId,
                personalization = personalization,
                accountAccess = UnusedAccountAccess(),
                ownershipRepository = EmptyOwnedPersonalizationRepository(),
            )

        assertThrows(PersonalizationNotOwnedException::class.java) {
            service.execute(personalization.id)
        }
    }

    @Test
    fun `creator claim is rejected while account purchase is pending`() {
        val creatorUserId = UserId.generate()
        val accountId = AccountId.generate()
        val personalization = publishedNickStyle(creatorUserId)
        val owner = PersonalizationOwner.Account(accountId)
        val purchaseOperationRepository = mock<PurchaseOperationRepository>()

        whenever(
            purchaseOperationRepository.existsPending(
                owner = owner,
                personalizationId = personalization.id,
            ),
        ).thenReturn(true)

        val service =
            createService(
                userId = creatorUserId,
                personalization = personalization,
                accountAccess = FixedAccountAccess(accountId),
                ownershipRepository = EmptyOwnedPersonalizationRepository(),
                purchaseOperationRepository = purchaseOperationRepository,
            )

        assertThrows(PersonalizationOwnershipConflictException::class.java) {
            service.execute(personalization.id)
        }
    }

    private fun createService(
        userId: UserId,
        personalization: Personalization,
        accountAccess: PersonalizationAccountAccess,
        ownershipRepository: OwnedPersonalizationRepository,
        purchaseOperationRepository: PurchaseOperationRepository = mock(),
    ): ActivatePersonalizationService {
        val personalizationRepository = FixedPersonalizationRepository(personalization)
        val support =
            PersonalizationOwnershipSupport(
                accountAccess = accountAccess,
                personalizationRepository = personalizationRepository,
                ownershipRepository = ownershipRepository,
                purchaseOperationRepository = purchaseOperationRepository,
            )

        return ActivatePersonalizationService(
            actorProvider = FixedActorProvider(userId),
            ownershipRepository = ownershipRepository,
            support = support,
            selectionEventPublisher = mock<NotificationPackSelectionEventPublisher>(),
            clock = clock,
        )
    }

    private fun publishedNotificationPack(userId: UserId): Personalization {
        val price =
            PersonalizationPrice(
                currency = Currency.PRIME,
                amount = 100,
            )
        val personalization =
            Personalization.createCreatorDraft(
                creatorUserId = userId,
                type = PersonalizationType.NOTIFICATION_PACK,
                name = "Creator Pack",
                content = notificationPack(),
                price = price,
                now = now.minusSeconds(60),
            )

        personalization.submitForReview(now.minusSeconds(50))
        personalization.approve(
            moderatedPrice = price,
            now = now.minusSeconds(40),
        )
        personalization.publish(now.minusSeconds(30))

        return personalization
    }

    private fun publishedNickStyle(userId: UserId): Personalization {
        val price =
            PersonalizationPrice(
                currency = Currency.COIN,
                amount = 500,
            )
        val personalization =
            Personalization.createCreatorDraft(
                creatorUserId = userId,
                type = PersonalizationType.NICK_STYLE,
                name = "Creator Nick Style",
                content =
                    PersonalizationContent.NickStyle(
                        colors =
                            listOf(
                                GradientStop(
                                    color = HexColor("#F5FBFF"),
                                    position = 0,
                                ),
                                GradientStop(
                                    color = HexColor("#8ACBFF"),
                                    position = 100,
                                ),
                            ),
                        angleDegrees = 90,
                        glow = null,
                    ),
                price = price,
                now = now.minusSeconds(60),
            )

        personalization.submitForReview(now.minusSeconds(50))
        personalization.approve(
            moderatedPrice = price,
            now = now.minusSeconds(40),
        )
        personalization.publish(now.minusSeconds(30))

        return personalization
    }

    private fun notificationPack(): PersonalizationContent.NotificationPack =
        PersonalizationContent.NotificationPack(
            mixedAggregateTitle = "Events",
            templates =
                NotificationTemplateKey.entries.associateWith { key ->
                    NotificationTemplate(
                        emoji = null,
                        soloTitle = "${key.name} title",
                        soloBody = "${key.name} body",
                        compact = "${key.name} compact",
                        aggregateTitle = "${key.name} events",
                    )
                },
        )

    private class FixedActorProvider(
        userId: UserId,
    ) : CurrentPersonalizationActorProvider {
        private val actor =
            PersonalizationActor(
                userId = userId,
                roles = emptySet(),
            )

        override fun requireActor(): PersonalizationActor = actor
    }

    private class UnusedAccountAccess : PersonalizationAccountAccess {
        override fun requireActiveAccount(userId: UserId): AccountId =
            error("Account access must not be used for user-target personalization")

        override fun hasPermission(
            accountId: AccountId,
            userId: UserId,
            permission: RolePermission,
        ): Boolean = error("Account access must not be used for user-target personalization")
    }

    private class FixedAccountAccess(
        private val accountId: AccountId,
    ) : PersonalizationAccountAccess {
        override fun requireActiveAccount(userId: UserId): AccountId = accountId

        override fun hasPermission(
            accountId: AccountId,
            userId: UserId,
            permission: RolePermission,
        ): Boolean = permission == RolePermission.APPLY_PERSONALIZATIONS
    }

    private class FixedPersonalizationRepository(
        private val personalization: Personalization,
    ) : PersonalizationRepository {
        override fun save(personalization: Personalization): Personalization = personalization

        override fun findById(id: PersonalizationId): Personalization? = personalization.takeIf { it.id == id }

        override fun findByCreatorUserId(creatorUserId: UserId): List<Personalization> = emptyList()

        override fun findByStatus(status: PersonalizationStatus): List<Personalization> = emptyList()
    }

    private class EmptyOwnedPersonalizationRepository : OwnedPersonalizationRepository {
        override fun save(ownership: OwnedPersonalization): OwnedPersonalization = ownership

        override fun exists(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): Boolean = false

        override fun find(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): OwnedPersonalization? = null

        override fun findActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ): OwnedPersonalization? = null

        override fun findAll(owner: PersonalizationOwner): List<OwnedPersonalization> = emptyList()

        override fun deactivateActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ) = Unit
    }

    private class ConflictingOwnedPersonalizationRepository : OwnedPersonalizationRepository {
        override fun save(ownership: OwnedPersonalization): OwnedPersonalization =
            throw DataIntegrityViolationException(
                "duplicate key value violates unique constraint \"uq_owned_personalizations_owner_item\"",
            )

        override fun exists(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): Boolean = false

        override fun find(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): OwnedPersonalization? = null

        override fun findActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ): OwnedPersonalization? = null

        override fun findAll(owner: PersonalizationOwner): List<OwnedPersonalization> = emptyList()

        override fun deactivateActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ) = Unit
    }
}
