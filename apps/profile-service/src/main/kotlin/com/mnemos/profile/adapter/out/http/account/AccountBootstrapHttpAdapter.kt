package com.mnemos.profile.adapter.out.http.account

import com.mnemos.contracts.account.internal.v1.InternalAccountBootstrapResponse
import com.mnemos.profile.application.profile.model.AccountBootstrapSnapshot
import com.mnemos.profile.application.profile.port.out.AccountBootstrapSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.UUID

@Component
class AccountBootstrapHttpAdapter(
    restClientBuilder: RestClient.Builder,
    @Value("\${mnemos.profile.account-service.base-url}") baseUrl: String,
    @Value("\${mnemos.profile.account-service.api-key}") apiKey: String,
) : AccountBootstrapSource {
    private val client =
        restClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader("X-Mnemos-Service", "profile-service")
            .defaultHeader("X-Mnemos-API-Key", apiKey)
            .build()

    override fun fetch(
        afterAccountId: UUID?,
        limit: Int,
    ): List<AccountBootstrapSnapshot> {
        val response =
            if (afterAccountId == null) {
                client
                    .get()
                    .uri("/internal/v1/accounts/bootstrap?limit={limit}", limit)
                    .retrieve()
                    .body(RESPONSE_TYPE)
            } else {
                client
                    .get()
                    .uri(
                        "/internal/v1/accounts/bootstrap?afterAccountId={afterAccountId}&limit={limit}",
                        afterAccountId,
                        limit,
                    ).retrieve()
                    .body(RESPONSE_TYPE)
            }

        return response
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

    private companion object {
        val RESPONSE_TYPE =
            object : ParameterizedTypeReference<List<InternalAccountBootstrapResponse>>() {}
    }
}
