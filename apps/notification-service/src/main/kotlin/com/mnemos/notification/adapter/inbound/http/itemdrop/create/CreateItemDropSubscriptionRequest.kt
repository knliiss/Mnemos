package com.mnemos.notification.adapter.inbound.http.itemdrop.create

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateItemDropSubscriptionRequest(
    @field:Size(min = 4, max = 20)
    @field:Pattern(regexp = "^[\\p{L}0-9_]+$")
    val droppedFor: String?,
    @field:Size(min = 1, max = 24)
    val itemName: String?,
) {
    @get:AssertTrue(message = "At least one filter must be specified")
    @get:JsonIgnore
    val hasFilter: Boolean
        get() = droppedFor != null || itemName != null
}
