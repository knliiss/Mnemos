package com.mnemos.event.adapter.out.cristalix.localization

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.event.application.localization.model.SaoLocalizationPackDescriptor
import com.mnemos.event.application.localization.port.out.SaoLocalizationSource
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.security.MessageDigest

@Component
class CristalixSaoLocalizationSource : SaoLocalizationSource {
    private val objectMapper = ObjectMapper()

    private val restClient =
        RestClient
            .builder()
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .build()

    override fun listPacks(): List<SaoLocalizationPackDescriptor> {
        val bytes =
            requireNotNull(
                restClient
                    .get()
                    .uri(MANIFEST_URL)
                    .retrieve()
                    .body(ByteArray::class.java),
            ) {
                "Cristalix localization manifest is empty"
            }

        val languages = objectMapper.readTree(bytes).path("langs")
        require(languages.isArray) {
            "Cristalix localization manifest has no langs array"
        }

        return languages
            .mapNotNull { language ->
                val locale = language.path("lang").asText()
                val pies = language.path("pies")

                if (locale.isBlank() || !pies.isArray) {
                    return@mapNotNull null
                }

                val saoPie =
                    pies.firstOrNull { pie ->
                        pie.path("pieKey").asText() == SAO_PIE_KEY
                    } ?: return@mapNotNull null

                val hash = saoPie.path("hash").asText()
                val url = saoPie.path("url").asText()

                if (hash.isBlank() || url.isBlank()) {
                    return@mapNotNull null
                }

                SaoLocalizationPackDescriptor(
                    locale = locale,
                    hash = hash,
                    url = url,
                )
            }.sortedBy(SaoLocalizationPackDescriptor::locale)
    }

    override fun downloadProperties(descriptor: SaoLocalizationPackDescriptor): Map<String, String> {
        val bytes =
            requireNotNull(
                restClient
                    .get()
                    .uri(descriptor.url)
                    .retrieve()
                    .body(ByteArray::class.java),
            ) {
                "Cristalix SAO localization pack ${descriptor.locale} is empty"
            }

        val actualHash = bytes.sha256Hex()
        require(actualHash == descriptor.hash) {
            "Cristalix SAO localization hash mismatch for ${descriptor.locale}: " +
                "expected ${descriptor.hash}, got $actualHash"
        }

        val propertiesNode = objectMapper.readTree(bytes).path("properties")
        require(propertiesNode.isObject) {
            "Cristalix SAO localization pack ${descriptor.locale} has no properties object"
        }

        val properties =
            propertiesNode
                .fields()
                .asSequence()
                .associate { entry -> entry.key to entry.value.asText() }

        require(properties.isNotEmpty()) {
            "Cristalix SAO localization pack ${descriptor.locale} has no properties"
        }

        return properties
    }

    private fun ByteArray.sha256Hex(): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(this)
            .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private companion object {
        const val MANIFEST_URL = "https://webdata.c7x.dev/client/lang.json"
        const val SAO_PIE_KEY = "sao"
        const val USER_AGENT = "Mnemos/event-service"
    }
}
