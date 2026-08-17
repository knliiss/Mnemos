package com.mnemos.personalization.adapter.out.persistence.purchase

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId

object PurchaseOperationPersistenceMapper {
    fun PurchaseOperation.toEntity(): PurchaseOperationEntity =
        PurchaseOperationEntity(
            id = id.id,
            requestId = requestId,
            personalizationId = personalizationId.id,
            buyerUserId = buyerUserId.id,
            ownerType = owner.target,
            ownerId = owner.id,
            currency = currency,
            amount = amount,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
            createdAt = createdAt,
        )

    fun PurchaseOperationEntity.toDomain(): PurchaseOperation =
        PurchaseOperation(
            id = PurchaseOperationId(id),
            requestId = requestId,
            personalizationId = PersonalizationId(personalizationId),
            buyerUserId = UserId(buyerUserId),
            owner =
                when (ownerType) {
                    PersonalizationTarget.USER -> PersonalizationOwner.User(UserId(ownerId))
                    PersonalizationTarget.ACCOUNT -> PersonalizationOwner.Account(AccountId(ownerId))
                },
            currency = currency,
            amount = amount,
            createdAt = createdAt,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
        )
}
