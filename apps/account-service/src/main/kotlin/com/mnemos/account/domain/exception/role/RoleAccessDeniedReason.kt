package com.mnemos.account.domain.exception.role

enum class RoleAccessDeniedReason {
    ACTOR_HAS_NO_ROLE,
    SELF_ROLE_MANAGEMENT,
    TARGET_IS_OWNER,
    ROLE_NOT_LOWER,
    TARGET_MEMBER_NOT_LOWER,
    PROTECTED_HIERARCHY_CHANGED,
    OWN_ROLE_POSITION_CHANGED,
    SELF_KICK,
}
