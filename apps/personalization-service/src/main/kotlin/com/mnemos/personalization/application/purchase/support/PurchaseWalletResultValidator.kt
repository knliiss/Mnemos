package com.mnemos.personalization.application.purchase.support

import com.mnemos.personalization.application.purchase.PurchaseWalletOwnerResolver
import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import org.springframework.stereotype.Component

@Component
class PurchaseWalletResultValidator {
    fun validate(
        operation: PurchaseOperation,
        result: WalletResultContext,
    ) {
        val expectedOwner = PurchaseWalletOwnerResolver.resolve(operation)

        check(result.actorUserId == operation.buyerUserId.id) {
            "Personalization purchase wallet result actor does not match buyer"
        }
        check(result.ownerType == expectedOwner.type && result.ownerId == expectedOwner.id) {
            "Personalization purchase wallet result owner does not match operation payer"
        }
        check(result.currency == operation.currency) {
            "Personalization purchase wallet result currency does not match operation"
        }
        check(result.transactionType == TransactionType.DEBIT) {
            "Personalization purchase wallet result must be a debit"
        }
        check(result.source == TransactionSource.PERSONALIZATION_PURCHASE) {
            "Personalization purchase wallet result source does not match operation"
        }
        check(result.sourceReferenceId == operation.id.id) {
            "Personalization purchase wallet result source reference does not match operation"
        }
        check(result.amount == operation.amount) {
            "Personalization purchase wallet result amount does not match operation"
        }
    }
}
