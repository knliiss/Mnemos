package com.mnemos.account.application.usecase.player

fun interface SyncPlayerNicknameUseCase {
    fun execute(command: SyncPlayerNicknameCommand)
}
