package com.mnemos.accountservice.application.event.account

import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountCreatedV1
import com.mnemos.messaging.core.event.IntegrationEvent

object AccountIntegrationEvents {

    fun created(
        account: Account,
        ownerMember: AccountMember
    ): IntegrationEvent<AccountCreatedV1> =
        IntegrationEvent(
            eventType = AccountEventType.ACCOUNT_CREATED,
            eventVersion = 1,
            aggregateType = AccountEventContract.AGGREGATE_TYPE,
            aggregateId = account.id.value,
            occurredAt = account.createdAt,
            payload = AccountCreatedV1(
                ownerMemberId = ownerMember.id.value,
                ownerUserId = ownerMember.userId.value,
                cristalixUuid = account.cristalixUuid.value,
                cristalixNickname = account.cristalixNickname.value,
                maxMembers = account.maxMembers
            )
        )
}