package com.mnemos.personalization.application.purchase

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.personalization.application.purchase.event.PersonalizationPurchaseEventPublisher
import com.mnemos.personalization.application.purchase.support.PurchaseWalletResultValidator
import com.mnemos.personalization.application.purchase.usecase.completewallet.CompletePurchaseWalletService
import com.mnemos.personalization.application.purchase.usecase.failwallet.FailPurchaseWalletService
import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.personalization.contracts.event.PersonalizationPurchaseCompletedV1
import com.mnemos.personalization.contracts.shared.PersonalizationPurchaseStatus
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
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PurchaseWalletUseCasesTest {
    private val now = Instant.parse("2026-08-15T17:30:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `completed wallet result grants ownership and publishes confirmation once`() {
        val personalization = paidCustomNotificationPack(UserId.generate())
        val operation = purchaseOperation(personalization)
        val fixture = fixture(personalization, operation)
        val transactionId = UUID.fromString("019c1152-63c0-7000-8000-000000000011")

        fixture.completeService.execute(
            result = walletResult(operation),
            transactionId = transactionId,
        )
        fixture.completeService.execute(
            result = walletResult(operation),
            transactionId = UUID.fromString("019c1152-63c0-7000-8000-000000000012"),
        )

        val ownership = fixture.ownershipRepository.saved.single()
        val payload =
            fixture.publisher.events
                .single()
                .payload as PersonalizationPurchaseCompletedV1

        assertEquals(PurchaseOperationStatus.CONFIRMED, operation.status)
        assertEquals(transactionId, operation.walletTransactionId)
        assertNull(operation.failureReason)
        assertEquals(operation.owner, ownership.owner)
        assertEquals(personalization.id, ownership.personalizationId)
        assertEquals(personalization.type, ownership.type)
        assertEquals(now, ownership.acquiredAt)
        assertFalse(ownership.active)
        assertEquals(PersonalizationPurchaseStatus.CONFIRMED, payload.status)
        assertEquals(operation.id.id, payload.operationId)
        assertEquals(personalization.id.id, payload.personalizationId)
        assertEquals(transactionId, payload.walletTransactionId)
        assertNull(payload.failureReason)
        assertEquals(1, fixture.operationRepository.saveCalls)
    }

    @Test
    fun `failed wallet result does not grant ownership and publishes failure once`() {
        val personalization = paidCustomNotificationPack(UserId.generate())
        val operation = purchaseOperation(personalization)
        val fixture = fixture(personalization, operation)

        fixture.failService.execute(
            result = walletResult(operation),
            reason = "INSUFFICIENT_BALANCE",
        )
        fixture.failService.execute(
            result = walletResult(operation),
            reason = "SHOULD_BE_IGNORED",
        )

        val payload =
            fixture.publisher.events
                .single()
                .payload as PersonalizationPurchaseCompletedV1

        assertEquals(PurchaseOperationStatus.FAILED, operation.status)
        assertNull(operation.walletTransactionId)
        assertEquals("INSUFFICIENT_BALANCE", operation.failureReason)
        assertTrue(fixture.ownershipRepository.saved.isEmpty())
        assertEquals(PersonalizationPurchaseStatus.FAILED, payload.status)
        assertEquals("INSUFFICIENT_BALANCE", payload.failureReason)
        assertNull(payload.walletTransactionId)
        assertEquals(1, fixture.operationRepository.saveCalls)
    }

    @Test
    fun `wallet result with different amount cannot complete purchase`() {
        val personalization = paidCustomNotificationPack(UserId.generate())
        val operation = purchaseOperation(personalization)
        val fixture = fixture(personalization, operation)
        val result = walletResult(operation).copy(amount = operation.amount + 1)

        assertThrows(IllegalStateException::class.java) {
            fixture.completeService.execute(
                result = result,
                transactionId = UUID.fromString("019c1152-63c0-7000-8000-000000000014"),
            )
        }

        assertEquals(PurchaseOperationStatus.PENDING, operation.status)
        assertTrue(fixture.ownershipRepository.saved.isEmpty())
        assertTrue(fixture.publisher.events.isEmpty())
        assertEquals(0, fixture.operationRepository.saveCalls)
    }

    @Test
    fun `prime account purchase result is correlated to buyer user wallet`() {
        val buyerUserId = UserId.generate()
        val accountId = AccountId.generate()
        val personalization = accountPersonalization(buyerUserId)
        val operation =
            PurchaseOperation.create(
                personalizationId = personalization.id,
                buyerUserId = buyerUserId,
                owner = PersonalizationOwner.Account(accountId),
                currency = Currency.PRIME,
                amount = 25,
                now = now.minusSeconds(30),
            )
        val fixture = fixture(personalization, operation)
        val transactionId = UUID.fromString("019c1152-63c0-7000-8000-000000000015")
        val result =
            WalletResultContext(
                requestId = operation.requestId,
                actorUserId = buyerUserId.id,
                ownerType = WalletOwnerType.USER,
                ownerId = buyerUserId.id,
                currency = Currency.PRIME,
                transactionType = TransactionType.DEBIT,
                source = TransactionSource.PERSONALIZATION_PURCHASE,
                sourceReferenceId = operation.id.id,
                amount = operation.amount,
            )

        fixture.completeService.execute(
            result = result,
            transactionId = transactionId,
        )

        val ownership = fixture.ownershipRepository.saved.single()

        assertEquals(PurchaseOperationStatus.CONFIRMED, operation.status)
        assertEquals(PersonalizationOwner.Account(accountId), ownership.owner)
        assertEquals(transactionId, operation.walletTransactionId)
    }

    private fun fixture(
        personalization: Personalization,
        operation: PurchaseOperation,
    ): Fixture {
        val operationRepository = FakePurchaseOperationRepository(operation)
        val personalizationRepository = FakePersonalizationRepository(personalization)
        val ownershipRepository = FakeOwnedPersonalizationRepository()
        val publisher = RecordingIntegrationEventPublisher()
        val eventPublisher =
            PersonalizationPurchaseEventPublisher(
                publisher = publisher,
                clock = clock,
            )
        val validator = PurchaseWalletResultValidator()

        return Fixture(
            completeService =
                CompletePurchaseWalletService(
                    operationRepository = operationRepository,
                    personalizationRepository = personalizationRepository,
                    ownershipRepository = ownershipRepository,
                    validator = validator,
                    eventPublisher = eventPublisher,
                    clock = clock,
                ),
            failService =
                FailPurchaseWalletService(
                    operationRepository = operationRepository,
                    validator = validator,
                    eventPublisher = eventPublisher,
                ),
            operationRepository = operationRepository,
            ownershipRepository = ownershipRepository,
            publisher = publisher,
        )
    }

    private fun purchaseOperation(personalization: Personalization): PurchaseOperation {
        val buyerUserId = requireNotNull(personalization.creatorUserId)

        return PurchaseOperation.create(
            personalizationId = personalization.id,
            buyerUserId = buyerUserId,
            owner = PersonalizationOwner.User(buyerUserId),
            currency = Currency.PRIME,
            amount = 50,
            now = now.minusSeconds(30),
        )
    }

    private fun walletResult(operation: PurchaseOperation): WalletResultContext {
        val owner = operation.owner as PersonalizationOwner.User

        return WalletResultContext(
            requestId = operation.requestId,
            actorUserId = operation.buyerUserId.id,
            ownerType = WalletOwnerType.USER,
            ownerId = owner.userId.id,
            currency = operation.currency,
            transactionType = TransactionType.DEBIT,
            source = TransactionSource.PERSONALIZATION_PURCHASE,
            sourceReferenceId = operation.id.id,
            amount = operation.amount,
        )
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

    private fun accountPersonalization(userId: UserId): Personalization =
        Personalization.createCreatorDraft(
            creatorUserId = userId,
            type = PersonalizationType.NICK_STYLE,
            name = "Prime Account Style",
            content =
                PersonalizationContent.NickStyle(
                    colors =
                        listOf(
                            GradientStop(
                                color = HexColor("#F5FBFF"),
                                position = 0,
                            ),
                        ),
                    angleDegrees = 90,
                    glow = null,
                ),
            price =
                PersonalizationPrice(
                    currency = Currency.PRIME,
                    amount = 25,
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
        val completeService: CompletePurchaseWalletService,
        val failService: FailPurchaseWalletService,
        val operationRepository: FakePurchaseOperationRepository,
        val ownershipRepository: FakeOwnedPersonalizationRepository,
        val publisher: RecordingIntegrationEventPublisher,
    )

    private class FakePurchaseOperationRepository(
        private val operation: PurchaseOperation,
    ) : PurchaseOperationRepository {
        var saveCalls = 0

        override fun save(operation: PurchaseOperation): PurchaseOperation {
            saveCalls += 1

            return operation
        }

        override fun findById(id: PurchaseOperationId): PurchaseOperation? = operation.takeIf { it.id == id }

        override fun findByIdempotencyKey(idempotencyKey: UUID): PurchaseOperation? =
            operation.takeIf { it.idempotencyKey == idempotencyKey }

        override fun findByIdForUpdate(id: PurchaseOperationId): PurchaseOperation? = operation.takeIf { it.id == id }

        override fun findByRequestIdForUpdate(requestId: UUID): PurchaseOperation? = operation.takeIf { it.requestId == requestId }

        override fun existsPending(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): Boolean =
            operation.owner == owner &&
                operation.personalizationId == personalizationId &&
                operation.status == PurchaseOperationStatus.PENDING
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
        val saved = mutableListOf<OwnedPersonalization>()

        override fun save(ownership: OwnedPersonalization): OwnedPersonalization {
            saved += ownership

            return ownership
        }

        override fun exists(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): Boolean =
            saved.any { ownership ->
                ownership.owner == owner && ownership.personalizationId == personalizationId
            }

        override fun find(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): OwnedPersonalization? =
            saved.firstOrNull { ownership ->
                ownership.owner == owner && ownership.personalizationId == personalizationId
            }

        override fun findActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ): OwnedPersonalization? =
            saved.firstOrNull { ownership ->
                ownership.owner == owner && ownership.type == type && ownership.active
            }

        override fun findAll(owner: PersonalizationOwner): List<OwnedPersonalization> =
            saved.filter { ownership -> ownership.owner == owner }

        override fun findActiveAccountIds(types: Set<PersonalizationType>): List<AccountId> =
            saved
                .filter { ownership -> ownership.active && ownership.type in types }
                .mapNotNull { ownership ->
                    (ownership.owner as? PersonalizationOwner.Account)?.accountId
                }.distinct()

        override fun deactivateActive(
            owner: PersonalizationOwner,
            type: PersonalizationType,
        ) {
            saved
                .filter { ownership -> ownership.owner == owner && ownership.type == type }
                .forEach(OwnedPersonalization::deactivate)
        }
        override fun delete(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ) = Unit
    }

    private class RecordingIntegrationEventPublisher : IntegrationEventPublisher {
        val events = mutableListOf<IntegrationEvent<*>>()

        override fun <T : Any> publish(
            event: IntegrationEvent<T>,
            metadata: EventMetadata,
        ): UUID {
            events += event

            return MESSAGE_ID
        }
    }

    private companion object {
        val MESSAGE_ID: UUID = UUID.fromString("019c1152-63c0-7000-8000-000000000013")
    }
}
