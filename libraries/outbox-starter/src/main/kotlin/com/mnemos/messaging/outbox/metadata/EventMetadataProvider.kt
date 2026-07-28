package com.mnemos.messaging.outbox.metadata

import com.mnemos.messaging.core.event.EventMetadata

fun interface EventMetadataProvider {
    fun current(): EventMetadata
}