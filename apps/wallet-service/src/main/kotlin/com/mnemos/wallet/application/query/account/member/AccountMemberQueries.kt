package com.mnemos.accountservice.application.query.account.member

import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.HexColor
import com.mnemos.accountservice.domain.model.account.role.RoleId
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
