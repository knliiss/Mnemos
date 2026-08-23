package com.mnemos.realtime.domain.collector.repository

import com.mnemos.realtime.domain.collector.model.CollectorCredential
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId

interface CollectorCredentialRepository {
    fun create(credential: CollectorCredential): CollectorCredential

    fun save(credential: CollectorCredential): CollectorCredential

    fun findById(id: CollectorCredentialId): CollectorCredential?

    fun findAll(): List<CollectorCredential>
}
