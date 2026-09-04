package com.mnemos.realtime.configuration.delivery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Bean

class RealtimeDeliveryConfigurationTest {
    @Test
    fun `realtime stream listener container starts and stops with application context`() {
        val factoryMethod =
            RealtimeDeliveryConfiguration::class.java.declaredMethods
                .single { method -> method.name == "realtimeEventStreamListenerContainer" }
        val bean = factoryMethod.getAnnotation(Bean::class.java)

        assertNotNull(bean)
        assertEquals("start", bean.initMethod)
        assertEquals("stop", bean.destroyMethod)
    }
}
