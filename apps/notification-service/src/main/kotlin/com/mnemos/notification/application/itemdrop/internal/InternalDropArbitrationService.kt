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

    private val pending = mutableMapOf<DropKey, PendingInternalDrop>()
    private val recentStandard = mutableMapOf<DropKey, Instant>()
    private val recentInternal = mutableMapOf<DropKey, Instant>()

    fun submitInternal(command: ProcessItemDropEventCommand) {
        if (!isSupportedInternalDrop(command)) return

        val key = command.dropKey()
        val now = Instant.now()

        synchronized(this) {
            prune(now)

            if (recentStandard.containsKey(key) || pending.containsKey(key)) {
                return
            }

            val future =
                executor.schedule(
                    { dispatchInternal(key, command) },
                    INTERNAL_GRACE.toMillis(),
                    TimeUnit.MILLISECONDS,
                )

            pending[key] = PendingInternalDrop(future)
        }
    }

    fun shouldDispatchStandard(command: ProcessItemDropEventCommand): Boolean {
        val key = command.dropKey()
        val now = Instant.now()

        synchronized(this) {
            prune(now)
            recentStandard[key] = now
            pending.remove(key)?.future?.cancel(false)

            return !recentInternal.containsKey(key)
        }
    }

    private fun dispatchInternal(
        key: DropKey,
        command: ProcessItemDropEventCommand,
    ) {
        synchronized(this) {
            val now = Instant.now()
            prune(now)

            if (pending.remove(key) == null) return
            if (recentStandard.containsKey(key)) return

            recentInternal[key] = now
        }

        processItemDropEventUseCase.execute(command)
    }

    private fun prune(now: Instant) {
        recentStandard.entries.removeIf { Duration.between(it.value, now) > SUPPRESSION_TTL }
        recentInternal.entries.removeIf { Duration.between(it.value, now) > SUPPRESSION_TTL }
    }

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
        val future: ScheduledFuture<*>,
    )

    private companion object {
        val INTERNAL_GRACE: Duration = Duration.ofSeconds(5)
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
