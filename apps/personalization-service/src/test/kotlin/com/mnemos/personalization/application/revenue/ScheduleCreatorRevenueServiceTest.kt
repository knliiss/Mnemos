package com.mnemos.personalization.application.revenue

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.revenue.port.out.CreatorRevenueWallet
import com.mnemos.personalization.configuration.revenue.CreatorRevenueProperties
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class ScheduleCreatorRevenueServiceTest {
    @Test
    fun `self purchase does not create creator revenue`() {
        val userId = UserId.generate()
        val personalization = personalization(userId)
        val purchase = purchase(personalization.id, userId)
        val fixture = fixture(personalization, purchase)

        fixture.service.execute(purchase.id)

        assertTrue(fixture.revenueRepository.operations.isEmpty())
        assertTrue(fixture.wallet.operations.isEmpty())
    }

    @Test
    fun `confirmed purchase creates prime revenue for creator`() {
        val creatorUserId = UserId.generate()
        val buyerUserId = UserId.generate()
        val personalization = personalization(creatorUserId)
        val purchase = purchase(personalization.id, buyerUserId)
        val fixture = fixture(personalization, purchase)

        fixture.service.execute(purchase.id)

        val revenue = fixture.revenueRepository.operations.single()

        assertEquals(creatorUserId, revenue.creatorUserId)
        assertEquals(70L, revenue.amountPrime)
        assertEquals(revenue, fixture.wallet.operations.single())
    }

    @Test
    fun `existing revenue operation prevents duplicate wallet credit`() {
        val creatorUserId = UserId.generate()
        val buyerUserId = UserId.generate()
        val personalization = personalization(creatorUserId)
        val purchase = purchase(personalization.id, buyerUserId)
        val fixture = fixture(personalization, purchase)

        fixture.revenueRepository.purchaseOperationIds += purchase.id

        fixture.service.execute(purchase.id)

        assertTrue(fixture.revenueRepository.operations.isEmpty())
        assertTrue(fixture.wallet.operations.isEmpty())
    }

    private fun fixture(
        personalization: Personalization,
        purchase: PurchaseOperation,
    ): Fixture {
        val purchaseRepository = FakePurchaseOperationRepository(purchase)
        val personalizationRepository = FakePersonalizationRepository(personalization)
        val revenueRepository = FakeCreatorRevenueOperationRepository()
        val wallet = RecordingCreatorRevenueWallet()
        val service =
            ScheduleCreatorRevenueService(
                purchaseOperationRepository = purchaseRepository,
                personalizationRepository = personalizationRepository,
                revenueOperationRepository = revenueRepository,
                revenuePolicy = CreatorRevenuePolicy(CreatorRevenueProperties()),
                wallet = wallet,
                clock = Clock.fixed(Instant.parse("2026-08-15T03:10:00Z"), ZoneOffset.UTC),
            )

        return Fixture(
            service = service,
            revenueRepository = revenueRepository,
            wallet = wallet,
        )
    }

    private fun personalization(creatorUserId: UserId): Personalization =
        Personalization(
            id = PersonalizationId.generate(),
            creatorUserId = creatorUserId,
            type = PersonalizationType.NICK_STYLE,
            origin = PersonalizationOrigin.CREATOR,
            visibility = PersonalizationVisibility.PUBLIC,
            name = "Creator Style",
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
                    glow =
                        GlowStyle(
                            color = HexColor("#FFFFFF"),
                            strength = 20,
                        ),
                ),
            price = PersonalizationPrice(Currency.PRIME, 100),
            status = PersonalizationStatus.PUBLISHED,
            rejectionReason = null,
            createdAt = Instant.parse("2026-08-15T03:00:00Z"),
            updatedAt = Instant.parse("2026-08-15T03:00:00Z"),
        )

    private fun purchase(
        personalizationId: PersonalizationId,
        buyerUserId: UserId,
    ): PurchaseOperation =
        PurchaseOperation(
            id = PurchaseOperationId.generate(),
            requestId = UUID.randomUUID(),
            personalizationId = personalizationId,
            buyerUserId = buyerUserId,
            owner = PersonalizationOwner.Account(AccountId.generate()),
            currency = Currency.PRIME,
            amount = 100,
            createdAt = Instant.parse("2026-08-15T03:05:00Z"),
            status = PurchaseOperationStatus.CONFIRMED,
            walletTransactionId = UUID.randomUUID(),
            failureReason = null,
        )

    private data class Fixture(
        val service: ScheduleCreatorRevenueService,
        val revenueRepository: FakeCreatorRevenueOperationRepository,
        val wallet: RecordingCreatorRevenueWallet,
    )

    private class FakePurchaseOperationRepository(
        private val operation: PurchaseOperation,
    ) : PurchaseOperationRepository {
        override fun save(operation: PurchaseOperation): PurchaseOperation = operation

        override fun findById(id: PurchaseOperationId): PurchaseOperation? =
            operation.takeIf { it.id == id }

        override fun findByIdForUpdate(id: PurchaseOperationId): PurchaseOperation? =
            operation.takeIf { it.id == id }

        override fun findByRequestIdForUpdate(requestId: UUID): PurchaseOperation? = null

        override fun existsPending(
            owner: PersonalizationOwner,
            personalizationId: PersonalizationId,
        ): Boolean = false
    }

    private class FakePersonalizationRepository(
        private val personalization: Personalization,
    ) : PersonalizationRepository {
        override fun save(personalization: Personalization): Personalization = personalization

        override fun findById(id: PersonalizationId): Personalization? =
            personalization.takeIf { it.id == id }

        override fun findByCreatorUserId(creatorUserId: UserId): List<Personalization> =
            listOf(personalization).filter { it.creatorUserId == creatorUserId }

        override fun findByStatus(status: PersonalizationStatus): List<Personalization> =
            listOf(personalization).filter { it.status == status }
    }

    private class FakeCreatorRevenueOperationRepository : CreatorRevenueOperationRepository {
        val operations = mutableListOf<CreatorRevenueOperation>()
        val purchaseOperationIds = mutableSetOf<PurchaseOperationId>()

        override fun save(operation: CreatorRevenueOperation): CreatorRevenueOperation {
            operations += operation
            purchaseOperationIds += operation.purchaseOperationId

            return operation
        }

        override fun findByRequestIdForUpdate(requestId: UUID): CreatorRevenueOperation? = null

        override fun existsByPurchaseOperationId(purchaseOperationId: PurchaseOperationId): Boolean =
            purchaseOperationId in purchaseOperationIds
    }

    private class RecordingCreatorRevenueWallet : CreatorRevenueWallet {
        val operations = mutableListOf<CreatorRevenueOperation>()

        override fun requestCredit(operation: CreatorRevenueOperation) {
            operations += operation
        }
    }
}
