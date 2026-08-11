package com.mnemos.notification.application.dungeon.matcher

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.dungeon.repository.DungeonSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DungeonSubscriptionMatcher(
    val repository: DungeonSubscriptionRepository,
) {
    @Transactional(readOnly = true)
    fun match(dungeonType: DomainDungeonType): Set<UserId> = repository.findMatchingUserIds(dungeonType)
}
