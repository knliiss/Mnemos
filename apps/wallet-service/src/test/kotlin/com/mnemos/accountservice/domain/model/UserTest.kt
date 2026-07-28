package com.mnemos.accountservice.domain.model

import com.mnemos.accountservice.domain.model.user.User
import com.mnemos.accountservice.support.TEST_TIME
import com.mnemos.accountservice.support.user
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.common.core.model.user.Username
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class UserTest {
    @Test
    fun `create initializes english localization and no active account`() {
        val user = User.create(TelegramId(123), Username("tester"), KeycloakId(UUID.randomUUID()))

        assertEquals(LocalizationLanguage.EN, user.localizationLanguage)
        assertNull(user.activeAccountId)
        assertEquals(user.createdAt, user.updatedAt)
    }

    @Test
    fun `update username is idempotent`() {
        val user = user(username = Username("tester"))

        user.updateUsername(user, Username("tester"))

        assertEquals(TEST_TIME, user.updatedAt)
    }

    @Test
    fun `update username changes value and timestamp`() {
        val user = user(username = Username("tester"))

        user.updateUsername(user, Username("updated"))

        assertEquals(Username("updated"), user.username)
        assertTrue(user.updatedAt >= TEST_TIME)
    }

    @Test
    fun `update localization is idempotent`() {
        val user = user(localizationLanguage = LocalizationLanguage.EN)

        user.updateLocalization(user, LocalizationLanguage.EN)

        assertEquals(TEST_TIME, user.updatedAt)
    }

    @Test
    fun `update localization changes value`() {
        val user = user(localizationLanguage = LocalizationLanguage.EN)

        user.updateLocalization(user, LocalizationLanguage.UK)

        assertEquals(LocalizationLanguage.UK, user.localizationLanguage)
        assertTrue(user.updatedAt >= TEST_TIME)
    }

    @Test
    fun `change active account sets and replaces account`() {
        val first = AccountId(UUID.randomUUID())
        val second = AccountId(UUID.randomUUID())
        val user = user()

        assertTrue(user.changeActiveAccount(first))
        assertEquals(first, user.activeAccountId)
        assertTrue(user.changeActiveAccount(second))
        assertEquals(second, user.activeAccountId)
    }

    @Test
    fun `change active account is idempotent`() {
        val accountId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = accountId)

        assertFalse(user.changeActiveAccount(accountId))
        assertEquals(TEST_TIME, user.updatedAt)
    }

    @Test
    fun `clear active account is idempotent`() {
        val user = user()

        assertFalse(user.clearActiveAccount())
        assertEquals(TEST_TIME, user.updatedAt)
    }

    @Test
    fun `clear active account removes current account`() {
        val user = user(activeAccountId = AccountId(UUID.randomUUID()))

        assertTrue(user.clearActiveAccount())
        assertNull(user.activeAccountId)
        assertTrue(user.updatedAt >= TEST_TIME)
    }
}
