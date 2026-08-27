package com.mnemos.payment.adapter.out.persistence.stars

import com.mnemos.payment.adapter.out.persistence.stars.StarPrimePurchasePersistenceMapper.toDomain
import com.mnemos.payment.adapter.out.persistence.stars.StarPrimePurchasePersistenceMapper.toEntity
import com.mnemos.payment.domain.stars.model.StarPrimePurchase
import com.mnemos.payment.domain.stars.repository.StarPrimePurchaseRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class StarPrimePurchaseRepositoryAdapter(
    private val repository: StarPrimePurchaseJpaRepository,
) : StarPrimePurchaseRepository {
    override fun save(purchase: StarPrimePurchase): StarPrimePurchase =
        repository.save(purchase.toEntity()).toDomain()

    override fun findById(id: UUID): StarPrimePurchase? =
        repository.findById(id).orElse(null)?.toDomain()

    override fun findByInvoicePayload(invoicePayload: String): StarPrimePurchase? =
        repository.findByInvoicePayloadForUpdate(invoicePayload)?.toDomain()

    override fun findByTelegramPaymentChargeId(chargeId: String): StarPrimePurchase? =
        repository.findByTelegramPaymentChargeId(chargeId)?.toDomain()
}
