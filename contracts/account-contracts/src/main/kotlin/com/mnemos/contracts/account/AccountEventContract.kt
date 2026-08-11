package com.mnemos.contracts.account

object AccountEventContract {
    const val TOPIC = "mnemos.account.events.v1"
    const val VERSION = 1
}

object AccountEventType {
    const val ACCOUNT_CREATED = "account.created"
    const val USER_CREATED = "user.created"
}

object AccountEventAggregateType {
    const val ACCOUNT_AGGREGATE_TYPE = "ACCOUNT"
    const val USER_AGGREGATE_TYPE = "USER"
}