package com.mnemos.playerdirectory.application.realm.port.out

import com.mnemos.playerdirectory.application.realm.model.SaoRealmPlayer
import java.time.Instant

interface SaoRealmReader {
    fun findFreshOnlinePlayers(cutoff: Instant): List<SaoRealmPlayer>
}
