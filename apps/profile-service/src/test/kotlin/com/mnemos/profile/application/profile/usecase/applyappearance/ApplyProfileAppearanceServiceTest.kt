package com.mnemos.profile.application.profile.usecase.applyappearance

import com.mnemos.profile.application.profile.port.out.ProfileAppearanceRepository
import com.mnemos.profile.domain.profile.model.ProfileAppearance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ApplyProfileAppearanceServiceTest {
    private val accountId = UUID.fromString("0198f6ce-e934-7000-8000-000000000001")

    @Test
    fun `newer appearance revision replaces current projection`() {
        val repository = InMemoryProfileAppearanceRepository()
        val service = ApplyProfileAppearanceService(repository)
        val first = appearance(revision = 1, accent = "#111111")
        val second = appearance(revision = 2, accent = "#222222")

        service.execute(first)
        service.execute(second)

        val saved = repository.findByAccountId(accountId)

        assertEquals(2, saved?.revision)
        assertEquals("#222222", saved?.profileTheme?.accent)
    }

    @Test
    fun `stale appearance revision cannot overwrite current projection`() {
        val repository = InMemoryProfileAppearanceRepository()
        val service = ApplyProfileAppearanceService(repository)
        val current = appearance(revision = 3, accent = "#333333")
        val stale = appearance(revision = 2, accent = "#222222")

        service.execute(current)
        service.execute(stale)

        val saved = repository.findByAccountId(accountId)

        assertEquals(3, saved?.revision)
        assertEquals("#333333", saved?.profileTheme?.accent)
    }

    private fun appearance(
        revision: Long,
        accent: String,
    ): ProfileAppearance =
        ProfileAppearance(
            accountId = accountId,
            revision = revision,
            nickStyle = null,
            profileTheme =
                ProfileAppearance.ProfileTheme(
                    personalizationId = UUID.fromString("0198f6ce-e934-7000-8000-000000000002"),
                    background = "#000000",
                    surface = "#101010",
                    accent = accent,
                    text = "#FFFFFF",
                    border = "#303030",
                    glow = null,
                ),
            avatarFrame = null,
            profileEffect = null,
            updatedAt = Instant.parse("2026-08-17T12:00:00Z").plusSeconds(revision),
        )

    private class InMemoryProfileAppearanceRepository : ProfileAppearanceRepository {
        private val appearances = mutableMapOf<UUID, ProfileAppearance>()

        override fun ensureState(accountId: UUID) {
            appearances.putIfAbsent(accountId, ProfileAppearance.empty(accountId))
        }

        override fun save(appearance: ProfileAppearance): ProfileAppearance {
            appearances[appearance.accountId] = appearance

            return appearance
        }

        override fun findByAccountId(accountId: UUID): ProfileAppearance? = appearances[accountId]

        override fun findByAccountIdForUpdate(accountId: UUID): ProfileAppearance? = appearances[accountId]

        override fun findByAccountIds(accountIds: Collection<UUID>): Map<UUID, ProfileAppearance> =
            appearances.filterKeys(accountIds::contains)
    }
}
