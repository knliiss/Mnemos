package com.mnemos.playerdirectory.adapter.inbound.http.internal

import com.mnemos.contracts.playerdirectory.internal.v1.InternalPlayerResponse
import com.mnemos.contracts.playerdirectory.internal.v1.InternalResolvePlayerRequest
import com.mnemos.playerdirectory.adapter.inbound.http.internal.InternalPlayerApiMapper.toResponse
import com.mnemos.playerdirectory.application.player.usecase.resolve.ResolvePlayerByNicknameUseCase
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/players")
class InternalPlayerController(
    private val resolvePlayerByNicknameUseCase: ResolvePlayerByNicknameUseCase,
) {
    @PostMapping("/resolve")
    @PreAuthorize("authentication.name == 'account-service'")
    fun resolve(
        @Valid @RequestBody request: ResolvePlayerRequest,
    ): InternalPlayerResponse =
        resolvePlayerByNicknameUseCase
            .execute(PlayerNickname.of(request.nickname))
            .toResponse()

    data class ResolvePlayerRequest(
        @field:NotBlank
        @field:Size(max = 64)
        val nickname: String,
    ) {
        fun toContract(): InternalResolvePlayerRequest = InternalResolvePlayerRequest(nickname = nickname)
    }
}
