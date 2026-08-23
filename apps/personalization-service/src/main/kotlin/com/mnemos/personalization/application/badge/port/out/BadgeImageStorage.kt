package com.mnemos.personalization.application.badge.port.out

import com.mnemos.personalization.application.badge.model.BadgeImageContent

interface BadgeImageStorage {
    fun store(
        objectKey: String,
        image: BadgeImageContent,
    )

    fun load(objectKey: String): BadgeImageContent?

    fun delete(objectKey: String)
}
