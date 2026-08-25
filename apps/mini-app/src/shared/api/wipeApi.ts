import { api } from "@/shared/api/httpClient"

export type Wipe = {
  id: string
  number: number
  name: string | null
  startedAt: string
  endedAt: string | null
  active: boolean
  createdBy: string
  createdAt: string
}

export async function listWipes(): Promise<Wipe[]> {
  return api<Wipe[]>("/api/v1/events/wipes")
}

export async function getCurrentWipe(): Promise<Wipe> {
  return api<Wipe>("/api/v1/events/wipes/current")
}

export async function startWipe(request: {
  name: string | null
  startedAt: string
}): Promise<Wipe> {
  return api<Wipe>("/api/v1/events/admin/wipes", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  })
}
