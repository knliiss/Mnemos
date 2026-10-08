package com.mnemos.realtime.botagent

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import tools.jackson.databind.json.JsonMapper

class BotAgentSessionsTest {
    private val sessions = BotAgentSessions(JsonMapper.builder().build(), mock(BotAgentIdentityClient::class.java))
    private val account = "00000000-0000-0000-0000-000000000001"

    @Test
    fun `accepts only approved account and manager actions`() {
        assertTrue(sessions.allowed("GET", "/api/accounts"))
        assertTrue(sessions.allowed("GET", "/api/account/$account/logs?after=12&limit=50"))
        assertTrue(sessions.allowed("POST", "/api/account/$account/settings"))
        assertTrue(sessions.allowed("POST", "/api/proxy-pools"))
        assertTrue(sessions.allowed("GET", "/api/groups"))
        assertTrue(sessions.allowed("POST", "/api/groups"))
        assertFalse(sessions.allowed("GET", "/api/groups?secret=true"))
    }

    @Test
    fun `rejects internal services and path traversal`() {
        assertFalse(sessions.allowed("GET", "/api/mnemos"))
        assertFalse(sessions.allowed("POST", "/api/runtime/shutdown"))
        assertFalse(sessions.allowed("GET", "/api/accounts?all=true"))
        assertFalse(sessions.allowed("GET", "/api/account/$account/../network"))
        assertFalse(sessions.allowed("GET", "/api/account/%2e%2e/logs"))
        assertFalse(sessions.allowed("POST", "/api/account/$account/logs"))
    }
}
