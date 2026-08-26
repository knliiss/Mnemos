import { useEffect, useState } from "react"
import {
  getPublicProfile,
  type ProfileAppearanceResponse,
} from "@/shared/api/profileSocialApi"

export function useAccountHomeAppearance(accountId: string): ProfileAppearanceResponse | null {
  const [appearance, setAppearance] = useState<ProfileAppearanceResponse | null>(null)

  useEffect(() => {
    let active = true

    setAppearance(null)

    void getPublicProfile(accountId)
      .then((profile) => {
        if (active) {
          setAppearance(profile.appearance)
        }
      })
      .catch(() => {
        if (active) {
          setAppearance(null)
        }
      })

    return () => {
      active = false
    }
  }, [accountId])

  return appearance
}
