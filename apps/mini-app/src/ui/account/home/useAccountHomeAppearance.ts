import { useEffect, useState } from "react"
import {
  getPublicProfile,
  type ProfileAppearanceResponse,
} from "@/shared/api/profileSocialApi"

export function useAccountHomeAppearance(accountId: string): ProfileAppearanceResponse | null {
  const [appearance, setAppearance] = useState<ProfileAppearanceResponse | null>(null)

  useEffect(() => {
    let active = true
    const controller = new AbortController()

    setAppearance(null)

    void getPublicProfile(accountId)
      .then((profile) => {
        if (active && !controller.signal.aborted) {
          setAppearance(profile.appearance)
        }
      })
      .catch(() => {
        if (active && !controller.signal.aborted) {
          setAppearance(null)
        }
      })

    return () => {
      active = false
      controller.abort()
    }
  }, [accountId])

  return appearance
}
