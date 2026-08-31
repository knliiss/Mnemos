package com.mnemos.payment.application.stars

import com.mnemos.payment.application.security.port.out.CurrentPaymentUser
import com.mnemos.payment.application.security.port.out.CurrentPaymentUserProvider
import com.mnemos.payment.application.stars.model.StarPrimePackage
import com.mnemos.payment.application.stars.port.out.TelegramStarInvoiceGateway
import com.mnemos.payment.configuration.StarPrimePurchaseProperties
import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class CreateStarPrimePurchaseServiceTest {
    private val currentUserProvider = mock<CurrentPaymentUserProvider>()
    private val packageCatalog = mock<StarPrimePackageCatalog>()
    private val repository = mock<StarPrimePurchaseRepository>()
    private val invoiceGateway = mock<TelegramStarInvoiceGateway>()
    private val transactionManager = mock<PlatformTransactionManager>()
    private val transactionStatus = mock<TransactionStatus>()
    private val properties =
        StarPrimePurchaseProperties().apply {
            invoicePhotoUrl = null
        }
    private val clock = Clock.fixed(NOW, ZoneOffset.UTC)
    private val transactionTemplate = TransactionTemplate(transactionManager)
    private val service =
        CreateStarPrimePurchaseService(
            currentUserProvider = currentUserProvider,
            packageCatalog = packageCatalog,
            repository = repository,
            invoiceGateway = invoiceGateway,
            properties = properties,
            clock = clock,
            transactionTemplate = transactionTemplate,
        )

    @Test
    fun `purchase transaction commits before Telegram invoice creation`() {
        var persistedPurchase: StarPrimePurchase? = null

        whenever(
            transactionManager.getTransaction(any<TransactionDefinition>()),
        ).thenReturn(transactionStatus)
        whenever(packageCatalog.requireById(PACKAGE_ID)).thenReturn(PRIME_PACKAGE)
        whenever(repository.saveAndFlush(any())).thenAnswer { invocation ->
            invocation.getArgument<StarPrimePurchase>(0).also { persistedPurchase = it }
        }
        whenever(repository.findById(any())).thenAnswer { persistedPurchase }
        whenever(repository.save(any())).thenAnswer { it.getArgument<StarPrimePurchase>(0) }
        whenever(invoiceGateway.createInvoice(any())).thenReturn(INVOICE_URL)

        val result =
            service.execute(
                packageId = PACKAGE_ID,
                user = USER,
                telegramRequestId = REQUEST_ID,
            )

        assertEquals(INVOICE_URL, result.invoiceUrl)

        val order =
            inOrder(
                transactionManager,
                repository,
                invoiceGateway,
            )

        order.verify(transactionManager).getTransaction(any<TransactionDefinition>())
        order.verify(repository).saveAndFlush(any())
        order.verify(transactionManager).commit(transactionStatus)
        order.verify(invoiceGateway).createInvoice(any())
        order.verify(transactionManager).getTransaction(any<TransactionDefinition>())
        order.verify(repository).findById(any())
        order.verify(repository).save(any())
        order.verify(transactionManager).commit(transactionStatus)
    }

    @Test
    fun `Telegram failure leaves purchase creation transaction committed`() {
        whenever(
            transactionManager.getTransaction(any<TransactionDefinition>()),
        ).thenReturn(transactionStatus)
        whenever(packageCatalog.requireById(PACKAGE_ID)).thenReturn(PRIME_PACKAGE)
        whenever(repository.saveAndFlush(any())).thenAnswer { it.getArgument<StarPrimePurchase>(0) }
        whenever(invoiceGateway.createInvoice(any())).thenThrow(IllegalStateException("Telegram unavailable"))

        assertThrows(IllegalStateException::class.java) {
            service.execute(
                packageId = PACKAGE_ID,
                user = USER,
                telegramRequestId = REQUEST_ID,
            )
        }

        verify(transactionManager, times(1)).getTransaction(any<TransactionDefinition>())
        verify(transactionManager, times(1)).commit(transactionStatus)
        verify(repository).saveAndFlush(any())
        verify(repository, never()).findById(any())
        verify(repository, never()).save(any())
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-08-31T02:00:00Z")
        val USER_ID: UUID = UUID.fromString("019c114d-69c0-7000-8000-000000000301")
        val USER =
            CurrentPaymentUser(
                userId = USER_ID,
                telegramId = TELEGRAM_ID,
            )
        val PRIME_PACKAGE =
            StarPrimePackage(
                id = PACKAGE_ID,
                primeAmount = 250,
                starsAmount = 250,
            )
        const val TELEGRAM_ID = 931458258L
        const val PACKAGE_ID = "prime-250-stars-250"
        const val REQUEST_ID = "callback-query-42"
        const val INVOICE_URL = "https://t.me/invoice/example"
    }
}
