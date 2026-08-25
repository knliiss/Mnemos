package com.mnemos.personalization.adapter.inbound.startup

import com.mnemos.personalization.application.personalization.builtin.SynchronizeBuiltinPersonalizationCatalogService
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class BuiltinPersonalizationCatalogStartup(
    private val synchronizeBuiltinPersonalizationCatalogService: SynchronizeBuiltinPersonalizationCatalogService,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        synchronizeBuiltinPersonalizationCatalogService.execute()
    }
}
