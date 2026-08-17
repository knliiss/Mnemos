package com.mnemos.personalization.application.revenue.support

import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Component

@Component
class CreatorRevenueWalletResultValidator {
    fun validate(
        operation: CreatorRevenueOperation,
        result: WalletResultContext,
    ) {
        check(result.actorUserId == null) {
            "Creator revenue wallet result must not have an actor user"
        }
        check(result.ownerType == WalletOwnerType.USER && result.ownerId == operation.creatorUserId.id) {
            "Creator revenue wallet result owner does not match operation"
        }
        check(result.currency == Currency.PRIME) {
            "Creator revenue wallet result must use PRIME"
        }
        check(result.transactionType == TransactionType.CREDIT) {
            "Creator revenue wallet result must be a credit"
        }
        check(result.source == TransactionSource.CREATOR_REVENUE) {
            "Creator revenue wallet result source does not match operation"
        }
        check(result.sourceReferenceId == operation.id.id) {
            "Creator revenue wallet result source reference does not match operation"
        }
        check(result.amount == operation.amountPrime) {
            "Creator revenue wallet result amount does not match operation"
        }
    }
}
