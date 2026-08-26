package com.mnemos.profile.application.profile.usecase.projectplayer

import com.mnemos.profile.domain.profile.model.ProfilePlayerState

fun interface ProjectPlayerObservedUseCase {
    fun execute(state: ProfilePlayerState)
}
