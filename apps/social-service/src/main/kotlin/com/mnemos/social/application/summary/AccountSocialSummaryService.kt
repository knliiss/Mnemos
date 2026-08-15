package com.mnemos.social.application.summary

import com.mnemos.social.application.summary.port.out.AccountSocialSummaryEventPublisher
import com.mnemos.social.application.summary.port.out.AccountSocialSummaryRepository
import com.mnemos.social.domain.reaction.model.AccountReactionType
import com.mnemos.social.domain.summary.model.AccountSocialSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AccountSocialSummaryService(
    private val repository: AccountSocialSummaryRepository,
    private val publisher: AccountSocialSummaryEventPublisher,
) {
    @Transactional
    fun lockAccount(
        accountId: UUID,
        now: Instant,
    ) {
        repository.ensureExists(accountId, now)
        repository.findForUpdate(accountId)
    }

    @Transactional
    fun commentAdded(
        accountId: UUID,
        actorUserId: UUID,
        now: Instant,
    ) {
        mutate(accountId, actorUserId, now) { summary ->
            summary.comments += 1
        }
    }

    @Transactional
    fun commentRemoved(
        accountId: UUID,
        actorUserId: UUID,
        now: Instant,
    ) {
        mutate(accountId, actorUserId, now) { summary ->
            summary.comments = (summary.comments - 1).coerceAtLeast(0)
        }
    }

    @Transactional
    fun reactionChanged(
        accountId: UUID,
        actorUserId: UUID,
        previous: AccountReactionType?,
        current: AccountReactionType,
        now: Instant,
    ) {
        if (previous == current) {
            return
        }

        mutate(accountId, actorUserId, now) { summary ->
            previous?.let { reaction -> summary.decrement(reaction) }
            summary.increment(current)
        }
    }

    @Transactional
    fun reactionRemoved(
        accountId: UUID,
        actorUserId: UUID,
        previous: AccountReactionType,
        now: Instant,
    ) {
        mutate(accountId, actorUserId, now) { summary ->
            summary.decrement(previous)
        }
    }

    private fun mutate(
        accountId: UUID,
        actorUserId: UUID,
        now: Instant,
        mutation: (AccountSocialSummary) -> Unit,
    ) {
        repository.ensureExists(accountId, now)
        val summary = repository.findForUpdate(accountId)

        mutation(summary)
        summary.revision += 1
        summary.updatedAt = now

        val saved = repository.save(summary)
        publisher.publish(saved, actorUserId)
    }

    private fun AccountSocialSummary.increment(type: AccountReactionType) {
        when (type) {
            AccountReactionType.LIKE -> likes += 1
            AccountReactionType.DISLIKE -> dislikes += 1
        }
    }

    private fun AccountSocialSummary.decrement(type: AccountReactionType) {
        when (type) {
            AccountReactionType.LIKE -> likes = (likes - 1).coerceAtLeast(0)
            AccountReactionType.DISLIKE -> dislikes = (dislikes - 1).coerceAtLeast(0)
        }
    }
}
