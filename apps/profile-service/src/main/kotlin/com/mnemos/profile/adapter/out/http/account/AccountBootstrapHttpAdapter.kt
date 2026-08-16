package com.mnemos.profile.adapter.out.http.account

import com.mnemos.profile.application.profile.model.AccountBootstrapSnapshot
import com.mnemos.profile.application.profile.port.out.AccountBootstrapSource
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountBootstrapHttpAdapter(
    private val accountServiceClient: ProfileAccountServiceClient,
) : AccountBootstrapSource {
    override fun fetch(
        afterAccountId: UUID?,
        limit: Int,
    ): List<AccountBootstrapSnapshot> =
        accountServiceClient
            .fetchBootstrap(
                afterAccountId = afterAccountId,
                limit = limit,
            )
            .orEmpty()
            .map { account ->
                AccountBootstrapSnapshot(
                    accountId = account.accountId,
                    cristalixUuid = account.cristalixUuid,
                    cristalixNickname = account.cristalixNickname,
                    createdAt = account.createdAt,
                )
            }
}
