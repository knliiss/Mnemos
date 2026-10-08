import { useEffect, useState } from "react"
import { workerRequest } from "@/shared/api/botAgentApi"
import { LeaderboardPlayerHead } from "../../leaderboards/LeaderboardPlayerHead"

const headCache = new Map<string, Promise<string | null>>()
const headQueue: Array<() => void> = []
let activeHeadRequests = 0

function drainHeadQueue() {
  while (activeHeadRequests < 3) {
    const next = headQueue.shift()
    if (!next) {
      break
    }
    activeHeadRequests += 1
    next()
  }
}

function limitedHeadRequest(factory: () => Promise<string | null>): Promise<string | null> {
  return new Promise((resolve) => {
    headQueue.push(() => {
      void factory()
        .then(resolve)
        .finally(() => {
          activeHeadRequests -= 1
          drainHeadQueue()
        })
    })
    drainHeadQueue()
  })
}

function fetchHead(agentId: string, accountId: string): Promise<string | null> {
  const key = agentId + "/" + accountId
  const previous = headCache.get(key)
  if (previous) {
    return previous
  }
  const request = limitedHeadRequest(() =>
    workerRequest<{ pngBase64: string }>(agentId, "GET", `/api/account/${accountId}/head`)
      .then((result) => {
        if (!/^[a-zA-Z0-9+/]+=*$/.test(result.pngBase64) || result.pngBase64.length > 125_000) {
          return null
        }
        return `data:image/png;base64,${result.pngBase64}`
      })
      .catch(() => null),
  )
  headCache.set(key, request)
  return request
}

export function BotAccountHead({
  agentId,
  accountId,
  nickname,
  size,
  online,
}: {
  agentId: string
  accountId: string
  nickname: string
  size: number
  online: boolean
}) {
  const [head, setHead] = useState<{ key: string; url: string | null } | null>(null)
  const key = agentId + "/" + accountId

  useEffect(() => {
    if (!online) {
      return
    }
    let live = true
    void fetchHead(agentId, accountId).then((url) => {
      if (live) {
        setHead({ key, url })
      }
    })
    return () => {
      live = false
    }
  }, [accountId, agentId, key, online])

  if (online && head?.key !== key) {
    return (
      <img
        className="c7x-agent-head-image"
        style={{ width: size, height: size }}
        src="/ui-illustrations/bot-agent-account.svg"
        alt=""
        width={size}
        height={size}
      />
    )
  }

  if (head?.key === key && head.url) {
    return (
      <img
        className="c7x-agent-head-image"
        style={{ width: size, height: size }}
        src={head.url}
        onError={() => setHead({ key, url: null })}
        alt=""
        width={size}
        height={size}
      />
    )
  }
  return (
    <LeaderboardPlayerHead
      entry={{ rank: 0, nickname, metric: "", detail: "" }}
      size={size}
      className="c7x-agent-minecraft-head"
    />
  )
}
