package com.mnemos.playerdirectory.adapter.out.http.cristalix

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

class CristalixPlayerClientTest {
    @Test
    fun `player id batch lookup does not use broken fields projection`() {
        assertBatchEndpointWithoutFieldsProjection(
            methodName = "findByIds",
            expectedPath = "/v1/api/players/batch",
        )
    }

    @Test
    fun `nickname batch lookup does not use broken fields projection`() {
        assertBatchEndpointWithoutFieldsProjection(
            methodName = "findByNames",
            expectedPath = "/v1/api/players/batch/names",
        )
    }

    private fun assertBatchEndpointWithoutFieldsProjection(
        methodName: String,
        expectedPath: String,
    ) {
        val method =
            CristalixPlayerClient::class.java.declaredMethods.single { candidate ->
                candidate.name == methodName
            }
        val postMapping = method.getAnnotation(PostMapping::class.java)
        val requestParameters =
            method.parameterAnnotations
                .flatten()
                .filterIsInstance<RequestParam>()

        assertEquals(listOf(expectedPath), postMapping.value.toList())
        assertFalse(requestParameters.isNotEmpty())
    }
}
