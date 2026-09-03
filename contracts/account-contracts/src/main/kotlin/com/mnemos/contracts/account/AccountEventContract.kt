package com.mnemos.contracts.account

object AccountEventContract {
    const val TOPIC = "mnemos.account.events.v1"
    const val VERSION = 1
}

object AccountEventType {
    const val ACCOUNT_CREATED = "account.created"
    const val ACCOUNT_ACTIVATED = "account.activated"
    const val ACCOUNT_DEACTIVATED = "account.deactivated"
    const val ACCOUNT_DELETED = "account.deleted"
    const val ACCOUNT_OWNER_TRANSFERRED = "account.owner.transferred"
    const val ACCOUNT_MEMBER_JOINED = "account.member.joined"
    const val ACCOUNT_MEMBER_LEFT = "account.member.left"
    const val ACCOUNT_MEMBER_REMOVED = "account.member.removed"
    const val USER_CREATED = "user.created"
}

object AccountEventAggregateType {
    const val ACCOUNT_AGGREGATE_TYPE = "ACCOUNT"
    const val USER_AGGREGATE_TYPE = "USER"
}
