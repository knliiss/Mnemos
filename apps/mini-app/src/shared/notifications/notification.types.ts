export type NotificationKind = "info" | "success" | "warning" | "error"

export interface AppNotification {
  id: string
  kind: NotificationKind
  message: string
  persistent?: boolean
}
