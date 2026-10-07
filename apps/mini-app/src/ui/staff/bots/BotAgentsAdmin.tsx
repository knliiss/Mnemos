import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { useMnemosData } from "@/ui/data/MnemosDataProvider"
import {
  botAccounts, botAction, botChat, botLogs, botManager, botSettings, createBotAgent,
  getBotAgents, renameBotAgent, revokeBotAgent, workerRequest,
  getBotGroups, saveBotGroups, getBotProxyPools, modifyBotProxyPool,
  getBotNetwork, updateBotNetwork,
  type RemoteGroupState, type ProxyPool, type AccountNetwork,
  type AgentCreation, type BotAccount, type BotAgent,
} from "@/shared/api/botAgentApi"
import { handleError } from "@/shared/errors/errorHandler"
import { PageHeader } from "../../shared/Primitives"
import "./bot-agents.css"

const DUNGEONS = ["LIGHT", "MEDIUM", "HARD", "EXPERT"] as const

export function BotAgentsAdmin({ back }: { back: () => void }) {
  const { user } = useMnemosData()
  const authorized = user.systemRoles.some((role) => role === "ADMIN" || role === "SUPER_ADMIN")
  const [agents, setAgents] = useState<BotAgent[]>([])
  const [selected, setSelected] = useState<string | null>(null)
  const [created, setCreated] = useState<AgentCreation | null>(null)
  const [accounts, setAccounts] = useState<BotAccount[]>([])
  const [accountId, setAccountId] = useState<string | null>(null)
  const [chosen, setChosen] = useState<string[]>([])
  const logCursor = useRef(0)
  const [logEntries, setLogEntries] = useState<{ sequence: number; timestamp: string; category: string; message: string }[]>([])
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
  const [groupState, setGroupState] = useState<RemoteGroupState>({ state: { groups: [], assignments: {} }, revision: 0 })
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
  const [showDiagnostics, setShowDiagnostics] = useState(false)
  const [diagnostics, setDiagnostics] = useState<unknown>(null)

  const active = agents.find((agent) => agent.id === selected)
  const account = accounts.find((entry) => entry.id === accountId)
  const visibleAccounts = useMemo(
    () => accounts.filter((entry) => entry.username.toLowerCase().includes(query.toLowerCase()) &&
      (!groupFilter || groupState.state.assignments[entry.id] === groupFilter)),
    [accounts, query, groupFilter, groupState],
  )
  const formatDate = (date: string | null) =>
    date ? new Date(date).toLocaleString() : "Ніколи"

  const refreshAgents = useCallback(async () => {
    if (!authorized) return
    try { setAgents(await getBotAgents()) }
    catch (error) { handleError(error) }
  }, [authorized])

  const refreshAccounts = useCallback(async (id: string) => {
    try { setAccounts(await botAccounts(id)) }
    catch (error) { handleError(error) }
  }, [])

  useEffect(() => {
    if (!authorized) return
    void refreshAgents()
    const timer = window.setInterval(() => void refreshAgents(), 10_000)
    return () => window.clearInterval(timer)
  }, [authorized, refreshAgents])

  useEffect(() => {
    setAccounts([])
    setAccountId(null)
    setLogEntries([])
    setChosen([])
    setProxyPools([])
    setGroupFilter("")
    if (!selected) return
    void getBotGroups(selected).then(setGroupState).catch(handleError)
    if (!active?.online) return
    void getBotProxyPools(selected).then((value) => setProxyPools(value.pools)).catch(handleError)
    void refreshAccounts(selected)
    const timer = window.setInterval(() => void refreshAccounts(selected), 4_000)
    return () => window.clearInterval(timer)
  }, [selected, active?.online, refreshAccounts])

  useEffect(() => {
    logCursor.current = 0
    setLogEntries([])
    if (!selected || !accountId || !active?.online) return
    let running = true
    let inFlight = false
    const poll = async () => {
      if (inFlight || !running) return
      inFlight = true
      try {
        let result = await botLogs(selected, accountId, logCursor.current)
        if (logCursor.current === 0 && result.lastSequence > 100) {
          result = await botLogs(selected, accountId, Math.max(0, result.lastSequence - 80))
        }
        if (!running) return
        if (result.entries.length) {
          logCursor.current = result.entries[result.entries.length - 1].sequence
          setLogEntries((previous) => [...previous, ...result.entries].slice(-150))
        }
      } catch {
        // A temporary worker disconnect should not clear the visible log.
      } finally {
        inFlight = false
      }
    }
    void poll()
    const timer = window.setInterval(() => void poll(), 2500)
    return () => { running = false; window.clearInterval(timer) }
  }, [selected, accountId, active?.online])

  useEffect(() => {
    setNetwork(null)
    if (!selected || !accountId || !active?.online) return
    let cancelled = false
    void getBotNetwork(selected, accountId).then((value) => {
      if (cancelled) return
      setNetwork(value)
      setProxyHost(value.proxy.host ?? "")
      setProxyPort(String(value.proxy.port ?? 1080))
      setProxyUsername(value.proxy.username ?? "")
      setProxyType(value.proxy.type ?? "SOCKS5")
      setProxyPassword("")
    }).catch(handleError)
    return () => { cancelled = true }
  }, [selected, accountId, active?.online])

  async function run(task: () => Promise<unknown>, refresh = true) {
    setBusy(true)
    setNotice("")
    try {
      await task()
      setNotice("Виконано")
      if (selected && refresh) await refreshAccounts(selected)
    } catch (error) {
      setNotice(error instanceof Error ? error.message : "Помилка виконання")
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
    } catch (error) { handleError(error) }
    finally { setBusy(false) }
  }

  async function renameWorker(agent: BotAgent) {
    const name = window.prompt("Назва програми", agent.name)?.trim()
    if (!name || name === agent.name) return
    await run(async () => { await renameBotAgent(agent.id, name); await refreshAgents() }, false)
  }

  async function removeWorker(agent: BotAgent) {
    if (!window.confirm(`Відкликати доступ програми «${agent.name}»? Запис залишиться в історії.`)) return
    await run(async () => { await revokeBotAgent(agent.id); await refreshAgents() }, false)
  }

  async function act(action: string, extra: Record<string, unknown> = {}) {
    if (!selected || !account) return
    await run(() => botAction(selected, account.id, action, extra))
  }

  async function settings(patch: Record<string, unknown>) {
    if (!selected || !account) return
    await run(() => botSettings(selected, account.id, {
      autoDungeon: account.autoDungeon,
      swapper: account.swapper,
      autoReconnect: account.autoReconnect,
      dungeons: account.dungeons,
      realm: account.targetRealm,
      ...patch,
    }))
  }

  async function bulk(action: string, extra: Record<string, unknown> = {}) {
    if (!selected) return
    await run(() => botManager(selected, { action, ids: chosen, ...extra }))
  }

  async function addAccount() {
    if (!selected || !loginName || !loginPassword) return
    await run(async () => {
      await workerRequest(selected, "POST", "/api/accounts/manage", {
        action: "login", username: loginName, password: loginPassword,
        otpCode: loginOtp, connect: false,
      })
      setLogin(false); setLoginPassword(""); setLoginOtp(""); setLoginName("")
    })
  }

  async function updateGroups(state: RemoteGroupState["state"]) {
    if (!selected) return
    await run(async () => {
      const next = await saveBotGroups(selected, { state, revision: groupState.revision })
      setGroupState(next)
    }, false)
  }

  async function addGroup() {
    const name = groupName.trim()
    if (!name || name.length > 64 || !selected) return
    const id = crypto.randomUUID()
    await updateGroups({
      groups: [...groupState.state.groups, { id, name }],
      assignments: groupState.state.assignments,
    })
    setGroupName("")
  }

  async function deleteGroup(id: string) {
    if (!window.confirm("Видалити групу? Акаунти залишаться у програмі.")) return
    const assignments = Object.fromEntries(
      Object.entries(groupState.state.assignments).filter(([, groupId]) => groupId !== id),
    )
    await updateGroups({
      groups: groupState.state.groups.filter((group) => group.id !== id),
      assignments,
    })
    if (groupFilter === id) setGroupFilter("")
  }

  async function assignGroup(accountId: string, id: string) {
    const assignments = { ...groupState.state.assignments }
    if (id) assignments[accountId] = id
    else delete assignments[accountId]
    await updateGroups({ ...groupState.state, assignments })
  }

  async function applyNetwork(changes: Record<string, unknown>) {
    if (!selected || !accountId) return
    await run(async () => {
      const next = await updateBotNetwork(selected, accountId, changes)
      setNetwork(next)
    })
  }

  async function proxyOperation(data: Record<string, unknown>) {
    if (!selected) return
    await run(async () => {
      const result = await modifyBotProxyPool(selected, data)
      setProxyPools(result.pools)
    }, false)
  }

  if (!authorized) return <div className="screen"><PageHeader title="Боти" back={back} /><p>Доступ лише для адміністратора.</p></div>

  return (
    <div className="screen c7x-admin">
      <PageHeader title="C7x · Боти" back={back} actions={
        <button type="button" className="c7x-admin-primary" onClick={() => void addWorker()} disabled={busy}>
          + Додати програму
        </button>
      } />
      <section className="c7x-admin-hero">
        <div className="c7x-admin-hero-copy"><span className="c7x-admin-eyebrow">C7X · REMOTE CONTROL</span><strong>Центр керування ботами</strong><p>Акаунти працюють на окремих ПК, а керування доступне тут, у Mnemos.</p></div><img className="c7x-admin-hero-art" src="/ui-illustrations/bot-agent-hero.svg" alt="" />
        <div className="c7x-admin-total"><b>{agents.filter((a) => a.online).length}</b><span>онлайн / {agents.length}</span></div>
      </section>

      {created ? (
        <section className="c7x-admin-card c7x-admin-pair">
          <strong>Ключ підключення програми</strong>
          <p>Відкрий C7xBotManager на потрібному ПК, обери «Mnemos» і встав цей ключ. Він показується лише зараз.</p>
          <code>{created.accessKey}</code>
          <div className="c7x-admin-actions">
            <button type="button" onClick={() => void navigator.clipboard.writeText(created.accessKey)}>Скопіювати ключ</button>
            <button type="button" onClick={() => setCreated(null)}>Закрити</button>
          </div>
        </section>
      ) : null}

      <div className="c7x-admin-section-head"><h2>Програми</h2><button type="button" onClick={() => void refreshAgents()}>Оновити</button></div>
      {agents.length === 0 ? <div className="c7x-admin-empty"><img src="/ui-illustrations/bot-agent-offline.svg" alt="" /><strong>Програм поки немає</strong><p>Створи підключення та встав ключ у C7xBotManager.</p></div> : null}
      <div className="c7x-admin-agent-list">
        {agents.map((agent) => (
          <div className={`c7x-admin-card c7x-admin-agent ${selected === agent.id ? "is-selected" : ""}`} key={agent.id}>
            <button type="button" className="c7x-admin-agent-main" onClick={() => setSelected(agent.id)}>
              <span className="c7x-admin-worker-art"><img src="/ui-illustrations/bot-agent-workspace.svg" alt="" /></span>
              <span><strong>{agent.name}</strong><small><i className={`c7x-admin-dot ${agent.online ? "is-online" : ""}`} /> {agent.machineName ?? "ПК не визначений"} · {agent.revoked ? "Доступ відкликано" : agent.online ? "Працює" : "Вимкнено"}</small></span>
              <span className="c7x-admin-last">{agent.online ? "Онлайн" : `Був: ${formatDate(agent.lastSeenAt)}`}</span>
            </button>
            <div className="c7x-admin-agent-actions">
              <button type="button" onClick={() => void renameWorker(agent)} disabled={busy || agent.revoked}>Назва</button>
              {!agent.revoked ? <button type="button" onClick={() => void removeWorker(agent)} disabled={busy}>Відкликати</button> : null}
            </div>
          </div>
        ))}
      </div>

      {active ? (
        <section className="c7x-admin-workspace">
          <div className="c7x-admin-section-head"><h2>{active.name}</h2><span className="c7x-admin-muted">{active.osUsername ?? "—"} · {active.fingerprint?.slice(0, 12) ?? "Без відбитка"}</span></div>
          {!active.online ? (
            <div className="c7x-admin-card c7x-admin-offline-card"><img src="/ui-illustrations/bot-agent-offline.svg" alt="" /><strong>Програма неактивна</strong><p>Востаннє на зв’язку: {formatDate(active.lastSeenAt)}. Запис зберігається, коли ПК вимкнений.</p></div>
          ) : (
            <>
              <div className="c7x-admin-section-head">
                <h2>Акаунти · {accounts.length}</h2>
                <button type="button" onClick={() => setLogin((x) => !x)}>+ Додати акаунт</button>
              </div>
              {login ? (
                <form className="c7x-admin-card c7x-admin-form" onSubmit={(e) => { e.preventDefault(); void addAccount() }}>
                  <label>Логін<input autoComplete="username" value={loginName} onChange={(e) => setLoginName(e.target.value)} required /></label>
                  <label>Пароль<input type="password" autoComplete="new-password" value={loginPassword} onChange={(e) => setLoginPassword(e.target.value)} required /></label>
                  <label>OTP (за потреби)<input value={loginOtp} onChange={(e) => setLoginOtp(e.target.value)} /></label>
                  <button type="submit" className="c7x-admin-primary" disabled={busy}>Додати на ПК</button>
                </form>
              ) : null}
              <section className="c7x-admin-card c7x-admin-group-panel">
                <div className="c7x-admin-section-head"><h3>Групи акаунтів</h3><span>Зберігаються у Mnemos</span></div>
                <form className="c7x-admin-form-inline" onSubmit={(event) => { event.preventDefault(); void addGroup() }}>
                  <input value={groupName} maxLength={64} placeholder="Нова група…" onChange={(event) => setGroupName(event.target.value)} />
                  <button type="submit" disabled={busy || !groupName.trim()}>Створити</button>
                </form>
                <div className="c7x-admin-group-tags">
                  <button type="button" className={!groupFilter ? "is-active" : ""} onClick={() => setGroupFilter("")}>Усі</button>
                  {groupState.state.groups.map((group) => (
                    <div className="c7x-admin-group-tag" key={group.id}>
                      <button type="button" className={groupFilter === group.id ? "is-active" : ""} onClick={() => setGroupFilter(group.id)}>{group.name}</button>
                      <button type="button" aria-label={"Видалити групу " + group.name} onClick={() => void deleteGroup(group.id)} disabled={busy}>×</button>
                    </div>
                  ))}
                </div>
              </section>
              <input className="c7x-admin-search" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Пошук за ніком…" />
              <div className="c7x-admin-actions">
                <button type="button" onClick={() => void bulk("connectAll")} disabled={busy}>Запустити всіх</button>
                <button type="button" onClick={() => void bulk("disconnectAll")} disabled={busy}>Зупинити всіх</button>
                {chosen.length > 0 ? <>
                  <button type="button" onClick={() => void bulk("bulkConnect")} disabled={busy}>Запустити вибраних ({chosen.length})</button>
                  <button type="button" onClick={() => void bulk("bulkReconnect")} disabled={busy}>Реконект</button>
                  <button type="button" onClick={() => void bulk("bulkDisconnect")} disabled={busy}>Зупинити</button>
                  <button type="button" onClick={() => void bulk("bulkEnterSao")} disabled={busy}>На SAO</button>
                  <button type="button" onClick={() => void bulk("bulkDungeonLobby")} disabled={busy}>Лобі данжів</button>
                  <button type="button" onClick={() => void bulk("bulkAutomation", { autoDungeon: true })} disabled={busy}>Автоданжі ✓</button>
                  <button type="button" onClick={() => void bulk("bulkAutomation", { autoDungeon: false })} disabled={busy}>Автоданжі ✕</button>
                  <button type="button" onClick={() => void bulk("bulkDirect")} disabled={busy}>Без проксі</button>
                  <button type="button" onClick={() => void bulk("bulkSpoofGenerate")} disabled={busy}>Нові Spoof</button>
                  <button type="button" onClick={() => void bulk("bulkStopNavigation")} disabled={busy}>Стоп навігації</button>
                </> : null}
              </div>
              {chosen.length > 0 ? (
                <section className="c7x-admin-card c7x-admin-bulk">
                  <div className="c7x-admin-section-head"><h3>Масові команди · {chosen.length} акаунтів</h3></div>
                  <div className="c7x-admin-actions">
                    {DUNGEONS.map((dungeon) => <button key={dungeon} type="button" disabled={busy}
                      onClick={() => void bulk("bulkDungeon", { dungeon })}>{dungeon}</button>)}
                  </div>
                  <form className="c7x-admin-form-inline" onSubmit={(event) => {
                    event.preventDefault()
                    if (!bulkChatText.trim()) return
                    void bulk("bulkChat", { message: bulkChatText.trim() })
                  }}>
                    <input value={bulkChatText} onChange={(event) => setBulkChatText(event.target.value)} maxLength={256} placeholder="Чат або команда для вибраних…" />
                    <button type="submit" disabled={busy || !bulkChatText.trim()}>Надіслати</button>
                  </form>
                </section>
              ) : null}
              <div className="c7x-admin-accounts">
                {visibleAccounts.length === 0 ? <div className="c7x-admin-empty"><img src="/ui-illustrations/bot-agent-account.svg" alt="" /><strong>Акаунтів не знайдено</strong></div> : null}
                {visibleAccounts.map((item) => (
                  <div key={item.id} className="c7x-admin-account-line">
                    <input type="checkbox" aria-label={`Вибрати ${item.username}`} checked={chosen.includes(item.id)}
                      onChange={(e) => setChosen((current) => e.target.checked ? [...current, item.id] : current.filter((x) => x !== item.id))} />
                    <img className="c7x-admin-avatar" src="/ui-illustrations/bot-agent-account.svg" alt="" /><button type="button" className={accountId === item.id ? "is-selected" : ""} onClick={() => setAccountId(item.id)}>
                      <strong>{item.username}</strong><small>{item.realm ?? "Без реалму"} · {item.activity}</small>
                    </button>
                    <span className={item.state === "ONLINE" ? "c7x-admin-online" : "c7x-admin-muted"}>{item.state}</span>
                  </div>
                ))}
              </div>

              <section className="c7x-admin-card c7x-admin-pools">
                <div className="c7x-admin-section-head">
                  <h3>Пули проксі · {proxyPools.length}</h3>
                  <button type="button" onClick={() => setPoolEditor((value) => !value)}>{poolEditor ? "Закрити" : "+ Налаштувати"}</button>
                </div>
                {proxyPools.map((pool) => (
                  <div className="c7x-admin-pool" key={pool.name}>
                    <span><strong>{pool.name}</strong><small>{pool.strategy} · {pool.count} проксі · {pool.entries.filter((entry) => entry.healthy).length} здорових</small></span>
                    <button disabled={busy} onClick={() => void proxyOperation({ action: "test", name: pool.name })}>Тест</button>
                    {chosen.length > 0 ? <button disabled={busy} onClick={() => void bulk("bulkPool", { poolName: pool.name })}>Призначити {chosen.length}</button> : null}
                    <button disabled={busy} onClick={() => {
                      if (window.confirm("Видалити пул " + pool.name + "?")) void proxyOperation({ action: "delete", name: pool.name })
                    }}>×</button>
                  </div>
                ))}
                {poolEditor ? (
                  <form className="c7x-admin-form" onSubmit={(event) => {
                    event.preventDefault()
                    void proxyOperation({ action: "upsert", name: poolName.trim(), strategy: poolStrategy, proxies: poolProxies })
                  }}>
                    <label>Назва пулу<input value={poolName} maxLength={64} onChange={(event) => setPoolName(event.target.value)} required /></label>
                    <label>Стратегія
                      <select value={poolStrategy} onChange={(event) => setPoolStrategy(event.target.value)}>
                        <option value="STICKY">Постійний проксі</option>
                        <option value="ROUND_ROBIN">По черзі</option>
                        <option value="LEAST_USED">Найменш зайнятий</option>
                      </select>
                    </label>
                    <label>Проксі (по одному в рядку)
                      <textarea rows={4} value={poolProxies} placeholder="socks5://user:password@host:port" onChange={(event) => setPoolProxies(event.target.value)} />
                    </label>
                    <button type="submit" disabled={busy || !poolName.trim()}>Зберегти пул на ПК</button>
                  </form>
                ) : null}
              </section>
              {account ? (
                <section className="c7x-admin-card c7x-admin-detail">
                  <div className="c7x-admin-section-head"><h2>{account.username}</h2><span>{account.location ?? "—"}</span></div>
                  {account.lastError ? <p className="c7x-admin-error">{account.lastError}</p> : null}
                  <div className="c7x-admin-actions">
                    {(["connect", "disconnect", "reconnect", "enterSao", "dungeonLobby", "pause", "resume"] as const).map((action) =>
                      <button type="button" key={action} disabled={busy} onClick={() => void act(action)}>{{
                        connect: "Запуск", disconnect: "Стоп", reconnect: "Реконект", enterSao: "На SAO",
                        dungeonLobby: "Лобі данжів", pause: "Пауза", resume: "Продовжити",
                      }[action]}</button>)}
                  </div>
                  <div className="c7x-admin-form-inline">
                    <label>Група акаунта
                      <select value={groupState.state.assignments[account.id] ?? ""}
                        onChange={(event) => void assignGroup(account.id, event.target.value)} disabled={busy}>
                        <option value="">Без групи</option>
                        {groupState.state.groups.map((group) => <option key={group.id} value={group.id}>{group.name}</option>)}
                      </select>
                    </label>
                  </div>
                  <h3>Автоматизація</h3>
                  <div className="c7x-admin-toggle-row">
                    {([["autoDungeon", "Автоданжі"], ["swapper", "Автосвапер"], ["autoReconnect", "Автореконект"]] as const).map(([key, label]) => (
                      <label key={key}><input type="checkbox" checked={account[key]} disabled={busy}
                        onChange={(e) => void settings({ [key]: e.target.checked })} /> {label}</label>
                    ))}
                  </div>
                  <div className="c7x-admin-toggle-row">
                    {DUNGEONS.map((dungeon) => (
                      <label key={dungeon}><input type="checkbox" disabled={busy} checked={account.dungeons.includes(dungeon)}
                        onChange={(e) => void settings({ dungeons: e.target.checked ?
                          [...account.dungeons, dungeon] : account.dungeons.filter((x) => x !== dungeon) })} /> {dungeon}</label>
                    ))}
                  </div>
                  <div className="c7x-admin-form-inline">
                    <label>Реалм<input type="number" min="1" value={realm} onChange={(e) => setRealm(e.target.value)} /></label>
                    <button type="button" disabled={busy || !Number(realm)} onClick={() => void act("changeRealm", { realm: Number(realm) })}>Перейти</button>
                    <button type="button" disabled={busy || !Number(realm)} onClick={() => void settings({ realm: Number(realm) })}>Ціль свапера</button>
                  </div>
                  <h3>Навігація</h3>
                  <div className="c7x-admin-form-inline">
                    {(["x", "y", "z"] as const).map((key) => (
                      <label key={key}>{key.toUpperCase()}<input type="number" value={navigation[key]} onChange={(e) => setNavigation((s) => ({ ...s, [key]: e.target.value }))} /></label>
                    ))}
                    <button type="button" disabled={busy} onClick={() => void act("navigate", {
                      x: Number(navigation.x), y: Number(navigation.y), z: Number(navigation.z), reach: 1,
                    })}>Йти</button>
                    <button type="button" disabled={busy} onClick={() => void act("stopNavigation")}>Стоп</button>
                  </div>
                  <h3>Чат</h3>
                  <form className="c7x-admin-form-inline" onSubmit={(e) => {
                    e.preventDefault()
                    if (!selected || !chatText.trim()) return
                    void run(() => botChat(selected, account.id, chatText.trim()), false)
                    setChatText("")
                  }}>
                    <input value={chatText} maxLength={256} onChange={(e) => setChatText(e.target.value)} placeholder="Написати повідомлення…" />
                    <button disabled={busy || !chatText.trim()} type="submit">Надіслати</button>
                  </form>
                  <h3>Діагностика</h3>
                  <p className="c7x-admin-muted">Проксі: {account.proxy} · {account.proxyStatus}</p>
                  <button type="button" disabled={busy} onClick={() => {
                    if (!selected) return
                    void run(async () => {
                      setDiagnostics(await workerRequest(selected, "GET", `/api/account/${account.id}/diagnostics`))
                      setShowDiagnostics(true)
                    }, false)
                  }}>Отримати діагностику</button>
                  {showDiagnostics ? <pre className="c7x-admin-log">{JSON.stringify(diagnostics, null, 2)}</pre> : null}
                  <div className="c7x-admin-section-head"><h3>Журнал</h3><button type="button" onClick={() => void navigator.clipboard.writeText(logEntries.map((x) => `[${x.timestamp}] ${x.category} ${x.message}`).join("\n"))}>Копіювати</button></div>
                  <div className="c7x-admin-log">{logEntries.length ? logEntries.map((entry) =>
                    <div key={entry.sequence}><small>{entry.timestamp} · {entry.category}</small><span>{entry.message}</span></div>) : "Записів поки немає"}</div>
                </section>
              ) : null}
            </>
          )}
        </section>
      ) : null}
      {notice ? <p className="c7x-admin-message" role="status">{notice}</p> : null}
    </div>
  )
}
