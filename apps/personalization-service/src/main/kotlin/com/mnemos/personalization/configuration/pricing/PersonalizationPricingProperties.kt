package com.mnemos.personalization.configuration.pricing

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mnemos.personalization.pricing")
data class PersonalizationPricingProperties(
    val notificationPackPrime: Range = Range(10, 500),
    val customNotificationPackPrime: Range = Range(30, 1_000),
    val customNotificationPackPurchasePrime: Long = 50,
    val nickStyleCoin: Range = Range(500, 50_000),
    val nickStylePrime: Range = Range(5, 500),
    val profileThemeCoin: Range = Range(1_000, 100_000),
    val profileThemePrime: Range = Range(10, 1_000),
    val avatarFrameCoin: Range = Range(750, 75_000),
    val avatarFramePrime: Range = Range(8, 750),
    val profileEffectCoin: Range = Range(1_500, 150_000),
    val profileEffectPrime: Range = Range(15, 1_500),
) {
    init {
        require(customNotificationPackPrime.min > notificationPackPrime.min) {
            "Custom notification pack minimum price must be higher than regular notification pack minimum price"
        }
        require(customNotificationPackPurchasePrime in customNotificationPackPrime.min..customNotificationPackPrime.max) {
            "Private custom notification pack purchase price must fit the custom notification pack price range"
        }
    }

    data class Range(
        val min: Long,
        val max: Long,
    ) {
        init {
            require(min > 0) { "Personalization minimum price must be positive" }
            require(max >= min) { "Personalization maximum price must be greater than or equal to minimum price" }
        }
    }
}
