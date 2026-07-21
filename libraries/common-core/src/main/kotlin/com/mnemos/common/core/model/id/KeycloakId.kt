package com.mnemos.common.core.model.user

import java.util.UUID

@JvmInline
value class KeycloakId(val value: UUID) {
    override fun toString(): String = value.toString()
}