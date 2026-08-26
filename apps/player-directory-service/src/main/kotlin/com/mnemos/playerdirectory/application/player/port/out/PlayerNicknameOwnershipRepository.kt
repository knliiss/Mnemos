package com.mnemos.playerdirectory.application.player.port.out

import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerNicknameOwnership

interface PlayerNicknameOwnershipRepository {
    fun save(ownership: PlayerNicknameOwnership): PlayerNicknameOwnership

    fun delete(ownership: PlayerNicknameOwnership)

    fun findByNickname(nickname: PlayerNickname): PlayerNicknameOwnership?

    fun findByNormalizedNicknamesForUpdate(normalizedNicknames: Set<String>): List<PlayerNicknameOwnership>
}
