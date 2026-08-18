package com.mnemos.personalization.adapter.inbound.http.admin

import com.mnemos.personalization.application.ownership.usecase.rebuild.RebuildProfileAppearanceService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class RebuildProfileAppearanceResponse(
    val rebuiltAccounts: Int,
)

@RestController
@RequestMapping("/api/v1/personalizations/admin/profile-appearances")
class ProfileAppearanceAdminController(
    private val rebuildProfileAppearanceService: RebuildProfileAppearanceService,
) {
    @PostMapping("/rebuild")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun rebuild(): RebuildProfileAppearanceResponse =
        RebuildProfileAppearanceResponse(
            rebuiltAccounts = rebuildProfileAppearanceService.execute(),
        )
}
