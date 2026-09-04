package com.mnemos.realtime.adapter.inbound.http.collector

import com.mnemos.realtime.application.collector.security.AuthenticateCollectorService
import com.mnemos.realtime.application.collector.security.CollectorAccessKeyParser
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/realtime/collector-credentials")
class CollectorCredentialValidationController(
    private val accessKeyParser: CollectorAccessKeyParser,
    private val authenticateCollectorService: AuthenticateCollectorService,
) {
    @GetMapping("/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun validate(
        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false)
        authorization: String?,
    ) {
        val encodedKey =
            authorization
                ?.takeIf { it.startsWith(AUTHORIZATION_PREFIX) }
                ?.removePrefix(AUTHORIZATION_PREFIX)
        val accessKey = encodedKey?.let(accessKeyParser::parse)
        val collectorId = accessKey?.let(authenticateCollectorService::authenticate)

        if (collectorId == null) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }

    private companion object {
        const val AUTHORIZATION_PREFIX = "Collector "
    }
}
