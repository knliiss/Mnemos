export type AppErrorKind = "network" | "api" | "validation" | "auth" | "unexpected"

export class AppError extends Error {
  constructor(
    public readonly kind: AppErrorKind,
    message: string,
    public readonly cause?: unknown,
  ) {
    super(message)
    this.name = "AppError"
  }
}
