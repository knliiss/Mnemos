package com.mnemos.common.core.model.id

import java.util.UUID

@JvmInline
value class KeycloakId(val id: UUID) {
    override fun toString(): String = id.toString()
}