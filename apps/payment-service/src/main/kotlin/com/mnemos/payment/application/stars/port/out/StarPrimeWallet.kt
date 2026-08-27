package com.mnemos.payment.application.stars.port.out

import com.mnemos.payment.domain.stars.model.StarPrimePurchase

fun interface StarPrimeWallet {
    fun requestCredit(purchase: StarPrimePurchase)
}
