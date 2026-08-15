package com.mnemos.personalization.application.pricing

import com.mnemos.personalization.application.pricing.model.PersonalizationPriceRange
import com.mnemos.personalization.application.pricing.model.PriceRange
import com.mnemos.personalization.configuration.pricing.PersonalizationPricingProperties
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.springframework.stereotype.Component

@Component
class PersonalizationPricePolicy(
    private val properties: PersonalizationPricingProperties,
) {
    fun requireAllowed(
        type: PersonalizationType,
        origin: PersonalizationOrigin,
        price: PersonalizationPrice,
    ) {
        val range = rangeFor(type, origin, price.currency)

        require(range.contains(price.amount)) {
            "Price ${price.amount} ${price.currency} is outside allowed range ${range.min}..${range.max}"
        }
    }

    fun customNotificationPackPurchasePrice(): PersonalizationPrice =
        PersonalizationPrice(
            currency = Currency.PRIME,
            amount = properties.customNotificationPackPurchasePrime,
        )

    fun marketplaceRanges(): List<PersonalizationPriceRange> =
        buildList {
            addRange(PersonalizationType.NOTIFICATION_PACK, PersonalizationOrigin.CREATOR, Currency.PRIME)
            addRange(PersonalizationType.NOTIFICATION_PACK, PersonalizationOrigin.CUSTOM, Currency.PRIME)

            listOf(
                PersonalizationType.NICK_STYLE,
                PersonalizationType.PROFILE_THEME,
                PersonalizationType.AVATAR_FRAME,
                PersonalizationType.PROFILE_EFFECT,
            ).forEach { type ->
                addRange(type, PersonalizationOrigin.CREATOR, Currency.COIN)
                addRange(type, PersonalizationOrigin.CREATOR, Currency.PRIME)
            }
        }

    fun rangeFor(
        type: PersonalizationType,
        origin: PersonalizationOrigin,
        currency: Currency,
    ): PriceRange {
        val range =
            when (type) {
                PersonalizationType.NOTIFICATION_PACK ->
                    when {
                        currency != Currency.PRIME -> null
                        origin == PersonalizationOrigin.CUSTOM -> properties.customNotificationPackPrime
                        else -> properties.notificationPackPrime
                    }

                PersonalizationType.NICK_STYLE ->
                    when (currency) {
                        Currency.COIN -> properties.nickStyleCoin
                        Currency.PRIME -> properties.nickStylePrime
                    }

                PersonalizationType.PROFILE_THEME ->
                    when (currency) {
                        Currency.COIN -> properties.profileThemeCoin
                        Currency.PRIME -> properties.profileThemePrime
                    }

                PersonalizationType.AVATAR_FRAME ->
                    when (currency) {
                        Currency.COIN -> properties.avatarFrameCoin
                        Currency.PRIME -> properties.avatarFramePrime
                    }

                PersonalizationType.PROFILE_EFFECT ->
                    when (currency) {
                        Currency.COIN -> properties.profileEffectCoin
                        Currency.PRIME -> properties.profileEffectPrime
                    }
            }

        requireNotNull(range) { "Currency $currency is not supported for personalization type $type" }

        return PriceRange(
            min = range.min,
            max = range.max,
        )
    }

    private fun MutableList<PersonalizationPriceRange>.addRange(
        type: PersonalizationType,
        origin: PersonalizationOrigin,
        currency: Currency,
    ) {
        add(
            PersonalizationPriceRange(
                type = type,
                origin = origin,
                currency = currency,
                range = rangeFor(type, origin, currency),
            ),
        )
    }
}
