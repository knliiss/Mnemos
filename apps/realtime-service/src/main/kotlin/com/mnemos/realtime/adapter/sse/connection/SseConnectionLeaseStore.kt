package com.mnemos.realtime.adapter.sse.connection

import java.util.UUID

interface SseConnectionLeaseStore {
    fun acquire(
        userId: UUID,
        clientIp: String,
        connectionId: UUID,
    ): SseConnectionLeaseAcquireResult

    fun renew(
        userId: UUID,
        clientIp: String,
        connectionId: UUID,
    ): Boolean

    fun release(
        userId: UUID,
        clientIp: String,
        connectionId: UUID,
    )
}

enum class SseConnectionLeaseAcquireResult {
    ACQUIRED,
    USER_LIMIT_EXCEEDED,
    CLIENT_IP_LIMIT_EXCEEDED,
    TOTAL_LIMIT_EXCEEDED,
}
