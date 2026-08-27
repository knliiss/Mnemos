package com.mnemos.playerdirectory.application.activity.usecase.observe

import com.mnemos.playerdirectory.application.activity.model.PlayerEventActivityType
import com.mnemos.playerdirectory.application.activity.port.out.PendingPlayerActivityRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameOwnershipRepository
import com.mnemos.playerdirectory.application.tracking.usecase.activity.RecordPlayerDropActivityCommand
import com.mnemos.playerdirectory.application.tracking.usecase.activity.RecordPlayerDropActivityUseCase
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class ObservePlayerEventActivityService(
    private val nicknameOwnershipRepository: PlayerNicknameOwnershipRepository,
    private val pendingPlayerActivityRepository: PendingPlayerActivityRepository,
    private val recordPlayerDropActivityUseCase: RecordPlayerDropActivityUseCase,
    private val clock: Clock,
) : ObservePlayerEventActivityUseCase {
    override fun execute(
        nickname: PlayerNickname,
        occurredAt: Instant,
        type: PlayerEventActivityType,
    ) {
        val ownership = nicknameOwnershipRepository.findByNickname(nickname)

        if (ownership != null) {
            if (type == PlayerEventActivityType.DROP) {
                recordPlayerDropActivityUseCase.execute(
                    RecordPlayerDropActivityCommand(
                        playerId = ownership.playerId,
                        occurredAt = occurredAt,
                    ),
                )
            }

            return
        }

        val recordedAt = Instant.now(clock)

        when (type) {
            PlayerEventActivityType.DROP ->
                pendingPlayerActivityRepository.enqueueDrop(
                    nickname = nickname,
                    occurredAt = occurredAt,
                    recordedAt = recordedAt,
                )

            PlayerEventActivityType.BOOSTER ->
                pendingPlayerActivityRepository.enqueueIdentityDemand(
                    nickname = nickname,
                    recordedAt = recordedAt,
                )
        }
    }
}
