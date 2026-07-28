package com.mnemos.account.support

import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.application.context.account.ActiveAccountContext
import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.policy.role.RoleAccessGuard
import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.model.account.invite.AccountInvite
import com.mnemos.account.domain.model.account.invite.InviteCode
import com.mnemos.account.domain.model.account.invite.InviteId
import com.mnemos.account.domain.model.account.role.AccountRoleName
import com.mnemos.account.domain.model.account.role.HexColor
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.model.user.User
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.common.core.model.user.UserId
import com.mnemos.common.core.model.user.Username
import com.mnemos.security.CurrentMnemosUser
import com.mnemos.security.MnemosSecurityProperties
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

val TEST_TIME: Instant = Instant.parse("2026-07-27T10:00:00Z")

fun user(
    id: UserId = UserId(UUID.randomUUID()),
    keycloakId: KeycloakId = KeycloakId(UUID.randomUUID()),
    telegramId: TelegramId = TelegramId(100_000L + kotlin.random.Random.nextLong(1, 1_000_000)),
    username: Username = Username("user_${UUID.randomUUID().toString().take(8)}"),
    localizationLanguage: LocalizationLanguage = LocalizationLanguage.EN,
    activeAccountId: AccountId? = null,
    createdAt: Instant = TEST_TIME,
    updatedAt: Instant = TEST_TIME,
): User =
    User.restore(
        id = id,
        telegramId = telegramId,
        username = username,
        keycloakId = keycloakId,
        localizationLanguage = localizationLanguage,
        createdAt = createdAt,
        updatedAt = updatedAt,
        activeAccountId = activeAccountId,
    )

fun account(
    id: AccountId = AccountId(UUID.randomUUID()),
    ownerMemberId: AccountMemberId = AccountMemberId(UUID.randomUUID()),
    cristalixUuid: CristalixUuid = CristalixUuid(UUID.randomUUID()),
    cristalixNickname: CristalixNickname = CristalixNickname("Player_${UUID.randomUUID().toString().take(6)}"),
    status: AccountStatus = AccountStatus.ACTIVE,
    createdAt: Instant = TEST_TIME,
    updatedAt: Instant = TEST_TIME,
    deletionRequestedAt: Instant? = null,
    deletedAt: Instant? = null,
    maxMembers: Int = Account.DEFAULT_MAX_MEMBERS,
    baseRoleId: RoleId? = null,
): Account =
    Account.restore(
        id = id,
        ownerMemberId = ownerMemberId,
        cristalixUuid = cristalixUuid,
        cristalixNickname = cristalixNickname,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletionRequestedAt = deletionRequestedAt,
        deletedAt = deletedAt,
        maxMembers = maxMembers,
        baseRoleId = baseRoleId,
    )

fun member(
    id: AccountMemberId = AccountMemberId(UUID.randomUUID()),
    accountId: AccountId = AccountId(UUID.randomUUID()),
    userId: UserId = UserId(UUID.randomUUID()),
    joinedAt: Instant = TEST_TIME,
): AccountMember =
    AccountMember.restore(
        id = id,
        accountId = accountId,
        userId = userId,
        joinedAt = joinedAt,
    )

fun role(
    id: RoleId = RoleId(UUID.randomUUID()),
    accountId: AccountId = AccountId(UUID.randomUUID()),
    name: String = "Role_${UUID.randomUUID().toString().take(6)}",
    color: String = "#AABBCC",
    rank: Long = Role.RANK_STEP,
    permissions: Set<RolePermission> = emptySet(),
    createdAt: Instant = TEST_TIME,
    updatedAt: Instant = TEST_TIME,
): Role =
    Role.restore(
        id = id,
        accountId = accountId,
        roleColor = HexColor(color),
        roleName = AccountRoleName(name),
        hierarchyRank = rank,
        permissions = permissions,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun invite(
    id: InviteId = InviteId(UUID.randomUUID()),
    accountId: AccountId = AccountId(UUID.randomUUID()),
    code: InviteCode = InviteCode.of("ABCD-EFGH-JKLM-NPQR"),
    createdByUserId: UserId = UserId(UUID.randomUUID()),
    createdAt: Instant = TEST_TIME,
    expiresAt: Instant? = TEST_TIME.plus(1, ChronoUnit.DAYS),
    consumedAt: Instant? = null,
    revokedAt: Instant? = null,
): AccountInvite =
    AccountInvite.restore(
        id = id,
        accountId = accountId,
        code = code,
        createdByUserId = createdByUserId,
        createdAt = createdAt,
        expiresAt = expiresAt,
        consumedAt = consumedAt,
        revokedAt = revokedAt,
    )

data class ActiveContextFixture(
    val user: User,
    val account: Account,
    val member: AccountMember,
    val context: ActiveAccountContext,
)

fun activeContext(owner: Boolean = false): ActiveContextFixture {
    val accountId = AccountId(UUID.randomUUID())
    val user = user(activeAccountId = accountId)
    val actorMember = member(accountId = accountId, userId = user.id)
    val ownerMemberId = if (owner) actorMember.id else AccountMemberId(UUID.randomUUID())
    val account = account(id = accountId, ownerMemberId = ownerMemberId)
    return ActiveContextFixture(user, account, actorMember, ActiveAccountContext(user, account, actorMember))
}

fun authenticate(user: User) {
    val now = Instant.now()
    val jwt =
        Jwt
            .withTokenValue("test-token")
            .header("alg", "none")
            .subject(user.keycloakId.value.toString())
            .claim("preferred_username", user.username.value)
            .claim("telegram_id", user.telegramId.value)
            .issuedAt(now.minusSeconds(10))
            .expiresAt(now.plusSeconds(3_600))
            .build()
    SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt, listOf(SimpleGrantedAuthority("ROLE_USER")))
}

fun clearAuthentication() {
    SecurityContextHolder.clearContext()
}

fun currentUserProvider(userRepository: InMemoryUserRepository): CurrentUserProvider {
    val currentMnemosUser = CurrentMnemosUser(MnemosSecurityProperties())
    return CurrentUserProvider(currentMnemosUser, userRepository)
}

fun activeAccountContextProvider(
    userRepository: InMemoryUserRepository,
    accountRepository: InMemoryAccountRepository,
    memberRepository: InMemoryAccountMemberRepository,
): ActiveAccountContextProvider =
    ActiveAccountContextProvider(
        currentUserProvider(userRepository),
        accountRepository,
        memberRepository,
    )

fun roleAccessGuard(
    roleRepository: InMemoryRoleRepository,
    memberRepository: InMemoryAccountMemberRepository,
): RoleAccessGuard = RoleAccessGuard(roleRepository, memberRepository)
