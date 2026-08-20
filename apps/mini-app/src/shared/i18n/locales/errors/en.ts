export const errorCodesEn = {
  AUTHENTICATION_REQUIRED: {
    title: "Sign in again",
    message: "Your session has ended.",
  },
  INVALID_AUTHENTICATION_PRINCIPAL: {
    title: "Could not verify the user",
    message: "Refresh your authorization.",
  },
  ACCESS_DENIED: {
    title: "Action unavailable",
    message: "You do not have permission for this action.",
  },
  ACCOUNT_PERMISSION_DENIED: {
    title: "Insufficient permissions",
    message: "Your role does not allow this action.",
  },
  ROLE_ACCESS_DENIED: {
    title: "Insufficient permissions",
    message: "Your role is lower in the hierarchy.",
  },
  ACCOUNT_OWNER_REQUIRED: {
    title: "Owner required",
    message: "Only the account owner can perform this action.",
  },
  ACCOUNT_MEMBER_KICK_FORBIDDEN: {
    title: "Member cannot be removed",
    message: "Check the roles and your permissions.",
  },
  PERSONALIZATION_ACCESS_DENIED: {
    title: "Access denied",
    message: "This work cannot be changed.",
  },
  SANCTION_TARGET_ACCESS_DENIED: {
    title: "Sanction unavailable",
    message: "The user has a higher access level than you.",
  },
  ACTIVE_ACCOUNT_NOT_SET: {
    title: "Choose an account",
    message: "This action requires an active account.",
  },
  ACTIVE_ACCOUNT_REQUIRED: {
    title: "Choose an account",
    message: "An active Cristalix account is required.",
  },
  ACCOUNT_NOT_ACTIVE: {
    title: "Account is inactive",
    message: "Reactivate it or choose another account.",
  },
  USER_NOT_FOUND: {
    title: "User not found",
    message: "Check your request.",
  },
  ACCOUNT_NOT_FOUND: {
    title: "Account not found",
    message: "It may have been deleted.",
  },
  ACCOUNT_MEMBER_NOT_FOUND: {
    title: "Member not found",
    message: "The team may have changed already.",
  },
  ACTIVE_ACCOUNT_MEMBERSHIP_NOT_FOUND: {
    title: "Membership not found",
    message: "Check the active account.",
  },
  ACCOUNT_INVITE_NOT_FOUND: {
    title: "Invitation not found",
    message: "Check the code or create a new invitation.",
  },
  ROLE_NOT_FOUND: {
    title: "Role not found",
    message: "The role list may have changed already.",
  },
  ROLE_ASSIGNMENT_NOT_FOUND: {
    title: "Assignment not found",
    message: "Refresh the member list.",
  },
  BET_NOT_FOUND: {
    title: "Round not found",
    message: "It may have already closed.",
  },
  PERSONALIZATION_NOT_FOUND: {
    title: "Personalization not found",
    message: "It may have been deleted.",
  },
  BADGE_NOT_FOUND: {
    title: "Badge not found",
    message: "Refresh the badge list.",
  },
  SANCTION_NOT_FOUND: {
    title: "Sanction not found",
    message: "Its state may have changed already.",
  },
  SUBSCRIPTION_NOT_FOUND: {
    title: "Subscription not found",
    message: "Refresh the subscription list.",
  },
  CRISTALIX_PLAYER_NOT_FOUND: {
    title: "Player not found",
    message: "Check the nickname.",
  },
  COLLECTOR_CREDENTIAL_NOT_FOUND: {
    title: "Credential not found",
    message: "The credential may have been revoked.",
  },
  ENDPOINT_NOT_FOUND: {
    title: "Endpoint not found",
    message: "Refresh the page.",
  },
  USER_IDENTITY_CONFLICT: {
    title: "Profile already linked",
    message: "The user identity data conflicts.",
  },
  ACCOUNT_IDENTITY_CONFLICT: {
    title: "Account already linked",
    message: "Use a different Cristalix profile.",
  },
  ACCOUNT_DELETION_UNAVAILABLE: {
    title: "Account cannot be deleted now",
    message: "The current account state blocks deletion.",
  },
  ACCOUNT_MEMBER_LIMIT_REACHED: {
    title: "Team is full",
    message: "The member limit has been reached.",
  },
  ACCOUNT_MEMBER_ALREADY_EXISTS: {
    title: "Member already added",
    message: "No repeated action is needed.",
  },
  ACCOUNT_OWNER_CANNOT_LEAVE: {
    title: "Owner cannot leave",
    message: "Transfer ownership first.",
  },
  ACCOUNT_INVITE_UNAVAILABLE: {
    title: "Invitation unavailable",
    message: "It has already been used or has expired.",
  },
  ROLE_ALREADY_ASSIGNED: {
    title: "Role already assigned",
    message: "No repeated action is needed.",
  },
  ROLE_HIERARCHY_VIOLATION: {
    title: "Role order conflicts",
    message: "Change the role position.",
  },
  ROLE_HIERARCHY_INCONSISTENT: {
    title: "Role hierarchy changed",
    message: "Refresh the roles before editing.",
  },
  DAILY_REWARD_ALREADY_CLAIMED: {
    title: "Already claimed today",
    message: "Come back tomorrow.",
  },
  DAILY_STREAK_NOT_RECOVERABLE: {
    title: "Streak cannot be restored",
    message: "It does not meet the restore conditions.",
  },
  DAILY_STREAK_RESTORE_LIMIT_REACHED: {
    title: "No restores remaining",
    message: "The monthly restore limit has been used.",
  },
  BET_CONFLICT: {
    title: "Round has changed",
    message: "Refresh the data before placing a bet.",
  },
  PERSONALIZATION_ALREADY_OWNED: {
    title: "You already own it",
    message: "You do not need to purchase it again.",
  },
  PERSONALIZATION_NOT_PURCHASABLE: {
    title: "Not for sale right now",
    message: "The personalization status has changed.",
  },
  PURCHASE_IDEMPOTENCY_CONFLICT: {
    title: "Request was already processed",
    message: "You will not be charged twice.",
  },
  PERSONALIZATION_NOT_OWNED: {
    title: "Not in your collection",
    message: "Obtain the personalization first.",
  },
  PERSONALIZATION_OWNERSHIP_CONFLICT: {
    title: "Collection has changed",
    message: "Refresh the list.",
  },
  BADGE_ALREADY_EXISTS: {
    title: "Badge already exists",
    message: "Open the existing badge or change the data.",
  },
  BADGE_NOT_OWNED: {
    title: "You do not own this badge",
    message: "It cannot be activated.",
  },
  SANCTION_CONFLICT: {
    title: "State has changed",
    message: "Refresh the user data.",
  },
  SELF_SANCTION: {
    title: "You cannot sanction yourself",
    message: "Choose another user.",
  },
  WALLET_ADJUSTMENT_IDEMPOTENCY_CONFLICT: {
    title: "Adjustment already processed",
    message: "The repeated operation was blocked.",
  },
  VALIDATION_FAILED: {
    title: "Check the fields",
    message: "Some values are invalid.",
  },
  CONSTRAINT_VIOLATION: {
    title: "Invalid parameters",
    message: "Check the entered values.",
  },
  INVALID_REQUEST_BODY: {
    title: "Could not read the data",
    message: "Try the action again.",
  },
  INVALID_PARAMETER: {
    title: "Invalid parameter",
    message: "Check the value.",
  },
  MISSING_REQUEST_PARAMETER: {
    title: "Missing parameter",
    message: "Refresh the form and try again.",
  },
  MISSING_REQUEST_HEADER: {
    title: "Incomplete request",
    message: "Refresh the page.",
  },
  INVALID_ROLE_HIERARCHY: {
    title: "Invalid role order",
    message: "Check the positions in the hierarchy.",
  },
  INVALID_ACCOUNT_INVITE_EXPIRATION: {
    title: "Invalid expiration",
    message: "Change the invitation lifetime.",
  },
  SANCTION_DURATION_EXCEEDED: {
    title: "Duration is too long",
    message: "Shorten the sanction duration.",
  },
  METHOD_NOT_ALLOWED: {
    title: "Action not supported",
    message: "Refresh the page.",
  },
  UNSUPPORTED_MEDIA_TYPE: {
    title: "Format not supported",
    message: "Try again after refreshing.",
  },
  NOT_ACCEPTABLE: {
    title: "Response unavailable",
    message: "Refresh the page.",
  },
  PERSONALIZATION_PURCHASE_IN_PROGRESS: {
    title: "Purchase is being processed",
    message: "Do not start it again.",
  },
  ACCOUNT_UNAVAILABLE: {
    title: "Account unavailable",
    message: "Try again shortly.",
  },
  CRISTALIX_RATE_LIMIT_EXCEEDED: {
    title: "Too many requests",
    message: "Wait before trying again.",
  },
  CRISTALIX_UNAVAILABLE: {
    title: "Service unavailable",
    message: "Try again shortly.",
  },
  CRISTALIX_AUTHORIZATION_FAILED: {
    title: "Connection is not authorized",
    message: "Mnemos will retry the request later.",
  },
  CRISTALIX_INVALID_RESPONSE: {
    title: "Invalid response received",
    message: "Try again shortly.",
  },
  REALTIME_CONNECTION_LIMIT_EXCEEDED: {
    title: "Too many connections",
    message: "Close an extra connection or wait.",
  },
  REALTIME_CONNECTION_CAPACITY_UNAVAILABLE: {
    title: "Realtime is temporarily busy",
    message: "The connection will recover later.",
  },
  INTERNAL_SERVER_ERROR: {
    title: "Something went wrong",
    message: "Try the action again shortly.",
  },
} as const
