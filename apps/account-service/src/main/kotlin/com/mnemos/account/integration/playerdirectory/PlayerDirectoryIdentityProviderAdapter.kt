package com.mnemos.account.integration.playerdirectory

import com.mnemos.account.application.model.account.ResolvedPlayerIdentity
import com.mnemos.account.application.port.out.PlayerIdentityProvider
import com.mnemos.account.domain.exception.account.cristalix.CristalixUnavailableException
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.contracts.playerdirectory.internal.v1.InternalResolvePlayerRequest
import feign.FeignException
import org.springframework.stereotype.Component

@Component
class PlayerDirectoryIdentityProviderAdapter(
    private val client: PlayerDirectoryClient,
) : PlayerIdentityProvider {
    override fun resolveByNickname(nickname: CristalixNickname): ResolvedPlayerIdentity? =
        try {
            val player =
                client.resolve(
                    InternalResolvePlayerRequest(
                        nickname = nickname.value,
                    ),
                )

            ResolvedPlayerIdentity(
                uuid = CristalixUuid(player.playerId),
                nickname = CristalixNickname(player.nickname),
            )
        } catch (_: FeignException.NotFound) {
            null
        } catch (exception: FeignException) {
            throw CristalixUnavailableException()
        }
}
