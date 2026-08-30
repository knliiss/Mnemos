package com.mnemos.common.core.model.id

import java.util.UUID

object TimeOrderedUuidOrder {
    fun compare(
        left: UUID,
        right: UUID,
    ): Int {
        val mostSignificantComparison =
            java.lang.Long.compareUnsigned(
                left.mostSignificantBits,
                right.mostSignificantBits,
            )

        if (mostSignificantComparison != 0) {
            return mostSignificantComparison
        }

        return java.lang.Long.compareUnsigned(
            left.leastSignificantBits,
            right.leastSignificantBits,
        )
    }
}
