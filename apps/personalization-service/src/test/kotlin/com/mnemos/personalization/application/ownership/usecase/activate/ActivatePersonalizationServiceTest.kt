package com.mnemos.personalization.application.ownership.usecase.activate

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.personalization.application.account.port.out.PersonalizationAccountAccess
import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceEventPublisher
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.ownership.exception.PersonalizationOwnershipConflictException
import com.mnemos.personalization.application.ownership.support.PersonalizationOwnershipSupport
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
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
        val ownershipRepository = mock<OwnedPersonalizationRepository>()
        val fixture =
            fixture(
                userId = userId,
                personalization = personalization,
                ownershipRepository = ownershipRepository,
            )
        val owner = PersonalizationOwner.User(userId)

        whenever(ownershipRepository.findForUpdate(owner, personalization.id)).thenReturn(null)
        whenever(fixture.purchaseOperationRepository.existsPending(owner, personalization.id)).thenReturn(false)
        whenever(ownershipRepository.save(any())).thenThrow(
            DataIntegrityViolationException(
                "duplicate key value violates unique constraint \"uq_owned_personalizations_owner_item\"",
            ),
        )

        assertThrows(PersonalizationOwnershipConflictException::class.java) {
            fixture.service.execute(personalization.id)
        }
    }

    @Test
    fun `creator cannot claim an unlisted personalization`() {
        val userId = UserId.generate()
        val personalization = publishedNotificationPack(userId)
        personalization.unlist(now.minusSeconds(20))
        val ownershipRepository = mock<OwnedPersonalizationRepository>()
        val fixture =
            fixture(
                userId = userId,
                personalization = personalization,
                ownershipRepository = ownershipRepository,
            )
        val owner = PersonalizationOwner.User(userId)

        whenever(ownershipRepository.findForUpdate(owner, personalization.id)).thenReturn(null)

        assertThrows(PersonalizationNotOwnedException::class.java) {
            fixture.service.execute(personalization.id)
        }
    }

    @Test
    fun `creator claim is rejected while account purchase is pending`() {
        val creatorUserId = UserId.generate()
        val accountId = AccountId.generate()
        val personalization = publishedNickStyle(creatorUserId)
        val ownershipRepository = mock<OwnedPersonalizationRepository>()
        val accountAccess = mock<PersonalizationAccountAccess>()
        val fixture =
            fixture(
                userId = creatorUserId,
                personalization = personalization,
                ownershipRepository = ownershipRepository,
                accountAccess = accountAccess,
            )
        val owner = PersonalizationOwner.Account(accountId)

        whenever(accountAccess.requireActiveAccount(creatorUserId)).thenReturn(accountId)
        whenever(
            accountAccess.hasPermission(
                accountId = accountId,
                userId = creatorUserId,
                permission = RolePermission.APPLY_PERSONALIZATIONS,
            ),
        ).thenReturn(true)
        whenever(ownershipRepository.findForUpdate(owner, personalization.id)).thenReturn(null)
        whenever(fixture.purchaseOperationRepository.existsPending(owner, personalization.id)).thenReturn(true)

        assertThrows(PersonalizationOwnershipConflictException::class.java) {
            fixture.service.execute(personalization.id)
        }
    }

    private fun fixture(
        userId: UserId,
        personalization: Personalization,
        ownershipRepository: OwnedPersonalizationRepository,
        accountAccess: PersonalizationAccountAccess = mock(),
    ): Fixture {
        val actorProvider = mock<CurrentPersonalizationActorProvider>()
        val personalizationRepository = mock<PersonalizationRepository>()
        val purchaseOperationRepository = mock<PurchaseOperationRepository>()

        whenever(actorProvider.requireActor()).thenReturn(
            PersonalizationActor(
                userId = userId,
                roles = emptySet(),
            ),
        )
        whenever(personalizationRepository.findByIdForUpdate(personalization.id)).thenReturn(personalization)

        val support =
            PersonalizationOwnershipSupport(
                accountAccess = accountAccess,
                personalizationRepository = personalizationRepository,
                ownershipRepository = ownershipRepository,
                purchaseOperationRepository = purchaseOperationRepository,
            )
        val service =
            ActivatePersonalizationService(
                actorProvider = actorProvider,
                ownershipRepository = ownershipRepository,
                support = support,
                selectionEventPublisher = mock<NotificationPackSelectionEventPublisher>(),
                profileAppearanceEventPublisher = mock<ProfileAppearanceEventPublisher>(),
                clock = clock,
            )

        return Fixture(
            service = service,
            purchaseOperationRepository = purchaseOperationRepository,
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

    private data class Fixture(
        val service: ActivatePersonalizationService,
        val purchaseOperationRepository: PurchaseOperationRepository,
    )
}
