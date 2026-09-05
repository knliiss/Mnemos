package com.mnemos.event.adapter.out.cristalix.localization

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

class CristalixSaoLocalizationSourceTest {
    @Test
    fun `constructs without a Spring managed RestClient builder`() {
        CristalixSaoLocalizationSource(
            objectMapper = ObjectMapper(),
        )
    }
}
