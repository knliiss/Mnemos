package com.mnemos.moderation.application.wallet.usecase.query

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.moderation.application.security.model.ModeratorActor
import com.mnemos.moderation.application.security.port.out.CurrentModeratorProvider
import com.mnemos.moderation.application.wallet.model.WalletBalance
import com.mnemos.moderation.application.wallet.port.out.ModerationWalletBalanceReader
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.access.AccessDeniedException
import java.util.UUID

class GetWalletBalanceServiceTest {
    private val actorUserId = UserId(UUID.fromString("0198a000-0000-7000-8000-000000000001"))
    private val targetId = UUID.fromString("0198a000-0000-7000-8000-000000000002")

    @Test
    fun `admin can read account coin balance`() {
        val currentModeratorProvider = mock<CurrentModeratorProvider>()
        val balanceReader = mock<ModerationWalletBalanceReader>()
        val service =
            GetWalletBalanceService(
                currentModeratorProvider = currentModeratorProvider,
                balanceReader = balanceReader,
            )
        val expected =
            WalletBalance(
                ownerType = WalletOwnerType.ACCOUNT,
                ownerId = targetId,
                currency = Currency.COIN,
                amount = 1250,
            )

        whenever(currentModeratorProvider.current()).thenReturn(
            ModeratorActor(
                userId = actorUserId,
                role = UserRole.ADMIN,
            ),
        )
        whenever(balanceReader.getBalance(WalletOwnerType.ACCOUNT, targetId)).thenReturn(expected)

        val result = service.execute(WalletOwnerType.ACCOUNT, targetId)

        assertEquals(expected, result)
    }

    @Test
    fun `admin cannot read user prime balance`() {
        val currentModeratorProvider = mock<CurrentModeratorProvider>()
        val balanceReader = mock<ModerationWalletBalanceReader>()
        val service =
            GetWalletBalanceService(
                currentModeratorProvider = currentModeratorProvider,
                balanceReader = balanceReader,
            )

        whenever(currentModeratorProvider.current()).thenReturn(
            ModeratorActor(
                userId = actorUserId,
                role = UserRole.ADMIN,
            ),
        )

        assertThrows(AccessDeniedException::class.java) {
            service.execute(WalletOwnerType.USER, targetId)
        }
        verifyNoInteractions(balanceReader)
    }

    @Test
    fun `super admin can read user prime balance`() {
        val currentModeratorProvider = mock<CurrentModeratorProvider>()
        val balanceReader = mock<ModerationWalletBalanceReader>()
        val service =
            GetWalletBalanceService(
                currentModeratorProvider = currentModeratorProvider,
                balanceReader = balanceReader,
            )
        val expected =
            WalletBalance(
                ownerType = WalletOwnerType.USER,
                ownerId = targetId,
                currency = Currency.PRIME,
                amount = 100,
            )

        whenever(currentModeratorProvider.current()).thenReturn(
            ModeratorActor(
                userId = actorUserId,
                role = UserRole.SUPER_ADMIN,
            ),
        )
        whenever(balanceReader.getBalance(WalletOwnerType.USER, targetId)).thenReturn(expected)

        val result = service.execute(WalletOwnerType.USER, targetId)

        assertEquals(expected, result)
    }
}
