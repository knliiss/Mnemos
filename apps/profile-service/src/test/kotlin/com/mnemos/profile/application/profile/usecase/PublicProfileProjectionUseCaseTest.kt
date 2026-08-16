package com.mnemos.profile.application.profile.usecase

import com.mnemos.profile.application.profile.port.out.PendingSocialSummaryRepository
import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.application.profile.support.PendingSocialSummaryCoordinator
import com.mnemos.profile.application.profile.usecase.applysocialsummary.ApplyAccountSocialSummaryService
import com.mnemos.profile.application.profile.usecase.projectaccount.ProjectAccountCreatedService
import com.mnemos.profile.application.profile.usecase.reconcile.ReconcilePendingSocialSummariesService
import com.mnemos.profile.application.profile.usecase.sync.SyncPublicProfileService
import com.mnemos.profile.domain.profile.model.PendingSocialSummary
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

class PublicProfileProjectionUseCaseTest {
    @Test
    fun `applies pending social summary when account projection arrives`() {
        val fixture = Fixture()
        val accountId = UUID.fromString("019c1ab0-1200-7000-8000-000000000001")
        val cristalixUuid = UUID.fromString("019c1ab0-1200-7000-8000-000000000002")
        val accountCreatedAt = Instant.parse("2026-08-16T00:00:00Z")
        val socialOccurredAt = Instant.parse("2026-08-16T00:05:00Z")

        fixture.applySocialSummary.execute(
            accountId = accountId,
            likes = 7,
            dislikes = 2,
            comments = 4,
            revision = 3,
            occurredAt = socialOccurredAt,
        )

        assertNull(fixture.profiles.findByAccountId(accountId))
        assertEquals(3, fixture.pending.findByAccountId(accountId)?.revision)

        fixture.projectAccount.execute(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = "Knalis",
            occurredAt = accountCreatedAt,
        )

        val profile = requireNotNull(fixture.profiles.findByAccountId(accountId))

        assertEquals(7, profile.likes)
        assertEquals(2, profile.dislikes)
        assertEquals(4, profile.comments)
        assertEquals(3, profile.socialRevision)
        assertEquals(socialOccurredAt, profile.updatedAt)
        assertNull(fixture.pending.findByAccountId(accountId))
    }

    @Test
    fun `keeps newest social revision while account projection is missing`() {
        val fixture = Fixture()
        val accountId = UUID.fromString("019c1ab0-1200-7000-8000-000000000003")

        fixture.applySocialSummary.execute(
            accountId = accountId,
            likes = 10,
            dislikes = 1,
            comments = 8,
            revision = 5,
            occurredAt = Instant.parse("2026-08-16T00:10:00Z"),
        )
        fixture.applySocialSummary.execute(
            accountId = accountId,
            likes = 1,
            dislikes = 0,
            comments = 1,
            revision = 2,
            occurredAt = Instant.parse("2026-08-16T00:06:00Z"),
        )

        val stored = requireNotNull(fixture.pending.findByAccountId(accountId))

        assertEquals(5, stored.revision)
        assertEquals(10, stored.likes)
        assertEquals(8, stored.comments)
    }

    @Test
    fun `ignores stale social summary after profile exists`() {
        val fixture = Fixture()
        val accountId = UUID.fromString("019c1ab0-1200-7000-8000-000000000004")

        fixture.projectAccount.execute(
            accountId = accountId,
            cristalixUuid = UUID.fromString("019c1ab0-1200-7000-8000-000000000005"),
            cristalixNickname = "Knalis",
            occurredAt = Instant.parse("2026-08-16T00:00:00Z"),
        )
        fixture.applySocialSummary.execute(
            accountId = accountId,
            likes = 6,
            dislikes = 2,
            comments = 3,
            revision = 4,
            occurredAt = Instant.parse("2026-08-16T00:04:00Z"),
        )
        fixture.applySocialSummary.execute(
            accountId = accountId,
            likes = 1,
            dislikes = 1,
            comments = 1,
            revision = 2,
            occurredAt = Instant.parse("2026-08-16T00:02:00Z"),
        )

        val profile = requireNotNull(fixture.profiles.findByAccountId(accountId))

        assertEquals(6, profile.likes)
        assertEquals(2, profile.dislikes)
        assertEquals(3, profile.comments)
        assertEquals(4, profile.socialRevision)
    }

