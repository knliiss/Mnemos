package com.mnemos.playerdirectory.application.observability.port.out

import com.mnemos.playerdirectory.application.observability.model.PlayerDirectoryOperationalSnapshot
import java.time.Instant

fun interface PlayerDirectoryOperationalSnapshotReader {
    fun read(
        now: Instant,
        staleBefore: Instant,
    ): PlayerDirectoryOperationalSnapshot
}
