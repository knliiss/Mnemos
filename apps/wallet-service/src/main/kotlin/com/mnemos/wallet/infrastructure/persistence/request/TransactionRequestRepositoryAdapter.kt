package com.mnemos.wallet.infrastructure.persistence.request

import com.mnemos.wallet.domain.model.request.TransactionRequest
import com.mnemos.wallet.domain.model.request.TransactionRequestStatus
import com.mnemos.wallet.domain.model.transaction.RequestId
import com.mnemos.wallet.domain.repository.TransactionRequestRepository
import com.mnemos.wallet.infrastructure.persistence.request.TransactionRequestPersistenceMapper.toDomain
import com.mnemos.wallet.infrastructure.persistence.request.TransactionRequestPersistenceMapper.toEntity
import org.springframework.stereotype.Repository

@Repository
class TransactionRequestRepositoryAdapter(
    private val transactionRequestJpaRepository: TransactionRequestJpaRepository,
    private val transactionRequestPersistenceWriter: TransactionRequestPersistenceWriter,
) : TransactionRequestRepository {
    override fun createIfAbsent(request: TransactionRequest): Boolean =
        transactionRequestPersistenceWriter.insertIfAbsent(request.toEntity())

    override fun findByRequestIdForUpdate(requestId: RequestId): TransactionRequest? =
        transactionRequestJpaRepository.findByRequestIdForUpdate(requestId.id)?.toDomain()

    override fun update(request: TransactionRequest) {
        val updatedRows =
            transactionRequestJpaRepository.updateResult(
                requestId = request.requestId.id,
                status = request.status,
                transactionId = request.transactionId?.id,
                failureReason = request.failureReason,
                completedAt = request.completedAt,
            )

        check(updatedRows == 1) {
            "Expected one transaction request row to be updated, but updated $updatedRows."
        }
    }

    override fun findStatusByRequestId(requestId: RequestId): TransactionRequestStatus? =
        transactionRequestJpaRepository.findStatusByRequestId(requestId.id)
}
