package com.mnemos.event.adapter.out.cristalix.localization

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.event.application.localization.model.SaoLocalizationPackDescriptor
import com.mnemos.event.application.localization.port.out.SaoLocalizationSource
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.security.MessageDigest

@Component
class CristalixSaoLocalizationSource(
    private val objectMapper: ObjectMapper,
) : SaoLocalizationSource {
    private val restClient =
        RestClient
            .builder()
            .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
            .build()

    override fun listPacks(): List<SaoLocalizationPackDescriptor> {
        val manifest =
            requireNotNull(
                restClient
                    .get()
                    .uri(MANIFEST_URL)
                    .retrieve()
                    .body(LocalizationManifest::class.java),
            ) {
                "Cristalix localization manifest is empty"
            }

        return manifest.langs
            .mapNotNull { language ->
                language.pies
                    .firstOrNull { pie -> pie.pieKey == SAO_PIE_KEY }
                    ?.let { pie ->
                        SaoLocalizationPackDescriptor(
                            locale = language.lang,
                            hash = pie.hash,
                            url = pie.url,
                        )
                    }
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

        val pack = objectMapper.readValue(bytes, LocalizationPack::class.java)
        require(pack.properties.isNotEmpty()) {
            "Cristalix SAO localization pack ${descriptor.locale} has no properties"
        }

        return pack.properties
    }

    private fun ByteArray.sha256Hex(): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(this)
            .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private data class LocalizationManifest(
        val langs: List<LocalizationManifestLanguage>,
    )

    private data class LocalizationManifestLanguage(
        val lang: String,
        val pies: List<LocalizationPie>,
    )

    private data class LocalizationPie(
        @field:JsonProperty("pieKey")
        val pieKey: String,
        val hash: String,
        val url: String,
    )

    private data class LocalizationPack(
        val properties: Map<String, String>,
    )

    private companion object {
        const val MANIFEST_URL = "https://webdata.c7x.dev/client/lang.json"
        const val SAO_PIE_KEY = "sao"
        const val USER_AGENT = "Mnemos/event-service"
    }
}
