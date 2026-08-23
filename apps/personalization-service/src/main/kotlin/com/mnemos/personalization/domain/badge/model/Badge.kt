package com.mnemos.personalization.domain.badge.model

import com.mnemos.personalization.domain.personalization.model.HexColor
import java.time.Instant

class Badge(
    val id: BadgeId,
    val code: String,
    val name: String,
    val imageObjectKey: String,
    val color: HexColor,
    val createdAt: Instant,
) {
    init {
        require(CODE.matches(code)) { "Badge code must use upper snake case" }
        require(name.isNotBlank()) { "Badge name cannot be blank" }
        require(name.length <= MAX_NAME_LENGTH) { "Badge name is too long" }
        require(IMAGE_OBJECT_KEY.matches(imageObjectKey)) { "Badge image object key is invalid" }
    }

    companion object {
        private const val MAX_NAME_LENGTH = 64
        private val CODE = Regex("^[A-Z][A-Z0-9_]{1,31}$")
        private val IMAGE_OBJECT_KEY = Regex("^badges/[0-9a-fA-F-]{36}\\.png$")

        fun create(
            code: String,
            name: String,
            color: HexColor,
            now: Instant,
        ): Badge {
            val id = BadgeId.generate()

            return Badge(
                id = id,
                code = code,
                name = name,
                imageObjectKey = "badges/${id.id}.png",
                color = color,
                createdAt = now,
            )
        }
    }
}
