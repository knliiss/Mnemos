package com.mnemos.event.domain.verification.model.report

import com.mnemos.event.domain.verification.model.EventFingerprint
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale

fun CollectorEventReport.fingerprint(): EventFingerprint {
    val canonical =
        when (this) {
            is ItemDropCollectorEventReport ->
                "ITEM_DROP|${itemName.normalize()}|${itemType.name}|${droppedFor.normalize()}"

            is BoosterCollectorEventReport ->
                "BOOSTER|${type.name}|${activatedBy.normalize()}"

            is GlobalCollectorEventReport ->
                "GLOBAL|${type.name}"

            is RaidCollectorEventReport ->
                "RAID|${locations.sorted().joinToString(",")}"
        }

    val hash =
        MessageDigest
            .getInstance("SHA-256")
            .digest(canonical.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") {
                "%02x".format(it.toInt() and 0xff)
            }

    return EventFingerprint(hash)
}

private fun String.normalize(): String = trim().lowercase(Locale.ROOT)
