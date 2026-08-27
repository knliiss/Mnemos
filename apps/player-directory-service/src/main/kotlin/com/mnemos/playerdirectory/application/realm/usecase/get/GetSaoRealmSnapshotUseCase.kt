package com.mnemos.playerdirectory.application.realm.usecase.get

import com.mnemos.playerdirectory.application.realm.model.SaoRealmSnapshot

interface GetSaoRealmSnapshotUseCase {
    fun execute(): SaoRealmSnapshot
}
