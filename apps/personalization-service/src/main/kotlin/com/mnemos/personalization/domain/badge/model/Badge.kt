package com.mnemos.personalization.domain.badge.model

import com.mnemos.personalization.domain.personalization.model.HexColor
import java.time.Instant

class Badge(
    val id: BadgeId,
    val code: String,
    val name: String,
    val icon: String,
    val color: HexColor,
    val createdAt: Instant,
) {
    init {
        require(CODE.matches(code)) { "Badge code must use upper snake case" }
        require(name.isNotBlank()) { "Badge name cannot be blank" }
        require(icon.isNotBlank()) { "Badge icon cannot be blank" }
    }

    companion object {
        private val CODE = Regex("^[A-Z][A-Z0-9_]{1,31}$")

        fun create(
            code: String,
            name: String,
            icon: String,
            color: HexColor,
            now: Instant,
        ): Badge =
            Badge(
                id = BadgeId.generate(),
                code = code,
                name = name,
                icon = icon,
                color = color,
                createdAt = now,
            )
    }
}
