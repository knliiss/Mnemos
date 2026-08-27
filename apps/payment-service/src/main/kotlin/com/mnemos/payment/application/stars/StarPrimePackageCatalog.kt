package com.mnemos.payment.application.stars

import com.mnemos.payment.application.stars.model.StarPrimePackage
import com.mnemos.payment.configuration.StarPrimePurchaseProperties
import org.springframework.stereotype.Component

@Component
class StarPrimePackageCatalog(
    private val properties: StarPrimePurchaseProperties,
) {
    fun all(): List<StarPrimePackage> {
        val packages =
            properties.packages
                .split(',')
                .map(String::trim)
                .filter(String::isNotEmpty)
                .map(::parsePackage)

        require(packages.isNotEmpty()) {
            "mnemos.payment.stars.packages must contain at least one package"
        }
        require(packages.map(StarPrimePackage::id).distinct().size == packages.size) {
            "mnemos.payment.stars.packages contains duplicate packages"
        }

        return packages
    }

    fun requireById(packageId: String): StarPrimePackage =
        all().firstOrNull { it.id == packageId }
            ?: throw IllegalArgumentException("Unknown PRIME package: $packageId")

    private fun parsePackage(value: String): StarPrimePackage {
        val parts = value.split(':', limit = 2)

        require(parts.size == 2) {
            "Invalid PRIME package '$value': expected PRIME:STARS"
        }

        val primeAmount = parts[0].toLongOrNull()
        val starsAmount = parts[1].toIntOrNull()

        require(primeAmount != null && primeAmount > 0) {
            "Invalid PRIME amount in package '$value'"
        }
        require(starsAmount != null && starsAmount > 0) {
            "Invalid Stars amount in package '$value'"
        }

        return StarPrimePackage(
            id = "prime-$primeAmount-stars-$starsAmount",
            primeAmount = primeAmount,
            starsAmount = starsAmount,
        )
    }
}
