package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuiltinPersonalizationCatalogTest {
    @Test
    fun `catalog keeps at least twenty definitions per personalization type`() {
        PersonalizationType.entries.forEach { type ->
            val count = BuiltinPersonalizationCatalog.definitions.count { definition -> definition.type == type }

            assertTrue(
                count >= MIN_DEFINITIONS_PER_TYPE,
                "Expected at least $MIN_DEFINITIONS_PER_TYPE built-ins for $type but found $count",
            )
        }
    }

    @Test
    fun `catalog keys are unique`() {
        val definitions = BuiltinPersonalizationCatalog.definitions
        val uniqueKeys = definitions.map { definition -> definition.catalogKey }.toSet()

        assertEquals(definitions.size, uniqueKeys.size)
    }

    private companion object {
        const val MIN_DEFINITIONS_PER_TYPE = 20
    }
}
