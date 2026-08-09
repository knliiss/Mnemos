package com.mnemos.event.domain.booster.repository

import com.mnemos.event.domain.booster.model.BoosterEvent

interface BoosterEventRepository {
    fun save(boosterEvent: BoosterEvent): BoosterEvent
}