    @Test
    fun `reconciliation applies pending summary when matching profile already exists`() {
        val fixture = Fixture()
        val accountId = UUID.fromString("019c1ab0-1200-7000-8000-000000000006")

        fixture.profiles.save(
            PublicProfile(
                accountId = accountId,
                cristalixUuid = UUID.fromString("019c1ab0-1200-7000-8000-000000000007"),
                cristalixNickname = "Knalis",
                likes = 0,
                dislikes = 0,
                comments = 0,
                socialRevision = 0,
                createdAt = Instant.parse("2026-08-16T00:00:00Z"),
                updatedAt = Instant.parse("2026-08-16T00:00:00Z"),
            ),
        )
        fixture.pending.save(
            PendingSocialSummary(
                accountId = accountId,
                likes = 2,
                dislikes = 3,
                comments = 5,
                revision = 6,
                occurredAt = Instant.parse("2026-08-16T00:06:00Z"),
            ),
        )

        fixture.reconcile.execute(limit = 100)

        val profile = requireNotNull(fixture.profiles.findByAccountId(accountId))

        assertEquals(2, profile.likes)
        assertEquals(3, profile.dislikes)
        assertEquals(5, profile.comments)
        assertEquals(6, profile.socialRevision)
        assertNull(fixture.pending.findByAccountId(accountId))
    }

    @Test
    fun `sync refreshes account identity without overwriting social counters`() {
        val fixture = Fixture()
        val accountId = UUID.fromString("019c1ab0-1200-7000-8000-000000000008")
        val createdAt = Instant.parse("2026-08-15T20:00:00Z")

        fixture.profiles.save(
            PublicProfile(
                accountId = accountId,
                cristalixUuid = UUID.fromString("019c1ab0-1200-7000-8000-000000000009"),
                cristalixNickname = "OldNickname",
                likes = 9,
                dislikes = 1,
                comments = 4,
                socialRevision = 8,
                createdAt = Instant.parse("2026-08-15T20:00:01Z"),
                updatedAt = Instant.parse("2026-08-16T00:00:00Z"),
            ),
        )

        fixture.sync.execute(
            accountId = accountId,
            cristalixUuid = UUID.fromString("019c1ab0-1200-7000-8000-000000000010"),
            cristalixNickname = "CurrentNickname",
            createdAt = createdAt,
        )

        val profile = requireNotNull(fixture.profiles.findByAccountId(accountId))

        assertEquals("CurrentNickname", profile.cristalixNickname)
        assertEquals(createdAt, profile.createdAt)
        assertEquals(9, profile.likes)
        assertEquals(1, profile.dislikes)
        assertEquals(4, profile.comments)
        assertEquals(8, profile.socialRevision)
    }

    private class Fixture {
        val profiles = FakePublicProfileRepository()
        val pending = FakePendingSocialSummaryRepository(profiles)
        private val coordinator = PendingSocialSummaryCoordinator(profiles, pending)
        val projectAccount = ProjectAccountCreatedService(profiles, coordinator)
        val applySocialSummary = ApplyAccountSocialSummaryService(profiles, coordinator)
        val reconcile = ReconcilePendingSocialSummariesService(pending, coordinator)
        val sync = SyncPublicProfileService(profiles, coordinator)
    }

    private class FakePublicProfileRepository : PublicProfileRepository {
        private val profiles = mutableMapOf<UUID, PublicProfile>()

        override fun ensureExists(
            accountId: UUID,
            cristalixUuid: UUID,
            cristalixNickname: String,
            createdAt: Instant,
        ) {
            profiles.putIfAbsent(
                accountId,
                PublicProfile(
                    accountId = accountId,
                    cristalixUuid = cristalixUuid,
                    cristalixNickname = cristalixNickname,
                    likes = 0,
                    dislikes = 0,
                    comments = 0,
                    socialRevision = 0,
                    createdAt = createdAt,
                    updatedAt = createdAt,
                ),
            )
        }

        override fun save(profile: PublicProfile): PublicProfile {
            profiles[profile.accountId] = profile
            return profile
        }

        override fun findByAccountId(accountId: UUID): PublicProfile? = profiles[accountId]

        override fun findByAccountIdForUpdate(accountId: UUID): PublicProfile? = profiles[accountId]

        override fun search(
            query: String,
            pageable: Pageable,
        ): Page<PublicProfile> = Page.empty(pageable)
    }

    private class FakePendingSocialSummaryRepository(
        private val profiles: PublicProfileRepository,
    ) : PendingSocialSummaryRepository {
        private val summaries = mutableMapOf<UUID, PendingSocialSummary>()

        override fun save(summary: PendingSocialSummary): PendingSocialSummary {
            summaries[summary.accountId] = summary
            return summary
        }

        override fun findByAccountId(accountId: UUID): PendingSocialSummary? = summaries[accountId]

        override fun findReady(limit: Int): List<PendingSocialSummary> =
            summaries.values
                .filter { summary -> profiles.findByAccountId(summary.accountId) != null }
                .sortedBy(PendingSocialSummary::occurredAt)
                .take(limit)

        override fun deleteIfRevision(
            accountId: UUID,
            revision: Long,
        ) {
            val current = summaries[accountId]

            if (current?.revision == revision) {
                summaries.remove(accountId)
            }
        }
    }
}
