package com.mnemos.reward.application.itemdrop.usecase

import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.reward.application.daily.port.out.AccountByNicknameProvider
import com.mnemos.reward.application.daily.port.out.RewardWallet
import com.mnemos.reward.configuration.RewardProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProcessItemDropRewardService(
    private val accounts: AccountByNicknameProvider,
    private val wallet: RewardWallet,
    private val properties: RewardProperties,
) {
    @Transactional
    fun process(command: ProcessItemDropRewardCommand) {
        val accountId = accounts.findAccountId(command.droppedFor) ?: return

        val amount =
            when (command.rarity) {
                ContractItemRarity.MYTHICAL -> properties.itemDrop.mythicalCoins
                ContractItemRarity.SECRET -> properties.itemDrop.secretCoins
            }

        wallet.creditItemDrop(accountId, amount, command.eventId)
    }
}
