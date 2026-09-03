package com.mnemos.personalization.application.purchase

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.personalization.application.account.port.out.PersonalizationAccountAccess
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.purchase.exception.PersonalizationNotPurchasableException
import com.mnemos.personalization.application.purchase.exception.PersonalizationPurchaseInProgressException
import com.mnemos.personalization.application.purchase.port.out.PersonalizationWallet
import com.mnemos.personalization.application.purchase.usecase.purchase.PurchasePersonalizationService
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
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
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PurchasePersonalizationServiceTest {
    private val now = Instant.parse("2026-08-15T17:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `account coin purchase requires account purchase permission`() {
        val creatorUserId = UserId.generate()
        val buyerUserId = UserId.generate()
        val accountId = AccountId.generate()
        val personalization = publishedNickStyle(creatorUserId)
        val fixture =
            fixture(
                personalization = personalization,
                buyerUserId = buyerUserId,
                accountId = accountId,
                grantedPermissions = setOf(RolePermission.BUY_ACCOUNT_PERSONALIZATIONS),
            )

        val result = fixture.service.execute(personalization.id)
        val operation = fixture.purchaseRepository.saved.single()

        assertEquals(personalization.id, result.personalizationId)
        assertEquals(operation.id, result.operationId)
        assertEquals(PersonalizationOwner.Account(accountId), operation.owner)
        assertEquals(buyerUserId, operation.buyerUserId)
        assertEquals(Currency.COIN, operation.currency)
        assertEquals(1_000L, operation.amount)
        assertEquals(PurchaseOperationStatus.PENDING, operation.status)
        assertEquals(listOf(operation), fixture.wallet.requestedDebits)
        assertEquals(
            listOf(RolePermission.BUY_ACCOUNT_PERSONALIZATIONS),
            fixture.accountAccess.checkedPermissions,
        )
    }

    @Test
    fun `account coin purchase is denied without account purchase permission`() {
        val personalization = publishedNickStyle(UserId.generate())
        val fixture =
            fixture(
                personalization = personalization,
                buyerUserId = UserId.generate(),
                accountId = AccountId.generate(),
            )

        assertThrows(PersonalizationAccessDeniedException::class.java) {
            fixture.service.execute(personalization.id)
        }

        assertTrue(fixture.purchaseRepository.saved.isEmpty())
        assertTrue(fixture.wallet.requestedDebits.isEmpty())
    }

    @Test
    fun `private custom notification pack purchase belongs to creator user`() {
        val userId = UserId.generate()
        val personalization = paidCustomNotificationPack(userId)
        val fixture =
            fixture(
                personalization = personalization,
                buyerUserId = userId,
                accountId = AccountId.generate(),
            )

        fixture.service.execute(personalization.id)

        val operation = fixture.purchaseRepository.saved.single()

        assertEquals(PersonalizationOwner.User(userId), operation.owner)
        assertEquals(Currency.PRIME, operation.currency)
        assertEquals(50L, operation.amount)
        assertEquals(0, fixture.accountAccess.requireActiveAccountCalls)
        assertTrue(fixture.accountAccess.checkedPermissions.isEmpty())
        assertEquals(listOf(operation), fixture.wallet.requestedDebits)
    }

    @Test
    fun `published personalization cannot be purchased by its creator`() {
        val creatorUserId = UserId.generate()
        val personalization = publishedNickStyle(creatorUserId)
        val fixture =
            fixture(
                personalization = personalization,
                buyerUserId = creatorUserId,
                accountId = AccountId.generate(),
                grantedPermissions = setOf(RolePermission.BUY_ACCOUNT_PERSONALIZATIONS),
            )

        assertThrows(PersonalizationNotPurchasableException::class.java) {
            fixture.service.execute(personalization.id)
        }

        assertEquals(0, fixture.accountAccess.requireActiveAccountCalls)
        assertTrue(fixture.purchaseRepository.saved.isEmpty())
        assertTrue(fixture.wallet.requestedDebits.isEmpty())
    }

    @Test
    fun `duplicate pending purchase does not send another wallet debit`() {
        val personalization = publishedNickStyle(UserId.generate())
        val fixture =
            fixture(
                personalization = personalization,
                buyerUserId = UserId.generate(),
                accountId = AccountId.generate(),
                grantedPermissions = setOf(RolePermission.BUY_ACCOUNT_PERSONALIZATIONS),
                pendingPurchaseExists = true,
            )

        assertThrows(PersonalizationPurchaseInProgressException::class.java) {
            fixture.service.execute(personalization.id)
        }

        assertTrue(fixture.purchaseRepository.saved.isEmpty())
        assertTrue(fixture.wallet.requestedDebits.isEmpty())
    }

    private fun fixture(
        personalization: Personalization,
        buyerUserId: UserId,
        accountId: AccountId,
        grantedPermissions: Set<RolePermission> = emptySet(),
        pendingPurchaseExists: Boolean = false,
    ): Fixture {
        val personalizationRepository = FakePersonalizationRepository(personalization)
        val ownershipRepository = FakeOwnedPersonalizationRepository()
        val purchaseRepository = FakePurchaseOperationRepository(pendingPurchaseExists)
        val wallet = RecordingPersonalizationWallet()
        val accountAccess = FakePersonalizationAccountAccess(accountId, grantedPermissions)
        val service =
            PurchasePersonalizationService(
                actorProvider = FixedActorProvider(buyerUserId),
                accountAccess = accountAccess,
                personalizationRepository = personalizationRepository,
                ownershipRepository = ownershipRepository,
                purchaseOperationRepository = purchaseRepository,
                wallet = wallet,
                clock = clock,
            )

        return Fixture(
            service = service,
            purchaseRepository = purchaseRepository,
            wallet = wallet,
            accountAccess = accountAccess,
        )
    }

    private fun publishedNickStyle(creatorUserId: UserId): Personalization {
        val price =
            PersonalizationPrice(
                currency = Currency.COIN,
                amount = 1_000,
            )
        val personalization =
            Personalization.createCreatorDraft(
                creatorUserId = creatorUserId,
                type = PersonalizationType.NICK_STYLE,
                name = "Creator Nick Style",
                content =
                    PersonalizationContent.NickStyle(
                        colors =
                            listOf(
                                GradientStop(
                                    color = HexColor("#FFFFFF"),
                                    position = 0,
                                ),
                            ),
                        angleDegrees = 0,
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

    private fun paidCustomNotificationPack(userId: UserId): Personalization =
        Personalization.createCustomNotificationPack(
            creatorUserId = userId,
            name = "Private Custom Pack",
            content = notificationPack(),
            price =
                PersonalizationPrice(
                    currency = Currency.PRIME,
                    amount = 50,
                ),
            now = now.minusSeconds(60),
        )

    private fun notificationPack(): PersonalizationContent.NotificationPack =
        PersonalizationContent.NotificationPack(
            mixedAggregateTitle = "Mixed events",
            templates =
                NotificationTemplateKey.entries.associateWith { key ->
                    NotificationTemplate(
                        emoji = null,
                        soloTitle = "${key.name} title",
                        soloBody = "${key.name} body",
                        compact = "${key.name} compact",
                        aggregateTitle = "${key.name} aggregate",
                    )
                },
        )

    private data class Fixture(
        val service: PurchasePersonalizationService,
        val purchaseRepository: FakePurchaseOperationRepository,
        val wallet: RecordingPersonalizationWallet,
        val accountAccess: FakePersonalizationAccountAccess,
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

    private class FakePersonalizationAccountAccess(
        private val accountId: AccountId,
        private val grantedPermissions: Set<RolePermission>,
    ) : PersonalizationAccountAccess {
        var requireActiveAccountCalls = 0
        val checkedPermissions = mutableListOf<RolePermission>()

        override fun requireActiveAccount(userId: UserId): AccountId {
            requireActiveAccountCalls += 1

            return accountId
        }

        override fun hasPermission(
            accountId: AccountId,
            userId: UserId,
            permission: RolePermission,
        ): Boolean {
            checkedPermissions += permission

            return permission in grantedPermissions
        }
    }

    private class FakePersonalizationRepository(
        private val personalization: Personalization,
    ) : PersonalizationRepository {
        override fun save(personalization: Personalization): Personalization = personalization

        override fun findById(id: PersonalizationId): Personalization? = personalization.takeIf { it.id == id }

        override fun findByCreatorUserId(creatorUserId: UserId): List<Personalization> = emptyList()

        override fun findByStatus(status: PersonalizationStatus): List<Personalization> = emptyList()

        override fun isAssetReferencedByPublicPersonalization(assetId: PersonalizationAssetId): Boolean = false

        override fun isAssetReferencedByReviewablePersonalization(assetId: PersonalizationAssetId): Boolean = false
    }

    private class FakeOwnedPersonalizationRepository : OwnedPersonalizationRepository {
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

        override fun findActiveAccountIds(types: Set<PersonalizationType>): List<AccountId> = emptyList()

        override fun deactivateActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ) = Unit

    override fun delete(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    ) = Unit

    }

    private class FakePurchaseOperationRepository(
        private val pendingPurchaseExists: Boolean,
    ) : PurchaseOperationRepository {
        val saved = mutableListOf<PurchaseOperation>()

        override fun save(operation: PurchaseOperation): PurchaseOperation {
            saved += operation

            return operation
        }

        override fun findById(id: PurchaseOperationId): PurchaseOperation? = null

        override fun findByIdempotencyKey(idempotencyKey: UUID): PurchaseOperation? =
            saved.find { operation -> operation.idempotencyKey == idempotencyKey }

        override fun findByIdForUpdate(id: PurchaseOperationId): PurchaseOperation? = null

        override fun findByRequestIdForUpdate(requestId: UUID): PurchaseOperation? = null

        override fun existsPending(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): Boolean = pendingPurchaseExists
    }

    private class RecordingPersonalizationWallet : PersonalizationWallet {
        val requestedDebits = mutableListOf<PurchaseOperation>()

        override fun requestDebit(operation: PurchaseOperation) {
            requestedDebits += operation
        }
    }
}
