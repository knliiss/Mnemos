package com.mnemos.accountservice.integration.cristalix

import com.mnemos.accountservice.domain.exception.account.cristalix.CristalixAuthorizationException
import com.mnemos.accountservice.domain.exception.account.cristalix.CristalixRateLimitExceededException
import com.mnemos.accountservice.domain.exception.account.cristalix.CristalixUnavailableException
import com.mnemos.accountservice.domain.model.account.CristalixNickname
import com.mnemos.accountservice.domain.model.account.CristalixUuid
import com.mnemos.accountservice.integration.cristalix.client.CristalixClient
import com.mnemos.accountservice.integration.cristalix.config.CristalixApiProperties
import com.mnemos.accountservice.integration.cristalix.dto.CristalixPlayerResponse
import com.mnemos.accountservice.integration.cristalix.model.CristalixPlayer
import feign.FeignException
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class CristalixIdentityProviderAdapter(
    private val client: CristalixClient,
    @Qualifier("cristalixRateLimiter")
    private val rateLimiter: RateLimiter,
    private val properties: CristalixApiProperties,
    private val logger: Logger = Logger.getLogger(CristalixIdentityProviderAdapter::class.java.name),
) : CristalixIdentityProvider {
    override fun findByNickname(nickname: CristalixNickname): CristalixPlayer? =
        try {
            rateLimiter.executeSupplier {
                client
                    .findPlayer(
                        authorization = "Bearer ${properties.projectToken.trim()}",
                        projectKey = properties.projectKey.trim(),
                        nickname = nickname.value,
                    ).toModel()
            }
        } catch (_: FeignException.NotFound) {
            logger.info { "Cristalix identity provider not found: $nickname" }
            null
        } catch (e: FeignException.Forbidden) {
            logger.info { "Cristalix identity provider Forbidden: $nickname" }
            throw CristalixAuthorizationException()
        } catch (e: FeignException.TooManyRequests) {
            logger.info { "Cristalix identity provider TooManyRequests: $nickname" }
            throw CristalixRateLimitExceededException()
        } catch (e: FeignException) {
            logger.warning { "Cristalix CristalixUnavailableException $nickname" }
            throw CristalixUnavailableException()
        } catch (_: RequestNotPermitted) {
            logger.info { "Cristalix identity provider not permitted: $nickname" }
            throw CristalixRateLimitExceededException()
        } finally {
            logger.info { "CristalixIdentityProvider findByNickname: $nickname" }
        }

    private fun CristalixPlayerResponse.toModel(): CristalixPlayer {
        logger.info { "CristalixIdentityProvider toModel: $id" }
        return CristalixPlayer(
            uuid = CristalixUuid(playerId),
            nickname = CristalixNickname(name),
        )
    }
}
