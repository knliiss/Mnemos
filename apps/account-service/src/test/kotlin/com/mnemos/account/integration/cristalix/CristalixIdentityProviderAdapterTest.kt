package com.mnemos.account.integration.cristalix

import com.mnemos.account.domain.exception.account.cristalix.CristalixAuthorizationException
import com.mnemos.account.domain.exception.account.cristalix.CristalixRateLimitExceededException
import com.mnemos.account.domain.exception.account.cristalix.CristalixUnavailableException
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.integration.cristalix.client.CristalixClient
import com.mnemos.account.integration.cristalix.config.CristalixApiProperties
import com.mnemos.account.integration.cristalix.dto.CristalixPlayerResponse
import feign.FeignException
import feign.Request
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.UUID

class CristalixIdentityProviderAdapterTest {
    @Test
    fun `passes trimmed credentials and maps player identity`() {
        val playerId = UUID.randomUUID()
        val client = RecordingClient(CristalixPlayerResponse(playerId, "Knalis"))
        val adapter = adapter(client, projectToken = " token ", projectKey = " key ")

        val player = adapter.findByNickname(CristalixNickname("Knalis"))

        assertEquals(playerId, player!!.uuid.value)
        assertEquals("Knalis", player.nickname.value)
        assertEquals("Bearer token", client.authorization)
        assertEquals("key", client.projectKey)
        assertEquals("Knalis", client.nickname)
        assertEquals("playerId,name", client.fields)
    }

    @Test
    fun `returns null for provider not found response`() {
        val client = ThrowingClient(notFound())

        assertNull(adapter(client).findByNickname(CristalixNickname("Missing")))
    }

    @Test
    fun `maps forbidden response to authorization exception`() {
        assertThrows<CristalixAuthorizationException> {
            adapter(ThrowingClient(forbidden())).findByNickname(CristalixNickname("Knalis"))
        }
    }

    @Test
    fun `maps remote rate limit response to domain exception`() {
        assertThrows<CristalixRateLimitExceededException> {
            adapter(ThrowingClient(tooManyRequests())).findByNickname(CristalixNickname("Knalis"))
        }
    }

    @Test
    fun `maps other feign failures to unavailable exception`() {
        assertThrows<CristalixUnavailableException> {
            adapter(ThrowingClient(FeignException.errorStatus("findPlayer", response(500))))
                .findByNickname(CristalixNickname("Knalis"))
        }
    }

    @Test
    fun `maps local rate limiter rejection to rate limit exception without calling client`() {
        val client = RecordingClient(CristalixPlayerResponse(UUID.randomUUID(), "Knalis"))
        val limiter = limiter(permits = 1)
        val adapter = adapter(client, limiter)

        adapter.findByNickname(CristalixNickname("Knalis"))
        assertThrows<CristalixRateLimitExceededException> {
            adapter.findByNickname(CristalixNickname("Knalis"))
        }
        assertEquals(1, client.calls)
    }

    private fun adapter(
        client: CristalixClient,
        limiter: RateLimiter = limiter(permits = 100),
        projectToken: String = "token",
        projectKey: String = "key",
    ) = CristalixIdentityProviderAdapter(
        client = client,
        rateLimiter = limiter,
        properties =
            CristalixApiProperties(
                url = "https://api.cristalix.gg",
                projectToken = projectToken,
                projectKey = projectKey,
                connectTimeout = Duration.ofSeconds(1),
                readTimeout = Duration.ofSeconds(1),
                rateLimit = CristalixApiProperties.RateLimit(100, Duration.ofMinutes(1), Duration.ZERO),
            ),
    )

    private fun limiter(permits: Int): RateLimiter =
        RateLimiter.of(
            UUID.randomUUID().toString(),
            RateLimiterConfig
                .custom()
                .limitForPeriod(permits)
                .limitRefreshPeriod(Duration.ofHours(1))
                .timeoutDuration(Duration.ZERO)
                .build(),
        )

    private fun request(): Request =
        Request.create(
            Request.HttpMethod.GET,
            "https://api.cristalix.gg/player",
            emptyMap(),
            ByteArray(0),
            StandardCharsets.UTF_8,
        )

    private fun response(status: Int): feign.Response =
        feign.Response
            .builder()
            .status(status)
            .reason("failure")
            .request(request())
            .headers(emptyMap())
            .build()

    private fun notFound() = FeignException.NotFound("not found", request(), ByteArray(0), emptyMap())

    private fun forbidden() = FeignException.Forbidden("forbidden", request(), ByteArray(0), emptyMap())

    private fun tooManyRequests() = FeignException.TooManyRequests("rate limit", request(), ByteArray(0), emptyMap())

    private class RecordingClient(
        private val response: CristalixPlayerResponse,
    ) : CristalixClient {
        var calls = 0
        var authorization: String? = null
        var projectKey: String? = null
        var nickname: String? = null
        var fields: String? = null

        override fun findPlayer(
            authorization: String,
            projectKey: String,
            nickname: String,
            fields: String,
        ): CristalixPlayerResponse {
            calls++
            this.authorization = authorization
            this.projectKey = projectKey
            this.nickname = nickname
            this.fields = fields
            return response
        }
    }

    private class ThrowingClient(
        private val error: RuntimeException,
    ) : CristalixClient {
        override fun findPlayer(
            authorization: String,
            projectKey: String,
            nickname: String,
            fields: String,
        ): CristalixPlayerResponse = throw error
    }
}
