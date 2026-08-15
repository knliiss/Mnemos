package com.mnemos.personalization.application.pricing.model

data class PriceRange(
    val min: Long,
    val max: Long,
) {
    init {
        require(min > 0) { "Minimum price must be positive" }
        require(max >= min) { "Maximum price must not be lower than minimum price" }
    }

    fun contains(amount: Long): Boolean = amount in min..max
}
