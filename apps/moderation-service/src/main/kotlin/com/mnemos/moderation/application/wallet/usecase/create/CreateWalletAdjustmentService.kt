package com.mnemos.moderation.application.wallet.usecase.create

import com.mnemos.common.core.model.user.UserRole
import com.mnemos.moderation.application.security.port.out.CurrentModeratorProvider
import com.mnemos.moderation.application.wallet.exception.WalletAdjustmentIdempotencyConflictException
import com.mnemos.moderation.application.wallet.port.out.ModerationWallet
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation
import com.mnemos.moderation.domain.wallet.repository.WalletAdjustmentOperationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CreateWalletAdjustmentService(
    private val currentModeratorProvider: CurrentModeratorProvider,
    private val repository: WalletAdjustmentOperationRepository,
    private val wallet: ModerationWallet,
    private val clock: Clock,
) {
    @Transactional
    fun execute(command: CreateWalletAdjustmentCommand): WalletAdjustmentOperation {
        val actor = currentModeratorProvider.current()
        val replay = command.idempotencyKey?.let(repository::findByIdempotencyKey)

        if (replay != null) {
            if (
                replay.actorUserId != actor.userId ||
                replay.targetType != command.targetType ||
                replay.targetId != command.targetId ||
                replay.currency != command.currency ||
                replay.type != command.type ||
                replay.amount != command.amount ||
                replay.reason != command.reason.trim()
            ) {
                throw WalletAdjustmentIdempotencyConflictException()
            }

            return replay
        }

        requirePermission(
            role = actor.role,
            currency = command.currency,
        )

        val operation =
            WalletAdjustmentOperation.create(
                targetType = command.targetType,
                targetId = command.targetId,
                actorUserId = actor.userId,
                currency = command.currency,
                type = command.type,
                amount = command.amount,
                reason = command.reason,
                now = clock.instant(),
                idempotencyKey = command.idempotencyKey,
            )
        val saved = repository.save(operation)

        wallet.requestAdjustment(saved)

        return saved
    }

    private fun requirePermission(
        role: UserRole,
        currency: Currency,
    ) {
        val allowed =
            when (currency) {
                Currency.COIN -> role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN
                Currency.PRIME -> role == UserRole.SUPER_ADMIN
            }

        if (!allowed) {
            throw AccessDeniedException("Role $role cannot adjust $currency")
        }
    }
}
