package com.mnemos.playerdirectory.adapter.out.http.account

import com.mnemos.playerdirectory.application.accountlink.model.AccountBootstrapSnapshot
import com.mnemos.playerdirectory.application.accountlink.port.out.AccountBootstrapSource
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountBootstrapHttpAdapter(
    private val accountServiceClient: PlayerDirectoryAccountServiceClient,
) : AccountBootstrapSource {
    override fun fetch(
        afterAccountId: UUID?,
        limit: Int,
    ): List<AccountBootstrapSnapshot> =
        accountServiceClient
            .fetchBootstrap(
                afterAccountId = afterAccountId,
                limit = limit,
            ).orEmpty()
            .map { account ->
                AccountBootstrapSnapshot(
                    accountId = account.accountId,
                    cristalixUuid = account.cristalixUuid,
                    createdAt = account.createdAt,
                )
            }
}
