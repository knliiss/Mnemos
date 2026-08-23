package com.mnemos.realtime.domain.collector.repository

import com.mnemos.realtime.domain.collector.model.CollectorActivation

interface CollectorActivationRepository {
    fun create(activation: CollectorActivation): CollectorActivation

    fun save(activation: CollectorActivation): CollectorActivation

    fun findByTokenHashForUpdate(tokenHash: String): CollectorActivation?
}
