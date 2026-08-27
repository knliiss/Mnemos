package com.mnemos.payment.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties("mnemos.payment.stars")
class StarPrimePurchaseProperties {
    var packages: String = "25:25,50:50,100:100,250:250"
    var invoiceLifetime: Duration = Duration.ofMinutes(15)
    var invoicePhotoUrl: String? = "https://app.knalis.rest/ui-illustrations/prime-cache.png"
}
