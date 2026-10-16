package com.mnemos.account.application.usecase.player

import com.mnemos.account.application.port.out.PlayerIdentityRevisionStore
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.repository.account.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SyncPlayerNicknameService(
    private val accountRepository: AccountRepository,
    private val revisionStore: PlayerIdentityRevisionStore,
) : SyncPlayerNicknameUseCase {
    @Transactional
    override fun execute(command: SyncPlayerNicknameCommand) {
        require(command.externalVersion >= 0) {
            "Player external version must not be negative"
        }

        val playerId = CristalixUuid(command.playerId)
        val nickname = CristalixNickname(command.nickname)
        val account = accountRepository.findByCristalixUuidForUpdate(playerId)
        val now = Instant.now()

        val advanced =
            revisionStore.advanceIfNewer(
                playerId = playerId,
                nickname = nickname,
                externalVersion = command.externalVersion,
                observedAt = command.observedAt,
                recordedAt = now,
            )

        if (account == null) {
            return
        }
        if (!advanced && !revisionStore.matchesCurrent(playerId, nickname, command.externalVersion)) {
            return
        }

        val changed =
            account.updateCristalixNickname(
                newCristalixNickname = nickname,
                now = now,
            )

        if (changed) {
            accountRepository.update(account)
        }
    }
}
