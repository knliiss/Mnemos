package com.mnemos.account.application.event.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountCreatedV1
import com.mnemos.messaging.core.event.IntegrationEvent

object AccountIntegrationEvents {
    fun created(
        account: Account,
        ownerMember: AccountMember,
    ): IntegrationEvent<AccountCreatedV1> =
        IntegrationEvent(
            type = AccountEventType.ACCOUNT_CREATED,
            aggregateId = account.id.id,
            occurredAt = account.createdAt,
            payload =
                AccountCreatedV1(
                    ownerMemberId = ownerMember.id.id,
                    ownerUserId = ownerMember.userId.id,
                    cristalixUuid =
                        account.cristalixUuid.id,
                    cristalixNickname =
                        account.cristalixNickname.value,
                    maxMembers = account.maxMembers,
                ),
        )
}
