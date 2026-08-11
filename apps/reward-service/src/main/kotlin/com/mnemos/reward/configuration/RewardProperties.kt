package com.mnemos.reward.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.reward")
data class RewardProperties(
    val daily: DailyRewardProperties = DailyRewardProperties(),
    val itemDrop: ItemDropRewardProperties = ItemDropRewardProperties(),
)
