package com.mnemos.playerdirectory.application.player.exception

class PlayerDirectoryUpstreamUnavailableException(
    message: String = "Cristalix player API is temporarily unavailable",
    cause: Throwable? = null,
) : RuntimeException(message, cause)
