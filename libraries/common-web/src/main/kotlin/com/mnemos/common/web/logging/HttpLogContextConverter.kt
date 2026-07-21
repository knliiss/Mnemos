package com.mnemos.common.web.logging

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent

class HttpLogContextConverter : ClassicConverter() {
    override fun convert(event: ILoggingEvent): String {
        val requestId = event.mdcPropertyMap[MdcKeys.REQUEST_ID]

        if (requestId.isNullOrBlank()) {
            return ""
        }

        val keycloakId = event.mdcPropertyMap[MdcKeys.KEYCLOAK_ID] ?: "-"
        val userId = event.mdcPropertyMap[MdcKeys.USER_ID] ?: "-"
        val accountId = event.mdcPropertyMap[MdcKeys.ACCOUNT_ID] ?: "-"

        return "requestId=$requestId keycloakId=$keycloakId userId=$userId accountId=$accountId "
    }
}