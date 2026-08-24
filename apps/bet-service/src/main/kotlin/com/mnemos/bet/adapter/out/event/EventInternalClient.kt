package com.mnemos.bet.adapter.out.event

import com.mnemos.bet.contracts.shared.BetItemType
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.time.Instant
import java.util.UUID

@FeignClient(
    name = "betEventInternalClient",
    url = "\${mnemos.clients.event-service.url}",
    configuration = [EventInternalFeignConfiguration::class],
    dismiss404 = true,
)
interface EventInternalClient {
    @GetMapping("/internal/v1/collectors/coverage")
    fun collectorCoverage(
        @RequestParam
        from: Instant,
        @RequestParam
        to: Instant,
    ): CollectorCoverageResponse

    @GetMapping("/internal/v1/item-drops/first")
    fun firstItemDrop(
        @RequestParam
        itemName: String,
        @RequestParam
        itemType: BetItemType,
        @RequestParam
        from: Instant,
        @RequestParam
        to: Instant,
    ): FirstItemDropResponse?

    @GetMapping("/api/v1/events/wipes/current")
    fun currentWipe(): EventWipeResponse

    @GetMapping("/api/v1/events/wipes/{wipeId}")
    fun wipe(
        @PathVariable wipeId: UUID,
    ): EventWipeResponse
}
