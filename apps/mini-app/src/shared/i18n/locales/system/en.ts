export const systemEn = {
  availability: {
    eyebrow: "CONNECTION LOST",
    title: "Mnemos is temporarily unavailable",
    description:
      "Mnemos services are not responding right now. We will automatically try to connect again.",
    retry: "Try again",
    retrying: "Checking the connection...",
  },
  authRecovery: {
    eyebrow: "SESSION",
    title: "The Telegram session needs to be refreshed",
    description:
      "We do not send stale authentication data. Mnemos will first try to restore the existing session.",
    hint: "If this message remains, close the Mini App and open it again in Telegram.",
    retry: "Check again",
    retrying: "Restoring the session...",
  },
  authFailure: {
    eyebrow: "AUTHORIZATION",
    title: "Could not sign in to Mnemos",
    description: "Authorization could not be completed. Try again.",
    retry: "Retry",
    retrying: "Retrying authorization...",
  },
  ban: {
    eyebrow: "ACCESS SUSPENDED",
    title: "Your Mnemos access is blocked",
    description:
      "This restriction applies to your Mnemos user account until the sanction is lifted.",
    reason: "Reason",
    reasonUnavailable: "No reason was provided.",
    remaining: "Time until unban",
    permanent: "Permanent ban",
    expiresAt: "Access returns",
    hint: "The page will unlock automatically when the ban expires or is revoked.",
    days: "days",
    hours: "hours",
    minutes: "min",
    seconds: "sec",
  },
}
