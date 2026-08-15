package com.mnemos.event.adapter.inbound.collector

import com.mnemos.event.adapter.inbound.collector.dto.CollectorClientMessage
import com.mnemos.event.adapter.inbound.collector.dto.CollectorFieldViolation
import jakarta.validation.Validator
import org.springframework.stereotype.Component

@Component
class CollectorMessageValidator(
    private val validator: Validator,
) {
    fun validate(message: CollectorClientMessage): List<CollectorFieldViolation> =
        validator
            .validate(message)
            .map {
                CollectorFieldViolation(
                    field = it.propertyPath.toString(),
                    message = it.message,
                )
            }.sortedBy { it.field }
}
