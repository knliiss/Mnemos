package com.mnemos.notification.application.itemdrop.internal

import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventCommand
import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventUseCase
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service
class InternalDropArbitrationService(
    private val processItemDropEventUseCase: ProcessItemDropEventUseCase,
) {
    private val executor =
        Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "mnemos-internal-drop-arbiter").apply {
                isDaemon = true
            }
        }

    private val pending = mutableMapOf<UUID, PendingInternalDrop>()
    private val recentStandard = mutableMapOf<DropKey, MutableList<RecentObservation>>()
    private val recentInternal = mutableMapOf<DropKey, MutableList<RecentObservation>>()

    fun submitInternal(command: ProcessItemDropEventCommand) {
        if (!isSupportedInternalDrop(command)) return

        val key = command.dropKey()
        val now = Instant.now()

        synchronized(this) {
            prune(now)

            if (recentStandard.hasMatching(key, command.occurredAt)) {
                return
            }

            if (
                pending.values.any {
                    it.key == key &&
                        matchesTime(it.command.occurredAt, command.occurredAt)
                }
            ) {
                return
            }

            val future =
                executor.schedule(
                    { dispatchInternal(command.messageId) },
                    INTERNAL_GRACE.toMillis(),
                    TimeUnit.MILLISECONDS,
                )

            pending[command.messageId] =
                PendingInternalDrop(
                    key = key,
                    command = command,
                    future = future,
                )
        }
    }

    fun shouldDispatchStandard(command: ProcessItemDropEventCommand): Boolean {
        val key = command.dropKey()
        val now = Instant.now()

        synchronized(this) {
            prune(now)
            recentStandard.add(key, command.occurredAt, now)

            val matchingPending =
                pending
                    .filterValues {
                        it.key == key &&
                            matchesTime(it.command.occurredAt, command.occurredAt)
                    }.keys
                    .toList()

            matchingPending.forEach { messageId ->
                pending.remove(messageId)?.future?.cancel(false)
            }

            return !recentInternal.hasMatching(key, command.occurredAt)
        }
    }

    private fun dispatchInternal(messageId: UUID) {
        val command =
            synchronized(this) {
                val now = Instant.now()
                prune(now)

                val pendingDrop = pending.remove(messageId) ?: return

                if (recentStandard.hasMatching(pendingDrop.key, pendingDrop.command.occurredAt)) {
                    return
                }

                recentInternal.add(
                    key = pendingDrop.key,
                    observedAt = pendingDrop.command.occurredAt,
                    receivedAt = now,
                )

                pendingDrop.command
            }

        processItemDropEventUseCase.execute(command)
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
    ): Boolean {
        return this[key]?.any { matchesTime(it.observedAt, observedAt) } == true
    }

    private fun matchesTime(
        first: Instant,
        second: Instant,
    ): Boolean = Duration.between(first, second).abs() <= MATCHING_WINDOW

    private fun isSupportedInternalDrop(command: ProcessItemDropEventCommand): Boolean {
        if (command.itemRarity !in INTERNAL_RARITIES) return false

        return command.itemRarity != NotificationItemRarity.SECRET ||
            command.itemType in SECRET_ITEM_TYPES
    }

    @PreDestroy
    fun shutdown() {
        synchronized(this) {
            pending.values.forEach { it.future.cancel(false) }
            pending.clear()
            recentStandard.clear()
            recentInternal.clear()
        }

        executor.shutdownNow()
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

    private data class PendingInternalDrop(
        val key: DropKey,
        val command: ProcessItemDropEventCommand,
        val future: ScheduledFuture<*>,
    )

    private data class RecentObservation(
        val observedAt: Instant,
        val receivedAt: Instant,
    )

    private companion object {
        val INTERNAL_GRACE: Duration = Duration.ofSeconds(5)
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
