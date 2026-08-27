package com.mnemos.playerdirectory.adapter.inbound.http.player

import com.mnemos.playerdirectory.adapter.inbound.http.player.PlayerApiMapper.toResponse
import com.mnemos.playerdirectory.application.player.usecase.resolve.ResolvePlayerByNicknameUseCase
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/players")
class PlayerController(
    private val resolvePlayerByNicknameUseCase: ResolvePlayerByNicknameUseCase,
) {
    @GetMapping("/resolve")
    fun resolve(
        @RequestParam
        @NotBlank
        @Size(max = 64)
        nickname: String,
    ): PlayerResponse =
        resolvePlayerByNicknameUseCase
            .execute(PlayerNickname.of(nickname))
            .toResponse()
}
