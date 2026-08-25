import { useEffect, useMemo, useState } from "react"
import { getPublicProfile, searchPublicProfiles } from "@/shared/api/profileSocialApi"
import { CristalixHead } from "../components/profile/CristalixHead"
import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"
import "./leaderboard-polish.css"

const SKIN_BASE_URL = "https://webdata.c7x.dev/textures/skin"
const FALLBACK_SKIN_URL = "data:image/gif;base64,R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs="

type ResolvedLeaderboardProfile = {
  accountId: string
  nickname: string
  skinUrl: string
}

const profileRequests = new Map<string, Promise<ResolvedLeaderboardProfile | null>>()

function nicknameKey(nickname: string): string {
  return nickname.trim().toLocaleLowerCase()
}

function accountKey(accountId: string): string {
  return `account:${accountId}`
}

function playerKey(accountId: string | undefined, nickname: string): string {
  return accountId ? accountKey(accountId) : `nickname:${nicknameKey(nickname)}`
}

function toResolvedProfile(profile: {
  accountId: string
  cristalixUuid: string
  cristalixNickname: string
}): ResolvedLeaderboardProfile {
  return {
    accountId: profile.accountId,
    nickname: profile.cristalixNickname,
    skinUrl: `${SKIN_BASE_URL}/${profile.cristalixUuid}`,
  }
}

function cacheResolvedProfile(profile: ResolvedLeaderboardProfile): void {
  const resolved = Promise.resolve(profile)

  profileRequests.set(accountKey(profile.accountId), resolved)
  profileRequests.set(`nickname:${nicknameKey(profile.nickname)}`, resolved)
}

function resolveLeaderboardProfile(
  accountId: string | undefined,
  nickname: string,
): Promise<ResolvedLeaderboardProfile | null> {
  const key = playerKey(accountId, nickname)
  const cached = profileRequests.get(key)

  if (cached) {
    return cached
  }

  const request = accountId
    ? getPublicProfile(accountId).then(toResolvedProfile)
    : searchPublicProfiles(nickname, 0, 20).then((profiles) => {
        const normalizedNickname = nicknameKey(nickname)
        const exact = profiles.content.find(
          (profile) => nicknameKey(profile.cristalixNickname) === normalizedNickname,
        )

        return exact ? toResolvedProfile(exact) : null
      })

  const safeRequest = request
    .then((profile) => {
      if (profile) {
        cacheResolvedProfile(profile)
      }

      return profile
    })
    .catch(() => null)

  profileRequests.set(key, safeRequest)

  return safeRequest
}

export function LeaderboardPlayerHead({
  entry,
  size,
  className,
}: {
  entry: LeaderboardDisplayEntry
  size: number
  className?: string
}) {
  const identityKey = useMemo(
    () => playerKey(entry.accountId, entry.nickname),
    [entry.accountId, entry.nickname],
  )
  const [resolved, setResolved] = useState<{
    identityKey: string
    skinUrl: string | null
  }>({
    identityKey: "",
    skinUrl: null,
  })

  useEffect(() => {
    let cancelled = false

    void resolveLeaderboardProfile(entry.accountId, entry.nickname).then((profile) => {
      if (cancelled) {
        return
      }

      setResolved({
        identityKey,
        skinUrl: profile?.skinUrl ?? null,
      })
    })

    return () => {
      cancelled = true
    }
  }, [entry.accountId, entry.nickname, identityKey])

  const skinUrl = resolved.identityKey === identityKey ? resolved.skinUrl : null

  return <CristalixHead skinUrl={skinUrl ?? FALLBACK_SKIN_URL} size={size} className={className} />
}
