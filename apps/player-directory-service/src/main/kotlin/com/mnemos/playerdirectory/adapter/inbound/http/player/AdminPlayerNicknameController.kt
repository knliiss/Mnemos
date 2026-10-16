package com.mnemos.playerdirectory.adapter.inbound.http.player

import com.mnemos.playerdirectory.application.player.usecase.refresh.LinkedPlayerNicknameRefreshResult
import com.mnemos.playerdirectory.application.player.usecase.refresh.RefreshLinkedPlayerNicknameService
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/players")
class AdminPlayerNicknameController(
    private val refresh: RefreshLinkedPlayerNicknameService,
) {
    @PostMapping("/{playerId}/refresh-nickname")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun refreshNickname(
        @PathVariable playerId: UUID,
    ): LinkedPlayerNicknameRefreshResult = refresh.execute(CristalixPlayerId(playerId))
}
