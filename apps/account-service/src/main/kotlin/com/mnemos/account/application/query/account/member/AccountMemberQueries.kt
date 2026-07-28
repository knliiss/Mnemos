package com.mnemos.account.application.query.account.member

import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.AccountRoleName
import com.mnemos.account.domain.model.account.role.HexColor
import com.mnemos.account.domain.model.account.role.RoleId
import java.time.Instant

data class AccountMemberListItem(
    val id: AccountMemberId,
    val name: String,
    val owner: Boolean,
    val highestRole: AccountMemberRoleSummary?,
    val joinedAt: Instant,
)

data class AccountMemberRoleSummary(
    val id: RoleId,
    val name: AccountRoleName,
    val color: HexColor,
)
