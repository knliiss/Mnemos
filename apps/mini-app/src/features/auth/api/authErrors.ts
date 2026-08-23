export type AuthErrorCode =
  | "AUTH_UNAVAILABLE"
  | "AUTH_FAILED"
  | "TELEGRAM_REQUIRED"
  | "TELEGRAM_SESSION_EXPIRED"
  | "TOKEN_MISSING"

export class AuthError extends Error {
  constructor(
    public readonly code: AuthErrorCode,
    public readonly source?: unknown,
  ) {
    super(code)
    this.name = "AuthError"
  }
}

export function isAuthError(error: unknown, code?: AuthErrorCode): error is AuthError {
  if (!(error instanceof AuthError)) {
    return false
  }

  return code === undefined || error.code === code
}
