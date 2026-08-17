import { AnimatePresence } from "framer-motion"
import { useState } from "react"
import { accountTransactions, initialMembers, initialRoles, permissionGroups } from "../mockData"
import type { Account, AccountRole, Member, RolePermission, Route } from "../model"
import { permissionLabels, transactionSourceLabels } from "../presentation"
import { CristalixHead } from "../components/CristalixHead"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { BottomSheet, ChoiceGrid, CurrencyValue, Field, IconButton, PageHeader, Section, Toggle } from "../shared/Primitives"

type Nav = (route: Route) => void
type Invite = { id: string; code: string; expires: string; active: boolean }

type AccountFeatureProps = {
  activeAccount: Account
  setActiveAccount: (account: Account) => void
  accounts: Account[]
  setAccounts: (accounts: Account[]) => void
  navigate: Nav
  back: () => void
  routeId: Route["id"]
  params?: Record<string, string>
}

export function AccountFeature(props: AccountFeatureProps) {
  const [members, setMembers] = useState(initialMembers)
  const [roles, setRoles] = useState(initialRoles)
  const [invites, setInvites] = useState<Invite[]>([
    { id: "invite-1", code: "MN-7Q4X-2K9", expires: "19 серп. · 18:00", active: true },
  ])

  if (props.routeId === "member") return <MemberPage {...props} members={members} setMembers={setMembers} roles={roles} />
  if (props.routeId === "roles") return <RolesPage {...props} roles={roles} setRoles={setRoles} />
  if (props.routeId === "role-editor") return <RoleEditorPage {...props} roles={roles} setRoles={setRoles} />
  if (props.routeId === "account-wallet") return <AccountWalletPage {...props} />
  if (props.routeId === "account-invites") return <InvitesPage {...props} invites={invites} setInvites={setInvites} />
  if (props.routeId === "account-settings") return <AccountSettingsPage {...props} roles={roles} members={members} setMembers={setMembers} />
  if (props.routeId === "account-create") return <AccountCreatePage {...props} />
  if (props.routeId === "account-join") return <AccountJoinPage {...props} />

  return <AccountHome {...props} members={members} roles={roles} inviteCount={invites.filter(invite => invite.active).length} />
}

