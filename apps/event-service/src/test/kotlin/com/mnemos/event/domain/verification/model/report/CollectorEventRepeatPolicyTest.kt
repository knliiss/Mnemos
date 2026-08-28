package com.mnemos.event.domain.verification.model.report

import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CollectorEventRepeatPolicyTest {
    @Test
    fun `booster reports from the same collector are not repeatable inside the matching window`() {
        val report =
            BoosterCollectorEventReport(
                type = DomainBoosterType.MONEY,
                activatedBy = "player",
            )

        assertFalse(report.allowsRepeatedObservationFromSameCollector())
    }

    @Test
    fun `item drop reports remain repeatable inside the matching window`() {
        val report =
            ItemDropCollectorEventReport(
                itemName = "Sword",
                itemType = DomainItemType.SWORD,
                droppedFor = "player",
                itemRarity = DomainItemRarity.MYTHICAL,
            )

        assertTrue(report.allowsRepeatedObservationFromSameCollector())
    }
}
