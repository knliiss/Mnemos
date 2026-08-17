package com.mnemos.social.contracts

object SocialEventContract {
    const val TOPIC = "mnemos.social.events.v1"
    const val VERSION = 1
    const val AGGREGATE_TYPE = "SOCIAL"
}

object SocialEventType {
    const val DROP_SPOTLIGHT_PUBLISHED = "social.drop-spotlight.published"
    const val ACCOUNT_SOCIAL_SUMMARY_CHANGED = "social.account-summary.changed"
}
