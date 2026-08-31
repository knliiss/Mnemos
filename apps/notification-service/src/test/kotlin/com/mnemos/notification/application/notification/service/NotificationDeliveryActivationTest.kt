package com.mnemos.notification.application.notification.service

import com.mnemos.notification.adapter.inbound.scheduling.notification.TelegramDeliveryScheduler
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

class NotificationDeliveryActivationTest {
    @Test
    fun `delivery service and scheduler use the same activation property`() {
        val serviceCondition = conditionOn(NotificationDeliveryService::class.java)
        val schedulerCondition = conditionOn(TelegramDeliveryScheduler::class.java)

        assertEquals("mnemos.notification.delivery", serviceCondition.prefix)
        assertArrayEquals(arrayOf("enabled"), serviceCondition.name)
        assertEquals("true", serviceCondition.havingValue)

        assertEquals(serviceCondition.prefix, schedulerCondition.prefix)
        assertArrayEquals(serviceCondition.name, schedulerCondition.name)
        assertEquals(serviceCondition.havingValue, schedulerCondition.havingValue)
    }

    private fun conditionOn(type: Class<*>): ConditionalOnProperty =
        requireNotNull(type.getAnnotation(ConditionalOnProperty::class.java))
}