function AccountHome({ activeAccount, setActiveAccount, accounts, setAccounts, navigate, members, roles, inviteCount }: AccountFeatureProps & { members: Member[]; roles: AccountRole[]; inviteCount: number }) {
  const [switcher, setSwitcher] = useState(false)
  const [rewardClaimed, setRewardClaimed] = useState(false)
  const [streak, setStreak] = useState(18)
  const [restores, setRestores] = useState(2)
  const [recoverable, setRecoverable] = useState(true)

  const selectAccount = (account: Account) => {
    if (account.active === false) {
      if (!account.owner) return

      const reactivated = { ...account, active: true }
      setAccounts(accounts.map(item => item.id === account.id ? reactivated : item))
      setActiveAccount(reactivated)
      setSwitcher(false)
      return
    }

    setActiveAccount(account)
    setSwitcher(false)
  }

  return <div className="screen account-screen">
    <PageHeader title="Аккаунт" actions={<><IconButton icon="settings" label="Налаштування аккаунта" onClick={() => navigate({ id: "account-settings" })} /><IconButton icon="swap" label="Змінити аккаунт" onClick={() => setSwitcher(true)} /></>} />
    <section className="account-identity"><CristalixHead skinUrl={activeAccount.skinUrl} size={72} /><div className="account-identity__copy"><strong>{activeAccount.nickname}</strong><small>{activeAccount.role} · {activeAccount.memberCount}/{activeAccount.maxMembers}</small></div><button className="profile-link" aria-label="Відкрити профіль" onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}><Glyph name="external" size={17} /></button></section>
    <button className="wallet-strip" onClick={() => navigate({ id: "account-wallet" })}><CurrencyValue currency="COIN" value={activeAccount.coin} /><span className="wallet-strip__divider" /><CurrencyValue currency="PRIME" value={activeAccount.prime} /><span className="wallet-strip__arrow">›</span></button>

    <section className={`daily-line ${rewardClaimed ? "is-claimed" : ""}`}><div className="daily-line__streak"><span>{streak}</span><small>днів</small></div><div className="daily-line__reward"><strong>{rewardClaimed ? "Отримано" : <><CurrencyMark currency="COIN" size={13} /> 320–520</>}</strong><small>{rewardClaimed ? "сьогодні" : <><CurrencyMark currency="PRIME" size={11} /> 4%</>}</small></div>{!rewardClaimed ? <button onClick={() => { setRewardClaimed(true); setStreak(streak + 1) }}>Забрати</button> : <Glyph name="check" size={19} />}</section>
    {recoverable ? <button className="recover-line" onClick={() => { if (restores > 0) { setRecoverable(false); setRestores(restores - 1); setStreak(17) } }}><span>Відновити 17</span><small>{restores}</small></button> : null}

    <Section title={`Мембери · ${members.length}`} action={<button className="text-action" aria-label="Створити запрошення" onClick={() => navigate({ id: "account-invites" })}>+</button>}>
      <div className="member-list">{members.map((member) => { const role = roles.find((item) => item.id === member.highestRoleId); return <button className="member-row" key={member.id} onClick={() => navigate({ id: "member", params: { memberId: member.id } })}><span className="member-avatar">{member.name.slice(0, 1).toUpperCase()}</span><span className="member-row__copy"><strong>{member.name}</strong><small>{member.owner ? "Owner" : role?.name ?? "Member"}</small></span>{role ? <span className="role-dot" style={{ background: role.color }} /> : null}<span className="row__chevron">›</span></button> })}</div>
    </Section>

    <div className="account-tools"><button onClick={() => navigate({ id: "roles" })}><Glyph name="roles" size={21} /><span>Ролі</span><small>{roles.length}</small></button><button onClick={() => navigate({ id: "account-invites" })}><Glyph name="members" size={21} /><span>Запрошення</span><small>{inviteCount}</small></button></div>

    <AnimatePresence>{switcher ? <BottomSheet title="Аккаунти" onClose={() => setSwitcher(false)}><div className="account-switcher">{accounts.map((account) => <button key={account.id} className={account.id === activeAccount.id ? "is-active" : ""} aria-disabled={account.active === false && !account.owner} onClick={() => selectAccount(account)}><CristalixHead skinUrl={account.skinUrl} size={42} /><span><strong>{account.nickname}</strong><small>{account.active === false ? account.owner ? "неактивний · активувати" : "неактивний" : account.role}</small></span>{account.id === activeAccount.id ? <Glyph name="check" size={17} /> : null}</button>)}</div><div className="account-switcher-actions"><button className="secondary-action" onClick={() => { setSwitcher(false); navigate({ id: "account-create" }) }}><Glyph name="plus" size={17} /> Створити</button><button className="secondary-action" onClick={() => { setSwitcher(false); navigate({ id: "account-join" }) }}><Glyph name="members" size={17} /> За кодом</button></div></BottomSheet> : null}</AnimatePresence>
  </div>
}

