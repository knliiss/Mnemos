import type {
  ClaimDailyRewardResponse,
  DailyRewardStateResponse,
} from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"

export async function getDailyRewardState(): Promise<DailyRewardStateResponse> {
  return api<DailyRewardStateResponse>("/api/v1/rewards/daily")
}

export async function claimDailyReward(): Promise<ClaimDailyRewardResponse> {
  return api<ClaimDailyRewardResponse>("/api/v1/rewards/daily/claim", { method: "POST" })
}

export async function restoreDailyReward(): Promise<{ currentStreak: number }> {
  return api<{ currentStreak: number }>("/api/v1/rewards/daily/restore", { method: "POST" })
}
