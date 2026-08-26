import { useEffect, useState } from "react"
import {
  getPublicProfile,
  type ProfileAppearanceResponse,
} from "@/shared/api/profileSocialApi"

type AppearanceState = {
  accountId: string
  appearance: ProfileAppearanceResponse | null
}

export function useAccountHomeAppearance(accountId: string): ProfileAppearanceResponse | null {
  const [state, setState] = useState<AppearanceState | null>(null)

  useEffect(() => {
    let active = true

    void getPublicProfile(accountId)
      .then((profile) => {
        if (active) {
          setState({
            accountId,
            appearance: profile.appearance,
          })
        }
      })
      .catch(() => {
        if (active) {
          setState({
            accountId,
            appearance: null,
          })
        }
      })

    return () => {
      active = false
    }
  }, [accountId])

  return state?.accountId === accountId ? state.appearance : null
}
