package com.mnemos.accountservice.integration.cristalix

import com.mnemos.accountservice.domain.exception.account.CristalixAuthorizationException
import com.mnemos.accountservice.domain.exception.account.CristalixInvalidResponseException
import com.mnemos.accountservice.domain.exception.account.CristalixRateLimitExceededException
import com.mnemos.accountservice.domain.exception.account.CristalixUnavailableException
import com.mnemos.accountservice.domain.model.account.CristalixNickname
import com.mnemos.accountservice.domain.model.account.CristalixUuid
import com.mnemos.accountservice.integration.cristalix.dto.CristalixPlayerResponse
import com.mnemos.accountservice.integration.cristalix.model.CristalixPlayer
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Component
class CristalixIdentityProviderAdapter(
    @Qualifier("cristalixRestClient")
    private val restClient: RestClient,
    @Qualifier("cristalixRateLimiter")
    private val rateLimiter: RateLimiter
) : CristalixIdentityProvider {

    override fun findByNickname(nickname: CristalixNickname): CristalixPlayer? {
        return try {
            rateLimiter.executeSupplier {
                requestByNickname(nickname)
            }
        } catch (_: RequestNotPermitted) {
            throw CristalixRateLimitExceededException()
        }
    }

    private fun requestByNickname(
        nickname: CristalixNickname
    ): CristalixPlayer? {
        val response = try {
            restClient.get()
                .uri { builder ->
                    builder
                        .path("/v1/api/players/by-name/{nickname}")
                        .queryParam("fields", "playerId,name")
                        .build(nickname.value)
                }
                .retrieve()
                .body(CristalixPlayerResponse::class.java)
        } catch (_: HttpClientErrorException.NotFound) {
            return null
        } catch (exception: HttpClientErrorException) {
            throw mapClientError(exception)
        } catch (_: HttpServerErrorException) {
            throw CristalixUnavailableException()
        } catch (_: ResourceAccessException) {
            throw CristalixUnavailableException()
        } catch (_: RestClientException) {
            throw CristalixInvalidResponseException()
        }

        return response?.toModel()
            ?: throw CristalixInvalidResponseException()
    }

    private fun mapClientError(
        exception: HttpClientErrorException
    ): RuntimeException {
        return when (exception.statusCode) {
            HttpStatus.UNAUTHORIZED,
            HttpStatus.FORBIDDEN -> CristalixAuthorizationException()

            HttpStatus.TOO_MANY_REQUESTS ->
                CristalixRateLimitExceededException()

            else -> CristalixInvalidResponseException()
        }
    }

    private fun CristalixPlayerResponse.toModel(): CristalixPlayer {
        return CristalixPlayer(
            uuid = CristalixUuid(playerId),
            nickname = CristalixNickname(name)
        )
    }
}