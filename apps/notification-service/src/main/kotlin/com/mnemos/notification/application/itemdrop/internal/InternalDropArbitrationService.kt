package com.mnemos.notification.application.itemdrop.internal

import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventCommand
import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventUseCase
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Locale

@Service
class InternalDropArbitrationService(
    private val processItemDropEventUseCase: ProcessItemDropEventUseCase,
) {
    private val recentStandard = mutableMapOf<DropKey, MutableList<RecentObservation>>()
    private val recentInternal = mutableMapOf<DropKey, MutableList<RecentObservation>>()

    fun submitInternal(command: ProcessItemDropEventCommand) {
        if (!isSupportedInternalDrop(command)) return

        val shouldDispatch =
            synchronized(this) {
                val key = command.dropKey()
                val now = Instant.now()

                prune(now)

                if (
                    recentStandard.hasMatching(key, command.occurredAt) ||
                    recentInternal.hasMatching(key, command.occurredAt)
                ) {
                    false
                } else {
                    recentInternal.add(
                        key = key,
                        observedAt = command.occurredAt,
                        receivedAt = now,
                    )
                    true
                }
            }

        if (shouldDispatch) {
            processItemDropEventUseCase.execute(command)
        }
    }

    fun shouldDispatchStandard(command: ProcessItemDropEventCommand): Boolean {
        val key = command.dropKey()
        val now = Instant.now()

        return synchronized(this) {
            prune(now)

            if (recentInternal.hasMatching(key, command.occurredAt)) {
                false
            } else {
                recentStandard.add(key, command.occurredAt, now)
                true
            }
        }
    }

    private fun prune(now: Instant) {
        recentStandard.prune(now)
        recentInternal.prune(now)
    }

    private fun MutableMap<DropKey, MutableList<RecentObservation>>.prune(now: Instant) {
        entries.removeIf { entry ->
            entry.value.removeIf {
                Duration.between(it.receivedAt, now) > SUPPRESSION_TTL
            }
            entry.value.isEmpty()
        }
    }

    private fun MutableMap<DropKey, MutableList<RecentObservation>>.add(
        key: DropKey,
        observedAt: Instant,
        receivedAt: Instant,
    ) {
        computeIfAbsent(key) { mutableListOf() }
            .add(
                RecentObservation(
                    observedAt = observedAt,
                    receivedAt = receivedAt,
                ),
            )
    }

    private fun Map<DropKey, List<RecentObservation>>.hasMatching(
        key: DropKey,
        observedAt: Instant,
    ): Boolean = this[key]?.any { matchesTime(it.observedAt, observedAt) } == true

    private fun matchesTime(
        first: Instant,
        second: Instant,
    ): Boolean = Duration.between(first, second).abs() <= MATCHING_WINDOW

    private fun isSupportedInternalDrop(command: ProcessItemDropEventCommand): Boolean {
        if (command.itemRarity !in INTERNAL_RARITIES) return false

        return command.itemRarity != NotificationItemRarity.SECRET ||
            command.itemType in SECRET_ITEM_TYPES
    }

    private fun ProcessItemDropEventCommand.dropKey(): DropKey =
        DropKey(
            droppedFor = droppedFor.normalize(),
            itemName = itemName.normalize(),
            itemType = itemType,
        )

    private fun String.normalize(): String =
        trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")

    private data class DropKey(
        val droppedFor: String,
        val itemName: String,
        val itemType: NotificationItemType,
    )

    private data class RecentObservation(
        val observedAt: Instant,
        val receivedAt: Instant,
    )

    private companion object {
        val MATCHING_WINDOW: Duration = Duration.ofSeconds(5)
        val SUPPRESSION_TTL: Duration = Duration.ofSeconds(30)

        val INTERNAL_RARITIES =
            setOf(
                NotificationItemRarity.MYTHICAL,
                NotificationItemRarity.SECRET,
            )

        val SECRET_ITEM_TYPES =
            setOf(
                NotificationItemType.SWORD,
                NotificationItemType.PET,
            )
    }
}
