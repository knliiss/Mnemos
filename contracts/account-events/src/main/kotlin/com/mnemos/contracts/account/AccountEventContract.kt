package com.mnemos.contracts.account

object AccountEventContract {
    const val TOPIC = "mnemos.account.events.v1"
    const val VERSION = 1
    const val AGGREGATE_TYPE = "ACCOUNT"
}

object AccountEventType {
    const val ACCOUNT_CREATED = "account.created"
}