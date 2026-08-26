package com.mnemos.profile.application.profile.usecase.get

import com.mnemos.profile.application.common.pagination.PageResult
import com.mnemos.profile.application.profile.port.out.ProfileAppearanceRepository
import com.mnemos.profile.application.profile.port.out.ProfileAppearanceSource
import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.application.profile.usecase.search.SearchPublicProfilesQuery
import com.mnemos.profile.domain.profile.model.ProfileAppearance
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class GetPublicProfileServiceTest {
    @Test
    fun `reconciles empty appearance from authoritative source`() {
        val accountId = UUID.fromString("019c1ab0-1200-7000-8000-000000000101")
        val authoritativeAppearance = appearance(accountId, revision = 7, updatedAt = NOW)
        val fixture = Fixture(profile(accountId))
        fixture.source.result = authoritativeAppearance

        val result = fixture.service.execute(accountId)

        assertSame(authoritativeAppearance, result.appearance)
        assertSame(authoritativeAppearance, fixture.appearances.saved)
        assertEquals(1, fixture.source.calls)
    }

    @Test
    fun `keeps fresh projection without calling source`() {
        val accountId = UUID.fromString("019c1ab0-1200-7000-8000-000000000102")
        val cachedAppearance = appearance(
            accountId = accountId,
            revision = 4,
            updatedAt = NOW.minusSeconds(10),
        )
        val fixture = Fixture(profile(accountId, cachedAppearance))

        val result = fixture.service.execute(accountId)

        assertSame(cachedAppearance, result.appearance)
        assertEquals(0, fixture.source.calls)
        assertEquals(null, fixture.appearances.saved)
    }

    @Test
    fun `keeps stale projection when authoritative source is unavailable`() {
        val accountId = UUID.fromString("019c1ab0-1200-7000-8000-000000000103")
        val cachedAppearance = appearance(
            accountId = accountId,
            revision = 3,
            updatedAt = NOW.minusSeconds(90),
        )
        val fixture = Fixture(profile(accountId, cachedAppearance))
        fixture.source.result = null

        val result = fixture.service.execute(accountId)

        assertSame(cachedAppearance, result.appearance)
        assertEquals(1, fixture.source.calls)
        assertEquals(null, fixture.appearances.saved)
    }

    private class Fixture(initialProfile: PublicProfile) {
        val profiles = FakePublicProfileRepository(initialProfile)
        val appearances = FakeProfileAppearanceRepository()
        val source = FakeProfileAppearanceSource()

        val service =
            GetPublicProfileService(
                repository = profiles,
                appearanceRepository = appearances,
                appearanceSource = source,
                clock = Clock.fixed(NOW, ZoneOffset.UTC),
                appearanceReconciliationMaxAge = Duration.ofSeconds(30),
            )
    }

    private class FakeProfileAppearanceSource : ProfileAppearanceSource {
        var calls = 0
        var result: ProfileAppearance? = null

        override fun fetch(accountId: UUID): ProfileAppearance? {
            calls += 1

            return result
        }
    }

    private class FakeProfileAppearanceRepository : ProfileAppearanceRepository {
        var saved: ProfileAppearance? = null

        override fun save(appearance: ProfileAppearance): ProfileAppearance {
            saved = appearance

            return appearance
        }

        override fun findByAccountId(accountId: UUID): ProfileAppearance? = saved

        override fun findByAccountIdForUpdate(accountId: UUID): ProfileAppearance? = saved

        override fun findByAccountIds(accountIds: Collection<UUID>): Map<UUID, ProfileAppearance> =
            saved?.let { appearance -> mapOf(appearance.accountId to appearance) } ?: emptyMap()
    }

    private class FakePublicProfileRepository(
        private var profile: PublicProfile,
    ) : PublicProfileRepository {
        override fun ensureExists(
            accountId: UUID,
            cristalixUuid: UUID,
            cristalixNickname: String,
            createdAt: Instant,
        ) = error("Not used")

        override fun save(profile: PublicProfile): PublicProfile {
            this.profile = profile

            return profile
        }

        override fun findByAccountId(accountId: UUID): PublicProfile? =
            profile.takeIf { current -> current.accountId == accountId }

        override fun findByAccountIdForUpdate(accountId: UUID): PublicProfile? = findByAccountId(accountId)

        override fun search(query: SearchPublicProfilesQuery): PageResult<PublicProfile> = error("Not used")
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-08-26T12:30:00Z")

        fun profile(
            accountId: UUID,
            appearance: ProfileAppearance = ProfileAppearance.empty(accountId),
        ): PublicProfile =
            PublicProfile(
                accountId = accountId,
                cristalixUuid = UUID.fromString("0307d79b-e415-11e8-8374-1cb72caa35fd"),
                cristalixNickname = "knaliz",
                likes = 1,
                dislikes = 0,
                comments = 0,
                socialRevision = 1,
                createdAt = NOW.minusSeconds(600),
                updatedAt = NOW.minusSeconds(60),
                appearance = appearance,
            )

        fun appearance(
            accountId: UUID,
            revision: Long,
            updatedAt: Instant,
        ): ProfileAppearance =
            ProfileAppearance(
                accountId = accountId,
                revision = revision,
                nickStyle =
                    ProfileAppearance.NickStyle(
                        personalizationId = UUID.fromString("019c1ab0-1200-7000-8000-000000000199"),
                        colors =
                            listOf(
                                ProfileAppearance.GradientStop(
                                    color = "#CBFF2D",
                                    position = 0,
                                ),
                            ),
                        angleDegrees = 90,
                        glow = null,
                    ),
                profileTheme = null,
                avatarFrame = null,
                profileEffect = null,
                updatedAt = updatedAt,
            )
    }
}
