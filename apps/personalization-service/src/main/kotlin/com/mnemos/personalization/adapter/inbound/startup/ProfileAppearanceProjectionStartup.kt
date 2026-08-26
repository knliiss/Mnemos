package com.mnemos.personalization.adapter.inbound.startup

import com.mnemos.personalization.application.ownership.usecase.rebuild.RebuildProfileAppearanceService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ProfileAppearanceProjectionStartup(
    private val rebuildProfileAppearanceService: RebuildProfileAppearanceService,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        val rebuiltAccounts = rebuildProfileAppearanceService.executeSystem()

        logger.info(
            "Reconciled active profile appearance projections on startup: accounts={}",
            rebuiltAccounts,
        )
    }

    private companion object {
        val logger = LoggerFactory.getLogger(ProfileAppearanceProjectionStartup::class.java)
    }
}
