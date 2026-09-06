import type { SystemRole } from "../model"

export function canAccessGuides(systemRoles: SystemRole[]) {
  return systemRoles.includes("ADMIN") || systemRoles.includes("SUPER_ADMIN")
}