function MemberPage({ back, params, members, setMembers, roles }: AccountFeatureProps & { members: Member[]; setMembers: (members: Member[]) => void; roles: AccountRole[] }) {
  const member = members.find((item) => item.id === params?.memberId) ?? members[0]
  const toggleRole = (roleId: string) => {
    if (member.owner) return

    const roleIds = member.roleIds.includes(roleId)
      ? member.roleIds.filter((id) => id !== roleId)
      : [...member.roleIds, roleId]
    const highestRoleId = roles
      .filter((role) => roleIds.includes(role.id))
      .sort((a, b) => b.hierarchyRank - a.hierarchyRank)[0]?.id ?? null

    setMembers(members.map((item) => item.id === member.id ? { ...item, roleIds, highestRoleId } : item))
  }

  return <div className="screen"><PageHeader title={member.name} back={back} /><section className="member-hero"><span className="member-hero__avatar">{member.name.slice(0, 1).toUpperCase()}</span><div><strong>{member.name}</strong><small>{member.joinedAt}</small></div></section><Section title="Ролі"><div className="permission-list">{roles.map((role) => <div className="permission-row" key={role.id}><span className="role-dot" style={{ background: role.color }} /><span><strong>{role.name}</strong><small>#{role.hierarchyRank}</small></span><Toggle checked={member.roleIds.includes(role.id)} onChange={() => toggleRole(role.id)} label={role.name} /></div>)}</div></Section>{!member.owner ? <div className="danger-zone"><button onClick={() => { setMembers(members.filter((item) => item.id !== member.id)); back() }}><Glyph name="trash" size={17} />Видалити</button></div> : null}</div>
}

function RolesPage({ back, navigate, roles, setRoles }: AccountFeatureProps & { roles: AccountRole[]; setRoles: (roles: AccountRole[]) => void }) {
  const move = (index: number, direction: -1 | 1) => {
    const next = [...roles]
    const target = index + direction

    if (target < 0 || target >= next.length || next[index].locked || next[target].locked) return

    const [item] = next.splice(index, 1)
    next.splice(target, 0, item)
    setRoles(next.map((role, roleIndex) => ({ ...role, hierarchyRank: (next.length - roleIndex) * 10 })))
  }

  return <div className="screen"><PageHeader title="Ролі" back={back} actions={<IconButton icon="plus" label="Створити роль" onClick={() => navigate({ id: "role-editor", params: { roleId: "new" } })} />} /><div className="role-list">{roles.map((role, index) => <article className="role-row" key={role.id}><button className="role-row__main" onClick={() => navigate({ id: "role-editor", params: { roleId: role.id } })}><span className="role-swatch" style={{ borderColor: role.color }} /><span><strong>{role.name}</strong><small>{Object.values(role.permissions).filter(Boolean).length} дозволів</small></span><b>{role.hierarchyRank}</b></button>{!role.locked ? <span className="role-order"><button onClick={() => move(index, -1)} aria-label="Вище">↑</button><button onClick={() => move(index, 1)} aria-label="Нижче">↓</button></span> : null}</article>)}</div></div>
}

function RoleEditorPage({ back, params, roles, setRoles }: AccountFeatureProps & { roles: AccountRole[]; setRoles: (roles: AccountRole[]) => void }) {
  const existing = roles.find((role) => role.id === params?.roleId)
  const [name, setName] = useState(existing?.name ?? "")
  const [color, setColor] = useState(existing?.color ?? "#8D9098")
  const emptyPermissions = Object.fromEntries(permissionGroups.flatMap((group) => group.permissions).map((permission) => [permission, false])) as Record<RolePermission, boolean>
  const [permissions, setPermissions] = useState<Record<RolePermission, boolean>>(existing?.permissions ?? emptyPermissions)
  const save = () => {
    if (!name.trim()) return

    if (existing) {
      setRoles(roles.map((role) => role.id === existing.id ? { ...role, name: name.trim(), color, permissions } : role))
    } else {
      setRoles([...roles, { id: `role-${Date.now()}`, name: name.trim(), color, hierarchyRank: 20, permissions }])
    }

    back()
  }

  return <div className="screen"><PageHeader title={existing ? existing.name : "Нова роль"} back={back} /><div className="form-stack role-editor"><Field label="Назва"><input value={name} maxLength={32} onChange={(event) => setName(event.target.value)} /></Field><Field label="Колір"><div className="color-field"><input type="color" value={color.slice(0, 7)} onChange={(event) => setColor(event.target.value.toUpperCase())} /><code>{color}</code></div></Field></div><div className="permission-groups">{permissionGroups.map((group) => <section key={group.label}><h2>{group.label}</h2>{group.permissions.map((permission) => <div className="permission-row" key={permission}><span className="permission-name">{permissionLabels[permission]}</span><Toggle checked={permissions[permission]} onChange={(checked) => setPermissions({ ...permissions, [permission]: checked })} label={permissionLabels[permission]} /></div>)}</section>)}</div><div className="sticky-actions"><button className="primary-action" onClick={save}>Зберегти</button>{existing && !existing.locked ? <button className="destructive-action" onClick={() => { setRoles(roles.filter((role) => role.id !== existing.id)); back() }}>Видалити</button> : null}</div></div>
}

function InvitesPage({ back, invites, setInvites }: AccountFeatureProps & { invites: Invite[]; setInvites: (invites: Invite[]) => void }) {
  const [expires, setExpires] = useState<"NONE" | "1H" | "1D" | "7D">("1D")
  const create = () => {
    const code = `MN-${Math.random().toString(36).slice(2, 6).toUpperCase()}-${Math.random().toString(36).slice(2, 5).toUpperCase()}`
    const label = { NONE: "без строку", "1H": "через 1 год", "1D": "через 24 год", "7D": "через 7 днів" }[expires]
    setInvites([{ id: `invite-${Date.now()}`, code, expires: label, active: true }, ...invites])
  }

  return <div className="screen"><PageHeader title="Запрошення" back={back} /><div className="form-stack invite-create"><Field label="Строк"><ChoiceGrid value={expires} options={["NONE", "1H", "1D", "7D"] as const} onChange={setExpires} labels={{ NONE: "∞", "1H": "1h", "1D": "24h", "7D": "7d" }} /></Field><button className="primary-action" onClick={create}>Створити</button></div><Section title="Активні"><div className="invite-list">{invites.filter((invite) => invite.active).map((invite) => <article key={invite.id}><span><strong>{invite.code}</strong><small>{invite.expires}</small></span><button aria-label="Копіювати" onClick={() => navigator.clipboard?.writeText(invite.code)}>Copy</button><button aria-label="Відкликати" className="micro-action micro-action--danger" onClick={() => setInvites(invites.map((item) => item.id === invite.id ? { ...item, active: false } : item))}><Glyph name="trash" size={15} /></button></article>)}</div></Section></div>
}

function AccountSettingsPage({ activeAccount, setActiveAccount, accounts, setAccounts, back, navigate, roles, members, setMembers }: AccountFeatureProps & { roles: AccountRole[]; members: Member[]; setMembers: (members: Member[]) => void }) {
  const [baseRoleId, setBaseRoleId] = useState(roles.find((role) => role.name === "Member")?.id ?? roles[roles.length - 1].id)
  const [transferTo, setTransferTo] = useState(members.find((member) => !member.owner)?.id ?? "")
  const active = activeAccount.active !== false

  const patchAccount = (patch: Partial<Account>) => {
    const updated = { ...activeAccount, ...patch }
    setAccounts(accounts.map((account) => account.id === activeAccount.id ? updated : account))
    setActiveAccount(updated)
  }

  const transfer = () => {
    if (!transferTo) return

    setMembers(members.map((member) => ({ ...member, owner: member.id === transferTo })))
    patchAccount({ owner: false, role: "Member" })
  }

  const deactivate = () => {
    if (!activeAccount.owner) return

    const deactivated = { ...activeAccount, active: false }
    const updatedAccounts = accounts.map(account => account.id === activeAccount.id ? deactivated : account)
    const next = updatedAccounts.find(account => account.id !== activeAccount.id && account.active !== false)

    setAccounts(updatedAccounts)
    setActiveAccount(next ?? deactivated)
    navigate({ id: next ? "account" : "me" })
  }

  const leave = () => {
    if (activeAccount.owner) return

    const remaining = accounts.filter(account => account.id !== activeAccount.id)
    const next = remaining.find(account => account.active !== false)

    setAccounts(remaining)
    if (next) setActiveAccount(next)
    navigate({ id: next ? "account" : "me" })
  }

  return <div className="screen"><PageHeader title="Налаштування" eyebrow={activeAccount.nickname} back={back} /><Section title="Базова роль"><ChoiceGrid value={baseRoleId} options={roles.filter((role) => !role.locked).map((role) => role.id)} onChange={setBaseRoleId} labels={Object.fromEntries(roles.map((role) => [role.id, role.name]))} /></Section>{activeAccount.owner ? <Section title="Власник"><Field label="Передати"><ChoiceGrid value={transferTo} options={members.filter((member) => !member.owner).map((member) => member.id)} onChange={setTransferTo} labels={Object.fromEntries(members.map((member) => [member.id, member.name]))} /></Field><button className="secondary-action full-action" onClick={transfer}>Передати</button></Section> : null}<Section title="Стан"><div className="settings-actions">{activeAccount.owner ? active ? <button className="destructive-action" onClick={deactivate}>Деактивувати</button> : <button className="primary-action" onClick={() => patchAccount({ active: true })}>Активувати</button> : <button className="destructive-action" onClick={leave}>Вийти з аккаунта</button>}</div></Section></div>
}

function AccountCreatePage({ back, activeAccount, accounts, setAccounts, setActiveAccount }: AccountFeatureProps) {
  const [nickname, setNickname] = useState("")
  const create = () => {
    const clean = nickname.trim()
    if (clean.length < 4 || clean.length > 20) return

    const id = crypto.randomUUID()
    const account: Account = { id, cristalixUuid: id, nickname: clean, role: "Owner", skinUrl: activeAccount.skinUrl, memberCount: 1, maxMembers: 6, owner: true, active: true, coin: 0, prime: 0 }
    setAccounts([...accounts, account])
    setActiveAccount(account)
    back()
  }

  return <div className="screen"><PageHeader title="Новий аккаунт" back={back} /><div className="form-stack account-create"><Field label="Cristalix nickname" hint="4–20 символів"><input autoFocus value={nickname} minLength={4} maxLength={20} onChange={(event) => setNickname(event.target.value)} /></Field><button className="primary-action" disabled={nickname.trim().length < 4} onClick={create}>Прив'язати</button></div></div>
}

function AccountJoinPage({ back, activeAccount, accounts, setAccounts, setActiveAccount }: AccountFeatureProps) {
  const [inviteCode, setInviteCode] = useState("")
  const normalized = inviteCode.trim().toUpperCase()
  const valid = /^MN-[A-Z0-9]{4}-[A-Z0-9]{3}$/.test(normalized)

  const join = () => {
    if (!valid) return

    const id = crypto.randomUUID()
    const account: Account = {
      id,
      cristalixUuid: id,
      nickname: "north",
      role: "Member",
      skinUrl: activeAccount.skinUrl,
      memberCount: 5,
      maxMembers: 8,
      owner: false,
      active: true,
      coin: 0,
      prime: 0,
    }

    setAccounts([...accounts, account])
    setActiveAccount(account)
    back()
  }

  return <div className="screen"><PageHeader title="Приєднатись" back={back} /><div className="form-stack account-create"><Field label="Код запрошення" hint="MN-XXXX-XXX"><input autoFocus autoCapitalize="characters" spellCheck={false} value={inviteCode} maxLength={11} onChange={(event) => setInviteCode(event.target.value.toUpperCase())} /></Field><button className="primary-action" disabled={!valid} onClick={join}>Приєднатись</button></div></div>
}

function AccountWalletPage({ activeAccount, back }: AccountFeatureProps) {
  return <div className="screen"><PageHeader title="Гаманець" eyebrow={activeAccount.nickname} back={back} /><div className="balance-grid"><div><CurrencyValue currency="COIN" value={activeAccount.coin} /></div><div><CurrencyValue currency="PRIME" value={activeAccount.prime} /></div></div><Section title="Історія"><div className="transaction-list">{accountTransactions.map((transaction) => <div className="transaction-row" key={transaction.id}><span className={`transaction-sign ${transaction.type === "CREDIT" ? "is-credit" : ""}`}>{transaction.type === "CREDIT" ? "+" : "−"}</span><span><strong>{transactionSourceLabels[transaction.source]}</strong><small>{transaction.createdAt}</small></span><CurrencyValue currency={transaction.currency} value={transaction.amount} compact /></div>)}</div></Section></div>
}
