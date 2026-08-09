package com.mnemos.messaging.core.publisher

data class IntegrationEventRoute(
    val topic: String,
    val version: Int,
    val aggregateType: String,
) {

    init {
        require(topic.isNotBlank()) {
            "Integration event topic cannot be blank"
        }

        require(version > 0) {
            "Integration event version must be positive"
        }

        require(AGGREGATE_TYPE_PATTERN.matches(aggregateType)) {
            "Aggregate type must use uppercase snake case, for example ACCOUNT_ROLE"
        }
    }

    private companion object {
        val AGGREGATE_TYPE_PATTERN =
            Regex("^[A-Z][A-Z0-9_]*$")
    }
}