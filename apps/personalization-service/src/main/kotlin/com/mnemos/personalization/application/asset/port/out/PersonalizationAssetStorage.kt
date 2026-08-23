package com.mnemos.personalization.application.asset.port.out

import com.mnemos.personalization.application.asset.model.PersonalizationAssetImage

interface PersonalizationAssetStorage {
    fun store(
        objectKey: String,
        image: PersonalizationAssetImage,
    )

    fun load(objectKey: String): PersonalizationAssetImage?

    fun delete(objectKey: String)
}
