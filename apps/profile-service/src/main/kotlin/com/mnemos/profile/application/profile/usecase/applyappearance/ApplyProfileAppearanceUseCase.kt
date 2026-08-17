package com.mnemos.profile.application.profile.usecase.applyappearance

import com.mnemos.profile.domain.profile.model.ProfileAppearance

fun interface ApplyProfileAppearanceUseCase {
    fun execute(appearance: ProfileAppearance)
}
