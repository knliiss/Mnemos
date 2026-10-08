import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { useMnemosData } from "@/ui/data/MnemosDataProvider"
import {
  botAccounts,
  botAction,
  botChat,
  botLogs,
  botManager,
  botSettings,
  createBotAgent,
  getBotAgents,
  renameBotAgent,
  revokeBotAgent,
  workerRequest,
  getBotGroups,
  saveBotGroups,
  getBotProxyPools,
  modifyBotProxyPool,
  getBotNetwork,
  updateBotNetwork,
  type RemoteGroupState,
  type ProxyPool,
  type AccountNetwork,
  type AgentCreation,
  type BotAccount,
  type BotAgent,
} from "@/shared/api/botAgentApi"
import { handleError } from "@/shared/errors/errorHandler"
import { PageHeader } from "../../shared/Primitives"
import "./bot-agents.css"

const DUNGEONS = ["LIGHT", "MEDIUM", "HARD", "EXPERT"] as const

export function BotAgentsAdmin({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const { user } = useMnemosData()
  const authorized = user.systemRoles.some((role) => role === "ADMIN" || role === "SUPER_ADMIN")
  const [agents, setAgents] = useState<BotAgent[]>([])
  const [selected, setSelected] = useState<string | null>(null)
  const [created, setCreated] = useState<AgentCreation | null>(null)
  const [accounts, setAccounts] = useState<BotAccount[]>([])
  const [accountId, setAccountId] = useState<string | null>(null)
  const [chosen, setChosen] = useState<string[]>([])
  const logCursor = useRef(0)
  const [logEntries, setLogEntries] = useState<
    { sequence: number; timestamp: string; category: string; message: string }[]
  >([])
  const [logError, setLogError] = useState<string | null>(null)
  const [logLoading, setLogLoading] = useState(false)
  const [renameAgent, setRenameAgent] = useState<BotAgent | null>(null)
  const [renameValue, setRenameValue] = useState("")
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState("")
  const [query, setQuery] = useState("")
  const [login, setLogin] = useState(false)
  const [loginName, setLoginName] = useState("")
  const [loginPassword, setLoginPassword] = useState("")
  const [loginOtp, setLoginOtp] = useState("")
  const [chatText, setChatText] = useState("")
  const [bulkChatText, setBulkChatText] = useState("")
  const [realm, setRealm] = useState("1")
  const [navigation, setNavigation] = useState({ x: "0", y: "64", z: "0" })
  const [groupState, setGroupState] = useState<RemoteGroupState>({
    state: { groups: [], assignments: {} },
    revision: 0,
  })
  const [groupFilter, setGroupFilter] = useState("")
  const [groupName, setGroupName] = useState("")
  const [proxyPools, setProxyPools] = useState<ProxyPool[]>([])
  const [poolEditor, setPoolEditor] = useState(false)
  const [poolName, setPoolName] = useState("")
  const [poolProxies, setPoolProxies] = useState("")
  const [poolStrategy, setPoolStrategy] = useState("STICKY")
  const [network, setNetwork] = useState<AccountNetwork | null>(null)
  const [proxyHost, setProxyHost] = useState("")
  const [proxyPort, setProxyPort] = useState("1080")
  const [proxyUsername, setProxyUsername] = useState("")
  const [proxyPassword, setProxyPassword] = useState("")
  const [proxyType, setProxyType] = useState("SOCKS5")
  const [networkMode, setNetworkMode] = useState("DIRECT")
  const [showDiagnostics, setShowDiagnostics] = useState(false)
  const [diagnostics, setDiagnostics] = useState<unknown>(null)

  const active = agents.find((agent) => agent.id === selected)
  const account = accounts.find((entry) => entry.id === accountId)
  const visibleAccounts = useMemo(
    () =>
      accounts.filter(
        (entry) =>
          entry.username.toLowerCase().includes(query.toLowerCase()) &&
          (!groupFilter || groupState.state.assignments[entry.id] === groupFilter),
      ),
    [accounts, query, groupFilter, groupState],
  )
  const formatDate = (date: string | null) =>
    date ? new Date(date).toLocaleString() : t("botAgents.never")

  const refreshAgents = useCallback(async () => {
    if (!authorized) {
      return
    }
    try {
      setAgents(await getBotAgents())
    } catch (error) {
      handleError(error)
    }
  }, [authorized])

  const refreshAccounts = useCallback(async (id: string) => {
    try {
      setAccounts(await botAccounts(id))
    } catch (error) {
      handleError(error)
    }
  }, [])

  useEffect(() => {
    if (!authorized) {
      return
    }
    const initial = window.setTimeout(() => void refreshAgents(), 0)
    const timer = window.setInterval(() => void refreshAgents(), 10_000)
    return () => {
      window.clearTimeout(initial)
      window.clearInterval(timer)
    }
  }, [authorized, refreshAgents])

  useEffect(() => {
    let cancelled = false
    if (selected) {
      const agentId = selected
      void (async () => {
        try {
          let data = await getBotGroups(agentId)
          if (active?.online) {
            const worker = await workerRequest<RemoteGroupState & { initialized: boolean }>(
              agentId, "GET", "/api/groups",
            )
            const serverEmpty = data.revision === 0 &&
              data.state.groups.length === 0 &&
              Object.keys(data.state.assignments).length === 0
            if (serverEmpty && worker.initialized && worker.state.groups.length) {
              data = await saveBotGroups(agentId, { state: worker.state, revision: data.revision })
            } else if (
              JSON.stringify(worker.state) !== JSON.stringify(data.state)
            ) {
              await workerRequest(agentId, "POST", "/api/groups", {
                state: data.state, revision: worker.revision,
              })
            }
          }
          if (!cancelled) setGroupState(data)
        } catch (error) {
          if (!cancelled) {
            setNotice(error instanceof Error ? error.message : t("botAgents.groupSyncError"))
            handleError(error)
          }
        }
      })()
    }
    let timer: number | undefined
    let initial: number | undefined
    if (selected && active?.online) {
      void getBotProxyPools(selected)
        .then((value) => {
          if (!cancelled) {
            setProxyPools(value.pools)
          }
        })
        .catch(handleError)
      initial = window.setTimeout(() => void refreshAccounts(selected), 0)
      timer = window.setInterval(() => void refreshAccounts(selected), 10_000)
    }
    return () => {
      cancelled = true
      if (initial !== undefined) {
        window.clearTimeout(initial)
      }
      if (timer !== undefined) {
        window.clearInterval(timer)
      }
    }
  }, [selected, active?.online, refreshAccounts, t])

  useEffect(() => {
    logCursor.current = 0
    setLogEntries([])
    setLogError(null)
    if (!selected || !accountId || !active?.online) {
      return
    }
    let running = true
    let inFlight = false
    const poll = async () => {
      if (inFlight || !running || document.visibilityState === "hidden") return
      inFlight = true
      setLogLoading(true)
      try {
        let result = await botLogs(selected, accountId, logCursor.current)
        if (logCursor.current === 0 && result.lastSequence > 80) {
          result = await botLogs(selected, accountId, Math.max(0, result.lastSequence - 80))
        }
        if (!running) return
        setLogError(null)
        if (result.entries.length) {
          logCursor.current = result.entries[result.entries.length - 1].sequence
          setLogEntries((previous) => {
            const merged = new Map(previous.map((entry) => [entry.sequence, entry]))
            for (const entry of result.entries) merged.set(entry.sequence, entry)
            return [...merged.values()].sort((a, b) => a.sequence - b.sequence).slice(-400)
          })
        }
      } catch (error) {
        if (running) setLogError(error instanceof Error ? error.message : t("botAgents.logError"))
      } finally {
        inFlight = false
        if (running) setLogLoading(false)
      }
    }
    void poll()
    const timer = window.setInterval(() => void poll(), 2_000)
    return () => {
      running = false
      window.clearInterval(timer)
    }
  }, [selected, accountId, active?.online, t])

  useEffect(() => {
    if (!selected || !accountId || !active?.online) {
      return
    }
    let cancelled = false
    void getBotNetwork(selected, accountId)
      .then((value) => {
        if (cancelled) {
          return
        }
        setNetwork(value)
        setNetworkMode(value.proxy.mode)
        setProxyHost(value.proxy.host ?? "")
        setProxyPort(String(value.proxy.port ?? 1080))
        setProxyUsername(value.proxy.username ?? "")
        setProxyType(value.proxy.type ?? "SOCKS5")
        setProxyPassword("")
      })
      .catch(handleError)
    return () => {
      cancelled = true
    }
  }, [selected, accountId, active?.online])

  function chooseWorker(id: string) {
    setSelected(id)
    setAccounts([])
    setAccountId(null)
    setLogEntries([])
    setChosen([])
    setProxyPools([])
    setGroupFilter("")
    setGroupState({ state: { groups: [], assignments: {} }, revision: 0 })
    setPoolEditor(false)
    setPoolName("")
    setPoolProxies("")
    setLogin(false)
    setLoginPassword("")
    setNetwork(null)
    logCursor.current = 0
  }

  function chooseAccount(id: string) {
    setAccountId(id)
    setNetwork(null)
    setLogEntries([])
    logCursor.current = 0
  }

  async function run(task: () => Promise<unknown>, refresh = true) {
    setBusy(true)
    setNotice("")
    try {
      await task()
      setNotice(t("botAgents.done"))
      if (selected && refresh) {
        await refreshAccounts(selected)
      }
    } catch (error) {
      setNotice(error instanceof Error ? error.message : t("botAgents.actionFailed"))
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  async function addWorker() {
    setBusy(true)
    try {
      setCreated(await createBotAgent())
      await refreshAgents()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  function renameWorker(agent: BotAgent) {
    setRenameAgent(agent)
    setRenameValue(agent.name)
  }

  async function submitRename() {
    if (!renameAgent || !renameValue.trim() || renameValue.trim() === renameAgent.name) {
      setRenameAgent(null)
      return
    }
    const agent = renameAgent
    const name = renameValue.trim()
    await run(async () => {
      await renameBotAgent(agent.id, name)
      await refreshAgents()
      setRenameAgent(null)
    }, false)
  }

  async function removeWorker(agent: BotAgent) {
    if (!window.confirm(t("botAgents.revokeConfirm", { name: agent.name }))) {
      return
    }
    await run(async () => {
      await revokeBotAgent(agent.id)
      await refreshAgents()
    }, false)
  }

  async function act(action: string, extra: Record<string, unknown> = {}) {
    if (!selected || !account) {
      return
    }
    await run(() => botAction(selected, account.id, action, extra))
  }

  async function settings(patch: Record<string, unknown>) {
    if (!selected || !account) {
      return
    }
    await run(() =>
      botSettings(selected, account.id, {
        autoDungeon: account.autoDungeon,
        swapper: account.swapper,
        autoReconnect: account.autoReconnect,
        dungeons: account.dungeons,
        realm: account.targetRealm,
        ...patch,
      }),
    )
  }

  async function bulk(action: string, extra: Record<string, unknown> = {}) {
    if (!selected) {
      return
    }
    await run(() => botManager(selected, { action, ids: chosen, ...extra }))
  }

  async function addAccount() {
    if (!selected || !loginName || !loginPassword) {
      return
    }
    await run(async () => {
      await workerRequest(selected, "POST", "/api/accounts/manage", {
        action: "login",
        username: loginName,
        password: loginPassword,
        otpCode: loginOtp,
        connect: false,
      })
      setLogin(false)
      setLoginPassword("")
      setLoginOtp("")
      setLoginName("")
    })
  }

  async function updateGroups(state: RemoteGroupState["state"]) {
    if (!selected) {
      return
    }
    await run(async () => {
      const workerId = selected
      const next = await saveBotGroups(workerId, { state, revision: groupState.revision })
      setGroupState(next)
      if (active?.online) {
        const current = await workerRequest<RemoteGroupState>(workerId, "GET", "/api/groups")
        await workerRequest(workerId, "POST", "/api/groups", {
          state: next.state, revision: current.revision,
        })
      }
    }, false)
  }

  async function addGroup() {
    const name = groupName.trim()
    if (!name || name.length > 64 || !selected) {
      return
    }
    const id = crypto.randomUUID()
    await updateGroups({
      groups: [...groupState.state.groups, { id, name }],
      assignments: groupState.state.assignments,
    })
    setGroupName("")
  }

  async function deleteGroup(id: string) {
    if (!window.confirm(t("botAgents.deleteGroupConfirm"))) {
      return
    }
    const assignments = Object.fromEntries(
      Object.entries(groupState.state.assignments).filter(([, groupId]) => groupId !== id),
    )
    await updateGroups({
      groups: groupState.state.groups.filter((group) => group.id !== id),
      assignments,
    })
    if (groupFilter === id) {
      setGroupFilter("")
    }
  }

  async function assignGroup(accountId: string, id: string) {
    const assignments = { ...groupState.state.assignments }
    if (id) {
      assignments[accountId] = id
    } else {
      delete assignments[accountId]
    }
    await updateGroups({ ...groupState.state, assignments })
  }

  async function applyNetwork(changes: Record<string, unknown>) {
    if (!selected || !accountId) {
      return
    }
    await run(async () => {
      const next = await updateBotNetwork(selected, accountId, changes)
      setNetwork(next)
      setNetworkMode(next.proxy.mode)
    })
  }

  async function proxyOperation(data: Record<string, unknown>) {
    if (!selected) {
      return
    }
    await run(async () => {
      const result = await modifyBotProxyPool(selected, data)
      setProxyPools(result.pools)
    }, false)
  }

  if (!authorized) {
    return (
      <div className="screen">
        <PageHeader title={t("botAgents.bots")} back={back} />
        <p>{t("botAgents.adminOnly")}</p>
      </div>
    )
  }

  return (
    <div className="screen c7x-admin">
      <PageHeader
        title={t("botAgents.pageTitle")}
        back={back}
        actions={
          <button
            type="button"
            className="c7x-admin-primary"
            onClick={() => void addWorker()}
            disabled={busy}
          >
            {t("botAgents.addAgent")}
          </button>
        }
      />
      <section className="c7x-admin-hero">
        <div className="c7x-admin-hero-copy">
          <span className="c7x-admin-eyebrow">C7X · REMOTE CONTROL</span>
          <strong>{t("botAgents.heroTitle")}</strong>
          <p>{t("botAgents.heroDescription")}</p>
        </div>
        <img className="c7x-admin-hero-art" src="/ui-illustrations/bot-agent-hero.svg" alt="" />
        <div className="c7x-admin-total">
          <b>{agents.filter((a) => a.online).length}</b>
          <span>{t("botAgents.onlineCount", { count: agents.length })}</span>
        </div>
      </section>

      {created ? (
        <section className="c7x-admin-card c7x-admin-pair">
          <strong>{t("botAgents.pairTitle")}</strong>
          <p>{t("botAgents.pairInstructions")}</p>
          <code>{created.accessKey}</code>
          <div className="c7x-admin-actions">
            <button
              type="button"
              onClick={() => void navigator.clipboard.writeText(created.accessKey)}
            >
              {t("botAgents.copyKey")}
            </button>
            <button type="button" onClick={() => setCreated(null)}>
              {t("botAgents.close")}
            </button>
          </div>
        </section>
      ) : null}

      <div className="c7x-admin-section-head">
        <h2>{t("botAgents.agents")}</h2>
        <button type="button" onClick={() => void refreshAgents()}>
          {t("botAgents.refresh")}
        </button>
      </div>
      {agents.length === 0 ? (
        <div className="c7x-admin-empty">
          <img src="/ui-illustrations/bot-agent-offline.svg" alt="" />
          <strong>{t("botAgents.noAgents")}</strong>
          <p>{t("botAgents.noAgentsHint")}</p>
        </div>
      ) : null}
      <div className="c7x-admin-agent-list">
        {agents.map((agent) => (
          <div
            className={`c7x-admin-card c7x-admin-agent ${selected === agent.id ? "is-selected" : ""}`}
            key={agent.id}
          >
            <button
              type="button"
              className="c7x-admin-agent-main"
              onClick={() => chooseWorker(agent.id)}
            >
              <span className="c7x-admin-worker-art">
                <img src="/ui-illustrations/bot-agent-workspace.svg" alt="" />
              </span>
              <span>
                <strong>{agent.name}</strong>
                <small>
                  <i className={`c7x-admin-dot ${agent.online ? "is-online" : ""}`} />{" "}
                  {agent.machineName ?? t("botAgents.unknownPc")} ·{" "}
                  {agent.revoked
                    ? t("botAgents.revoked")
                    : agent.online
                      ? t("botAgents.running")
                      : t("botAgents.disabled")}
                </small>
              </span>
              <span className="c7x-admin-last">
                {agent.online
                  ? t("botAgents.online")
                  : t("botAgents.lastSeen", { date: formatDate(agent.lastSeenAt) })}
              </span>
            </button>
            <div className="c7x-admin-agent-actions">
              <button
                type="button"
                onClick={() => void renameWorker(agent)}
                disabled={busy || agent.revoked}
              >
                {t("botAgents.name")}
              </button>
              {!agent.revoked ? (
                <button type="button" onClick={() => void removeWorker(agent)} disabled={busy}>
                  {t("botAgents.revoke")}
                </button>
              ) : null}
            </div>
          </div>
        ))}
      </div>

      {active ? (
        <section className="c7x-admin-workspace">
          <div className="c7x-admin-section-head">
            <h2>{active.name}</h2>
            <span className="c7x-admin-muted">
              {active.osUsername ?? "—"} ·{" "}
              {active.fingerprint?.slice(0, 12) ?? t("botAgents.noFingerprint")}
            </span>
          </div>
          {!active.online ? (
            <div className="c7x-admin-card c7x-admin-offline-card">
              <img src="/ui-illustrations/bot-agent-offline.svg" alt="" />
              <strong>{t("botAgents.agentInactive")}</strong>
              <p>{t("botAgents.lastConnected", { date: formatDate(active.lastSeenAt) })}</p>
            </div>
          ) : (
            <>
              <div className="c7x-admin-section-head">
                <h2>{t("botAgents.accountsCount", { count: accounts.length })}</h2>
                <button type="button" onClick={() => setLogin((x) => !x)}>
                  {t("botAgents.addAccount")}
                </button>
              </div>
              {login ? (
                <form
                  className="c7x-admin-card c7x-admin-form"
                  onSubmit={(e) => {
                    e.preventDefault()
                    void addAccount()
                  }}
                >
                  <label>
                    {t("botAgents.login")}
                    <input
                      autoComplete="username"
                      value={loginName}
                      onChange={(e) => setLoginName(e.target.value)}
                      required
                    />
                  </label>
                  <label>
                    {t("botAgents.password")}
                    <input
                      type="password"
                      autoComplete="new-password"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      required
                    />
                  </label>
                  <label>
                    {t("botAgents.otp")}
                    <input value={loginOtp} onChange={(e) => setLoginOtp(e.target.value)} />
                  </label>
                  <button type="submit" className="c7x-admin-primary" disabled={busy}>
                    {t("botAgents.addToPc")}
                  </button>
                </form>
              ) : null}
              <section className="c7x-admin-card c7x-admin-group-panel">
                <div className="c7x-admin-section-head">
                  <h3>{t("botAgents.accountGroups")}</h3>
                  <span>{t("botAgents.savedInMnemos")}</span>
                </div>
                <form
                  className="c7x-admin-form-inline"
                  onSubmit={(event) => {
                    event.preventDefault()
                    void addGroup()
                  }}
                >
                  <input
                    value={groupName}
                    maxLength={64}
                    placeholder={t("botAgents.newGroup")}
                    onChange={(event) => setGroupName(event.target.value)}
                  />
                  <button type="submit" disabled={busy || !groupName.trim()}>
                    {t("botAgents.create")}
                  </button>
                </form>
                <div className="c7x-admin-group-tags">
                  <button
                    type="button"
                    className={!groupFilter ? "is-active" : ""}
                    onClick={() => setGroupFilter("")}
                  >
                    {t("botAgents.all")}
                  </button>
                  {groupState.state.groups.map((group) => (
                    <div className="c7x-admin-group-tag" key={group.id}>
                      <button
                        type="button"
                        className={groupFilter === group.id ? "is-active" : ""}
                        onClick={() => setGroupFilter(group.id)}
                      >
                        {group.name}
                      </button>
                      <button
                        type="button"
                        aria-label={t("botAgents.deleteGroupAria", { name: group.name })}
                        onClick={() => void deleteGroup(group.id)}
                        disabled={busy}
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              </section>
              <input
                className="c7x-admin-search"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder={t("botAgents.searchNick")}
              />
              <div className="c7x-admin-actions">
                <button type="button" onClick={() => void bulk("connectAll")} disabled={busy}>
                  {t("botAgents.startAll")}
                </button>
                <button type="button" onClick={() => void bulk("disconnectAll")} disabled={busy}>
                  {t("botAgents.stopAll")}
                </button>
                <button
                  type="button"
                  onClick={() => void bulk("cancelBulkConnect")}
                  disabled={busy}
                >
                  {t("botAgents.cancelQueue")}
                </button>
                {chosen.length > 0 ? (
                  <>
                    <button type="button" onClick={() => void bulk("bulkConnect")} disabled={busy}>
                      {t("botAgents.startSelected", { count: chosen.length })}
                    </button>
                    <button
                      type="button"
                      onClick={() => void bulk("bulkReconnect")}
                      disabled={busy}
                    >
                      {t("botAgents.reconnect")}
                    </button>
                    <button
                      type="button"
                      onClick={() => void bulk("bulkDisconnect")}
                      disabled={busy}
                    >
                      {t("botAgents.stop")}
                    </button>
                    <button type="button" onClick={() => void bulk("bulkEnterSao")} disabled={busy}>
                      {t("botAgents.enterSao")}
                    </button>
                    <button
                      type="button"
                      onClick={() => void bulk("bulkDungeonLobby")}
                      disabled={busy}
                    >
                      {t("botAgents.dungeonLobby")}
                    </button>
                    <button
                      type="button"
                      onClick={() => void bulk("bulkAutomation", { autoDungeon: true })}
                      disabled={busy}
                    >
                      {t("botAgents.enableDungeons")}
                    </button>
                    <button
                      type="button"
                      onClick={() => void bulk("bulkAutomation", { autoDungeon: false })}
                      disabled={busy}
                    >
                      {t("botAgents.disableDungeons")}
                    </button>
                    <button type="button" onClick={() => void bulk("bulkDirect")} disabled={busy}>
                      {t("botAgents.direct")}
                    </button>
                    <button
                      type="button"
                      onClick={() => void bulk("bulkSpoofGenerate")}
                      disabled={busy}
                    >
                      {t("botAgents.newSpoof")}
                    </button>
                    <button
                      type="button"
                      onClick={() => void bulk("bulkStopNavigation")}
                      disabled={busy}
                    >
                      {t("botAgents.stopNavigation")}
                    </button>
                  </>
                ) : null}
              </div>
              {chosen.length > 0 ? (
                <section className="c7x-admin-card c7x-admin-bulk">
                  <div className="c7x-admin-section-head">
                    <h3>{t("botAgents.bulkCommands", { count: chosen.length })}</h3>
                  </div>
                  <div className="c7x-admin-actions">
                    {DUNGEONS.map((dungeon) => (
                      <button
                        key={dungeon}
                        type="button"
                        disabled={busy}
                        onClick={() => void bulk("bulkDungeon", { dungeon })}
                      >
                        {dungeon}
                      </button>
                    ))}
                  </div>
                  <div className="c7x-admin-form-inline">
                    {(["x", "y", "z"] as const).map((axis) => (
                      <label key={axis}>
                        {axis.toUpperCase()}
                        <input
                          type="number"
                          value={navigation[axis]}
                          onChange={(event) =>
                            setNavigation((old) => ({ ...old, [axis]: event.target.value }))
                          }
                        />
                      </label>
                    ))}
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() =>
                        void bulk("bulkNavigate", {
                          x: Number(navigation.x),
                          y: Number(navigation.y),
                          z: Number(navigation.z),
                          reach: 1,
                        })
                      }
                    >
                      {t("botAgents.navigateSelected")}
                    </button>
                  </div>
                  <div className="c7x-admin-form-inline">
                    <label>
                      {t("botAgents.targetRealm")}
                      <input
                        type="number"
                        min="1"
                        value={realm}
                        onChange={(event) => setRealm(event.target.value)}
                      />
                    </label>
                    <button
                      type="button"
                      disabled={busy || Number(realm) < 1}
                      onClick={() =>
                        void bulk("bulkAutomation", { swapper: true, realm: Number(realm) })
                      }
                    >
                      {t("botAgents.enableSwapper")}
                    </button>
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => void bulk("bulkAutomation", { swapper: false })}
                    >
                      {t("botAgents.disableSwapper")}
                    </button>
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => void bulk("bulkAutomation", { autoReconnect: true })}
                    >
                      {t("botAgents.autoReconnectCheck")}
                    </button>
                  </div>
                  <form
                    className="c7x-admin-form-inline"
                    onSubmit={(event) => {
                      event.preventDefault()
                      if (!bulkChatText.trim()) {
                        return
                      }
                      void bulk("bulkChat", { message: bulkChatText.trim() })
                    }}
                  >
                    <input
                      value={bulkChatText}
                      onChange={(event) => setBulkChatText(event.target.value)}
                      maxLength={256}
                      placeholder={t("botAgents.bulkMessagePlaceholder")}
                    />
                    <button type="submit" disabled={busy || !bulkChatText.trim()}>
                      {t("botAgents.send")}
                    </button>
                  </form>
                </section>
              ) : null}
              <div className="c7x-admin-accounts">
                {visibleAccounts.length === 0 ? (
                  <div className="c7x-admin-empty">
                    <img src="/ui-illustrations/bot-agent-account.svg" alt="" />
                    <strong>{t("botAgents.noAccounts")}</strong>
                  </div>
                ) : null}
                {visibleAccounts.map((item) => (
                  <div key={item.id} className="c7x-admin-account-line">
                    <input
                      type="checkbox"
                      aria-label={t("botAgents.selectAccountAria", { name: item.username })}
                      checked={chosen.includes(item.id)}
                      onChange={(e) =>
                        setChosen((current) =>
                          e.target.checked
                            ? [...current, item.id]
                            : current.filter((x) => x !== item.id),
                        )
                      }
                    />
                    <img
                      className="c7x-admin-avatar"
                      src="/ui-illustrations/bot-agent-account.svg"
                      alt=""
                    />
                    <button
                      type="button"
                      className={accountId === item.id ? "is-selected" : ""}
                      onClick={() => chooseAccount(item.id)}
                    >
                      <strong>{item.username}</strong>
                      <small>
                        {item.realm ?? t("botAgents.noRealm")} · {item.activity}
                      </small>
                    </button>
                    <span
                      className={item.state === "ONLINE" ? "c7x-admin-online" : "c7x-admin-muted"}
                    >
                      {item.state}
                    </span>
                  </div>
                ))}
              </div>

              <section className="c7x-admin-card c7x-admin-pools">
                <div className="c7x-admin-section-head">
                  <h3>{t("botAgents.proxyPoolsCount", { count: proxyPools.length })}</h3>
                  <button type="button" onClick={() => setPoolEditor((value) => !value)}>
                    {poolEditor ? t("botAgents.close") : t("botAgents.configure")}
                  </button>
                </div>
                {proxyPools.map((pool) => (
                  <div className="c7x-admin-pool" key={pool.name}>
                    <span>
                      <strong>{pool.name}</strong>
                      <small>
                        {t("botAgents.poolSummary", {
                          strategy: pool.strategy,
                          count: pool.count,
                          healthy: pool.entries.filter((entry) => entry.healthy).length,
                        })}
                      </small>
                    </span>
                    <button
                      disabled={busy}
                      onClick={() => void proxyOperation({ action: "test", name: pool.name })}
                    >
                      {t("botAgents.test")}
                    </button>
                    {chosen.length > 0 ? (
                      <button
                        disabled={busy}
                        onClick={() => void bulk("bulkPool", { poolName: pool.name })}
                      >
                        {t("botAgents.assignSelected", { count: chosen.length })}
                      </button>
                    ) : null}
                    <button
                      disabled={busy}
                      onClick={() => {
                        if (window.confirm(t("botAgents.removePoolConfirm", { name: pool.name }))) {
                          void proxyOperation({ action: "delete", name: pool.name })
                        }
                      }}
                    >
                      ×
                    </button>
                  </div>
                ))}
                {poolEditor ? (
                  <form
                    className="c7x-admin-form"
                    onSubmit={(event) => {
                      event.preventDefault()
                      void proxyOperation({
                        action: "upsert",
                        name: poolName.trim(),
                        strategy: poolStrategy,
                        proxies: poolProxies,
                      })
                    }}
                  >
                    <label>
                      {t("botAgents.poolName")}
                      <input
                        value={poolName}
                        maxLength={64}
                        onChange={(event) => setPoolName(event.target.value)}
                        required
                      />
                    </label>
                    <label>
                      {t("botAgents.strategy")}
                      <select
                        value={poolStrategy}
                        onChange={(event) => setPoolStrategy(event.target.value)}
                      >
                        <option value="STICKY">{t("botAgents.sticky")}</option>
                        <option value="ROUND_ROBIN">{t("botAgents.roundRobin")}</option>
                        <option value="LEAST_USED">{t("botAgents.leastUsed")}</option>
                      </select>
                    </label>
                    <label>
                      {t("botAgents.proxiesPerLine")}
                      <textarea
                        rows={4}
                        value={poolProxies}
                        placeholder="socks5://user:password@host:port"
                        onChange={(event) => setPoolProxies(event.target.value)}
                      />
                    </label>
                    <button type="submit" disabled={busy || !poolName.trim()}>
                      {t("botAgents.savePool")}
                    </button>
                  </form>
                ) : null}
              </section>
              {account ? (
                <section className="c7x-admin-card c7x-admin-detail">
                  <div className="c7x-admin-section-head">
                    <h2>{account.username}</h2>
                    <span>{account.location ?? "—"}</span>
                  </div>
                  {account.lastError ? (
                    <p className="c7x-admin-error">{account.lastError}</p>
                  ) : null}
                  <div className="c7x-admin-actions">
                    {(
                      [
                        "connect",
                        "disconnect",
                        "reconnect",
                        "enterSao",
                        "dungeonLobby",
                        "pause",
                        "resume",
                      ] as const
                    ).map((action) => (
                      <button
                        type="button"
                        key={action}
                        disabled={busy}
                        onClick={() => void act(action)}
                      >
                        {
                          {
                            connect: t("botAgents.start"),
                            disconnect: t("botAgents.stopShort"),
                            reconnect: t("botAgents.reconnect"),
                            enterSao: t("botAgents.enterSao"),
                            dungeonLobby: t("botAgents.dungeonLobby"),
                            pause: t("botAgents.pause"),
                            resume: t("botAgents.resume"),
                          }[action]
                        }
                      </button>
                    ))}
                  </div>
                  <div className="c7x-admin-form-inline">
                    <label>
                      {t("botAgents.accountGroup")}
                      <select
                        value={groupState.state.assignments[account.id] ?? ""}
                        onChange={(event) => void assignGroup(account.id, event.target.value)}
                        disabled={busy}
                      >
                        <option value="">{t("botAgents.noGroup")}</option>
                        {groupState.state.groups.map((group) => (
                          <option key={group.id} value={group.id}>
                            {group.name}
                          </option>
                        ))}
                      </select>
                    </label>
                  </div>
                  <div className="c7x-admin-actions">
                    {DUNGEONS.map((dungeon) => (
                      <button
                        key={dungeon}
                        type="button"
                        disabled={busy}
                        onClick={() => void act("dungeon", { dungeon })}
                      >
                        {t("botAgents.enterDungeon", { dungeon })}
                      </button>
                    ))}
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => {
                        if (!selected || !window.confirm(t("botAgents.removeAccountConfirm"))) {
                          return
                        }
                        const id = account.id
                        void run(async () => {
                          await workerRequest(selected, "POST", "/api/accounts/manage", {
                            action: "remove",
                            id,
                          })
                          setAccountId(null)
                        })
                      }}
                    >
                      {t("botAgents.deleteAccount")}
                    </button>
                  </div>
                  <h3>{t("botAgents.automation")}</h3>
                  <div className="c7x-admin-toggle-row">
                    {(
                      [
                        ["autoDungeon", t("botAgents.autoDungeons")],
                        ["swapper", t("botAgents.autoSwapper")],
                        ["autoReconnect", t("botAgents.autoReconnect")],
                      ] as const
                    ).map(([key, label]) => (
                      <label key={key}>
                        <input
                          type="checkbox"
                          checked={account[key]}
                          disabled={busy}
                          onChange={(e) => void settings({ [key]: e.target.checked })}
                        />{" "}
                        {label}
                      </label>
                    ))}
                  </div>
                  <div className="c7x-admin-toggle-row">
                    {DUNGEONS.map((dungeon) => (
                      <label key={dungeon}>
                        <input
                          type="checkbox"
                          disabled={busy}
                          checked={account.dungeons.includes(dungeon)}
                          onChange={(e) =>
                            void settings({
                              dungeons: e.target.checked
                                ? [...account.dungeons, dungeon]
                                : account.dungeons.filter((x) => x !== dungeon),
                            })
                          }
                        />{" "}
                        {dungeon}
                      </label>
                    ))}
                  </div>
                  <div className="c7x-admin-form-inline">
                    <label>
                      {t("botAgents.realm")}
                      <input
                        type="number"
                        min="1"
                        value={realm}
                        onChange={(e) => setRealm(e.target.value)}
                      />
                    </label>
                    <button
                      type="button"
                      disabled={busy || !Number(realm)}
                      onClick={() => void act("changeRealm", { realm: Number(realm) })}
                    >
                      {t("botAgents.go")}
                    </button>
                    <button
                      type="button"
                      disabled={busy || !Number(realm)}
                      onClick={() => void settings({ realm: Number(realm) })}
                    >
                      {t("botAgents.swapperTarget")}
                    </button>
                  </div>
                  <h3>{t("botAgents.navigation")}</h3>
                  <div className="c7x-admin-form-inline">
                    {(["x", "y", "z"] as const).map((key) => (
                      <label key={key}>
                        {key.toUpperCase()}
                        <input
                          type="number"
                          value={navigation[key]}
                          onChange={(e) => setNavigation((s) => ({ ...s, [key]: e.target.value }))}
                        />
                      </label>
                    ))}
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() =>
                        void act("navigate", {
                          x: Number(navigation.x),
                          y: Number(navigation.y),
                          z: Number(navigation.z),
                          reach: 1,
                        })
                      }
                    >
                      {t("botAgents.walk")}
                    </button>
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => void act("stopNavigation")}
                    >
                      {t("botAgents.stopShort")}
                    </button>
                  </div>
                  <h3>{t("botAgents.chat")}</h3>
                  <form
                    className="c7x-admin-form-inline"
                    onSubmit={(e) => {
                      e.preventDefault()
                      if (!selected || !chatText.trim()) {
                        return
                      }
                      void run(() => botChat(selected, account.id, chatText.trim()), false)
                      setChatText("")
                    }}
                  >
                    <input
                      value={chatText}
                      maxLength={256}
                      onChange={(e) => setChatText(e.target.value)}
                      placeholder={t("botAgents.messagePlaceholder")}
                    />
                    <button disabled={busy || !chatText.trim()} type="submit">
                      {t("botAgents.send")}
                    </button>
                  </form>
                  <h3>{t("botAgents.networkSpoof")}</h3>
                  {network ? (
                    <div className="c7x-admin-network">
                      <div className="c7x-admin-form-inline">
                        <label>
                          {t("botAgents.connectionType")}
                          <select
                            value={networkMode}
                            disabled={busy}
                            onChange={(event) => {
                              const mode = event.target.value
                              setNetworkMode(mode)
                              if (mode === "CUSTOM" || (mode === "POOL" && !proxyPools.length)) {
                                return
                              }
                              void applyNetwork({
                                proxy:
                                  mode === "POOL"
                                    ? { mode, poolName: proxyPools[0].name }
                                    : { mode },
                              })
                            }}
                          >
                            <option value="INHERIT">{t("botAgents.inherit")}</option>
                            <option value="DIRECT">{t("botAgents.directMode")}</option>
                            <option value="POOL" disabled={!proxyPools.length}>
                              {t("botAgents.proxyPool")}
                            </option>
                            <option value="CUSTOM">{t("botAgents.customProxy")}</option>
                          </select>
                        </label>
                        {network.proxy.mode === "POOL" ? (
                          <label>
                            {t("botAgents.pool")}
                            <select
                              value={network.proxy.poolName ?? ""}
                              disabled={busy}
                              onChange={(event) =>
                                void applyNetwork({
                                  proxy: { mode: "POOL", poolName: event.target.value },
                                })
                              }
                            >
                              {proxyPools.map((pool) => (
                                <option key={pool.name} value={pool.name}>
                                  {pool.name}
                                </option>
                              ))}
                            </select>
                          </label>
                        ) : null}
                      </div>
                      {networkMode === "CUSTOM" ? (
                        <form
                          className="c7x-admin-form"
                          onSubmit={(event) => {
                            event.preventDefault()
                            void applyNetwork({
                              proxy: {
                                mode: "CUSTOM",
                                type: proxyType,
                                host: proxyHost,
                                port: Number(proxyPort),
                                username: proxyUsername,
                                ...(proxyPassword ? { password: proxyPassword } : {}),
                              },
                            })
                          }}
                        >
                          <div className="c7x-admin-form-inline">
                            <label>
                              {t("botAgents.protocol")}
                              <select
                                value={proxyType}
                                onChange={(event) => setProxyType(event.target.value)}
                              >
                                <option value="SOCKS5">SOCKS5</option>
                                <option value="SOCKS4">SOCKS4</option>
                                <option value="HTTP">HTTP</option>
                                <option value="HTTPS">HTTPS</option>
                              </select>
                            </label>
                            <label>
                              {t("botAgents.host")}
                              <input
                                value={proxyHost}
                                onChange={(event) => setProxyHost(event.target.value)}
                                required
                              />
                            </label>
                            <label>
                              {t("botAgents.port")}
                              <input
                                type="number"
                                min="1"
                                max="65535"
                                value={proxyPort}
                                onChange={(event) => setProxyPort(event.target.value)}
                                required
                              />
                            </label>
                          </div>
                          <div className="c7x-admin-form-inline">
                            <label>
                              {t("botAgents.username")}
                              <input
                                value={proxyUsername}
                                onChange={(event) => setProxyUsername(event.target.value)}
                              />
                            </label>
                            <label>
                              {t("botAgents.newPassword")}
                              <input
                                type="password"
                                value={proxyPassword}
                                onChange={(event) => setProxyPassword(event.target.value)}
                              />
                            </label>
                          </div>
                          <button type="submit" disabled={busy || !proxyHost.trim()}>
                            {t("botAgents.saveProxy")}
                          </button>
                        </form>
                      ) : null}
                      <div className="c7x-admin-form-inline">
                        <label>
                          {t("botAgents.spoofProfile")}
                          <select
                            value={network.spoof.mode}
                            disabled={busy}
                            onChange={(event) =>
                              void applyNetwork({ spoof: { mode: event.target.value } })
                            }
                          >
                            <option value="INHERIT">{t("botAgents.inherit")}</option>
                            <option value="DISABLED">{t("botAgents.disabled")}</option>
                            <option value="PERSISTENT">{t("botAgents.persistent")}</option>
                            <option value="SESSION_RANDOM">{t("botAgents.perSession")}</option>
                          </select>
                        </label>
                        <button
                          type="button"
                          disabled={busy}
                          onClick={() =>
                            void applyNetwork({
                              spoof: {
                                mode: "PERSISTENT",
                                profileName: network.spoof.profileName ?? "Natural Desktop",
                                regenerate: true,
                              },
                            })
                          }
                        >
                          {t("botAgents.newSpoofSingle")}
                        </button>
                        <button
                          type="button"
                          disabled={busy}
                          onClick={() => {
                            if (!selected || !account) {
                              return
                            }
                            void run(
                              () =>
                                workerRequest(
                                  selected,
                                  "POST",
                                  `/api/account/${account.id}/proxy-test`,
                                  {},
                                ),
                              false,
                            )
                          }}
                        >
                          {t("botAgents.testProxy")}
                        </button>
                      </div>
                    </div>
                  ) : null}
                  <h3>{t("botAgents.diagnostics")}</h3>
                  <p className="c7x-admin-muted">
                    {t("botAgents.proxyStatus", {
                      proxy: account.proxy,
                      status: account.proxyStatus,
                    })}
                  </p>
                  <button
                    type="button"
                    disabled={busy}
                    onClick={() => {
                      if (!selected) {
                        return
                      }
                      void run(async () => {
                        setDiagnostics(
                          await workerRequest(
                            selected,
                            "GET",
                            `/api/account/${account.id}/diagnostics`,
                          ),
                        )
                        setShowDiagnostics(true)
                      }, false)
                    }}
                  >
                    {t("botAgents.getDiagnostics")}
                  </button>
                  {showDiagnostics ? (
                    <pre className="c7x-admin-log">{JSON.stringify(diagnostics, null, 2)}</pre>
                  ) : null}
                  <div className="c7x-admin-section-head">
                    <h3>{t("botAgents.journal")}</h3>
                    <button
                      type="button"
                      onClick={() =>
                        void navigator.clipboard.writeText(
                          logEntries
                            .map((x) => `[${x.timestamp}] ${x.category} ${x.message}`)
                            .join("\n"),
                        )
                      }
                    >
                      {t("botAgents.copy")}
                    </button>
                  </div>
                  <div className="c7x-admin-log">
                    {logEntries.length
                      ? logEntries.map((entry) => (
                          <div key={entry.sequence}>
                            <small>
                              {entry.timestamp} · {entry.category}
                            </small>
                            <span>{entry.message}</span>
                          </div>
                        ))
                      : t("botAgents.noLogs")}
                  </div>
                </section>
              ) : null}
            </>
          )}
        </section>
      ) : null}
      {notice ? (
        <p className="c7x-admin-message" role="status">
          {notice}
        </p>
      ) : null}
    </div>
  )
}
