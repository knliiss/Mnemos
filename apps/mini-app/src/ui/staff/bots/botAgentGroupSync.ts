import {
  getBotGroups,
  saveBotGroups,
  workerRequest,
  type RemoteGroupState,
} from "@/shared/api/botAgentApi"

type WorkerGroupState = RemoteGroupState & { initialized: boolean }

function stateData(data: RemoteGroupState["state"]) {
  return { groups: data.groups, assignments: data.assignments }
}

function sameGroupData(a: RemoteGroupState["state"], b: RemoteGroupState["state"]): boolean {
  return JSON.stringify(stateData(a)) === JSON.stringify(stateData(b))
}

function withMarker(
  state: RemoteGroupState["state"],
  workerRevision: number,
  nextServerRevision: number,
): RemoteGroupState["state"] {
  return {
    ...stateData(state),
    syncedWorkerRevision: workerRevision,
    syncedDbRevision: nextServerRevision,
  }
}

export async function synchronizeBotGroups(agentId: string): Promise<RemoteGroupState> {
  const database = await getBotGroups(agentId)
  const worker = await workerRequest<WorkerGroupState>(agentId, "GET", "/api/groups")

  const workerRevision = worker.revision
  const lastWorkerRevision = database.state.syncedWorkerRevision
  const lastDbRevision = database.state.syncedDbRevision
  const hasUntrackedState = lastWorkerRevision === undefined || lastDbRevision === undefined

  if (hasUntrackedState) {
    const hasDesktop =
      worker.initialized &&
      (worker.state.groups.length > 0 || Object.keys(worker.state.assignments).length > 0)
    const dbIds = new Set(database.state.groups.map((item) => item.id))
    const merged = {
      groups: [
        ...database.state.groups,
        ...(hasDesktop ? worker.state.groups.filter((item) => !dbIds.has(item.id)) : []),
      ],
      assignments: {
        ...(hasDesktop ? worker.state.assignments : {}),
        ...database.state.assignments,
      },
    }
    let syncedWorkerRevision = workerRevision
    if (!sameGroupData(merged, worker.state) || !worker.initialized) {
      const updated = await workerRequest<WorkerGroupState>(agentId, "POST", "/api/groups", {
        state: merged,
        revision: workerRevision,
      })
      syncedWorkerRevision = updated.revision
    }
    return saveBotGroups(agentId, {
      state: withMarker(merged, syncedWorkerRevision, database.revision + 1),
      revision: database.revision,
    })
  }

  const workerChanged = workerRevision !== lastWorkerRevision
  const serverChanged = database.revision !== lastDbRevision
  if (workerChanged && serverChanged) {
    throw new Error(
      "Groups changed independently on the PC and Mnemos. No data was overwritten. Refresh and resolve the conflict.",
    )
  }
  if (workerChanged) {
    return saveBotGroups(agentId, {
      state: withMarker(worker.state, workerRevision, database.revision + 1),
      revision: database.revision,
    })
  }
  if (serverChanged || !sameGroupData(database.state, worker.state)) {
    const updated = await workerRequest<WorkerGroupState>(agentId, "POST", "/api/groups", {
      state: stateData(database.state),
      revision: workerRevision,
    })
    return saveBotGroups(agentId, {
      state: withMarker(database.state, updated.revision, database.revision + 1),
      revision: database.revision,
    })
  }
  return database
}

export async function persistBotGroupChange(
  agentId: string,
  current: RemoteGroupState,
  nextState: RemoteGroupState["state"],
  online: boolean,
): Promise<RemoteGroupState> {
  let saved = await saveBotGroups(agentId, {
    state: {
      ...stateData(nextState),
      syncedWorkerRevision: current.state.syncedWorkerRevision,
      syncedDbRevision: current.state.syncedDbRevision,
    },
    revision: current.revision,
  })
  if (online) {
    saved = await synchronizeBotGroups(agentId)
  }
  return saved
}
