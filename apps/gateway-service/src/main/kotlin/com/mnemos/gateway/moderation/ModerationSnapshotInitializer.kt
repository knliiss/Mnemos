package com.mnemos.gateway.moderation

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class ModerationSnapshotInitializer(
    private val client: ModerationSnapshotClient,
    private val cache: BanCache,
) {
    @PostConstruct
    fun initialize() {
        cache.replace(client.load())
    }
}
