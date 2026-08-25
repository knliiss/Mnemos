package com.mnemos.wallet.adapter.out.persistence.leaderboard

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.adapter.out.persistence.balance.BalanceJpaRepository
import com.mnemos.wallet.application.leaderboard.coin.CoinLeaderboardCandidate
import com.mnemos.wallet.application.leaderboard.coin.CoinLeaderboardReader
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class CoinLeaderboardJpaAdapter(
    private val repository: BalanceJpaRepository,
) : CoinLeaderboardReader {
    override fun read(limit: Int): List<CoinLeaderboardCandidate> =
        repository
            .findLeaderboard(
                ownerType = WalletOwnerType.ACCOUNT,
                currency = Currency.COIN,
                pageable = PageRequest.of(0, limit),
            ).map { balance ->
                CoinLeaderboardCandidate(
                    accountId = AccountId(balance.id.ownerId),
                    amount = balance.amount,
                )
            }
}
