import { searchAccountDirectory } from "@/shared/api/moderationApi"
import { resolvePlayerByNickname } from "@/shared/api/playerDirectoryApi"
import type { LeaderboardExclusionSubjectType } from "@/shared/api/leaderboardExclusionApi"

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

export type ResolvedLeaderboardTarget = {
  id: string
  label: string
}

export async function resolveLeaderboardExclusionTarget(
  subjectType: LeaderboardExclusionSubjectType,
  input: string,
): Promise<ResolvedLeaderboardTarget | null> {
  const normalized = input.trim()

  if (!normalized) {
    return null
  }

  if (UUID_PATTERN.test(normalized)) {
    return {
      id: normalized,
      label: normalized,
    }
  }

  if (subjectType === "PLAYER") {
    const player = await resolvePlayerByNickname(normalized)

    return {
      id: player.playerId,
      label: player.nickname,
    }
  }

  const page = await searchAccountDirectory({
    query: normalized,
    page: 0,
    size: 20,
    order: "NEWEST",
  })
  const nickname = normalized.toLocaleLowerCase()
  const exact = page.content.find(
    (account) => account.cristalixNickname.toLocaleLowerCase() === nickname,
  )

  if (!exact) {
    return null
  }

  return {
    id: exact.accountId,
    label: exact.cristalixNickname,
  }
}
