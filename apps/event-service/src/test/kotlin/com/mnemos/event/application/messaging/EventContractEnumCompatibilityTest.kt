package com.mnemos.event.application.messaging

import com.mnemos.event.contracts.shared.ContractBoosterType
import com.mnemos.event.contracts.shared.ContractGlobalEventType
import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.event.contracts.shared.ContractItemType
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EventContractEnumCompatibilityTest {
    @Test
    fun `item types match contract`() {
        assertEquals(
            DomainItemType.entries.map { it.name }.toSet(),
            ContractItemType.entries.map { it.name }.toSet(),
        )
    }

    @Test
    fun `item rarities match contract`() {
        assertEquals(
            DomainItemRarity.entries.map { it.name }.toSet(),
            ContractItemRarity.entries.map { it.name }.toSet(),
        )
    }

    @Test
    fun `booster types match contract`() {
        assertEquals(
            DomainBoosterType.entries.map { it.name }.toSet(),
            ContractBoosterType.entries.map { it.name }.toSet(),
        )
    }

    @Test
    fun `global event types match contract`() {
        assertEquals(
            DomainGlobalEventType.entries.map { it.name }.toSet(),
            ContractGlobalEventType.entries.map { it.name }.toSet(),
        )
    }
}
