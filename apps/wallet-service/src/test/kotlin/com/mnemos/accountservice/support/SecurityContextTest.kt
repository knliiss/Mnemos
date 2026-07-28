package com.mnemos.accountservice.support

import org.junit.jupiter.api.AfterEach

abstract class SecurityContextTest {
    @AfterEach
    fun clearSecurityContext() {
        clearAuthentication()
    }
}
