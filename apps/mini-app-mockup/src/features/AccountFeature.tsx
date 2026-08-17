import { AnimatePresence } from "framer-motion"
import { useMemo, useState } from "react"
import { accountTransactions, accounts, initialMembers, initialRoles, permissionGroups } from "../mockData"
import type { Account, AccountRole, Member, RolePermission, Route } from "../model"
import { CristalixHead } from "../components/CristalixHead"
import { Glyph } from "../shared/MnemosIcon"
import { BottomSheet, CurrencyValue, Field, IconButton, PageHeader, Row, Section, Toggle } from "../shared/Primitives"

type Nav = (route: Route) => void

type AccountFeatureProps = {
  activeAccount: Account
  setActiveAccount: (account: Account) => void
  navigate: Nav
  back: () => void
  routeId: Route["id"]
  params?: Record<string, string>
}

export function AccountFeature(props: AccountFeatureProps) {
  const [members, setMembers] = useState(initialMembers)
  const [roles, setRoles] = useState(initialRoles)

  if (props.routeId === "member") return <MemberPage {...props} members={members} setMembers={setMembers} roles={roles} />
  if (props.routeId === "roles") return <RolesPage {...props} roles={roles} setRoles={setRoles} />
  if (props.routeId === "role-editor") return <RoleEditorPage {...props} roles={roles} setRoles={setRoles} />
  if (props.routeId === "account-wallet") return <AccountWalletPage {...props} />
  return <AccountHome {...props} members={members} roles={roles} />
}

function AccountHome({ activeAccount, setActiveAccount, navigate, members, roles }: AccountFeatureProps & { members: Member[]; roles: AccountRole[] }) {
  const [switcher, setSwitcher] = useState(false)
  const [rewardClaimed, setRewardClaimed] = useState(false)
  const [streak, setStreak] = useState(18)
  const [restores, setRestores] = useState(2)
  const [recoverable, setRecoverable] = useState(true)

  return <div className="screen account-screen">
    <PageHeader title="Аккаунт" actions={<IconButton icon="swap" label="Змінити аккаунт" onClick={() => setSwitcher(true)} />} />

    <section className="account-identity">
      <CristalixHead skinUrl={activeAccount.skinUrl} size={72} />
      <div className="account-identity__copy"><strong>{activeAccount.nickname}</strong><small>{activeAccount.role} · {activeAccount.memberCount}/{activeAccount.maxMembers}</small></div>
      <button className="profile-link" onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}><Glyph name="external" size={17} /></button>
    </section>

    <button className="wallet-strip" onClick={() => navigate({ id: "account-wallet" })}>
      <CurrencyValue currency="COIN" value={activeAccount.coin} />
      <span className="wallet-strip__divider" />
      <CurrencyValue currency="PRIME" value={activeAccount.prime} />
      <span className="wallet-strip__arrow">›</span>
    </button>

    <section className={`daily-line ${rewardClaimed ? "is-claimed" : ""}`}>
      <div className="daily-line__streak"><span>{streak}</span><small>streak</small></div>
      <div className="daily-line__reward"><strong>{rewardClaimed ? "Отримано" : "320–520"}</strong><small>{rewardClaimed ? "сьогодні" : "Coin · Prime chance 4%"}</small></div>
      {!rewardClaimed ? <button onClick={() => { setRewardClaimed(true); setStreak(streak + 1) }}>Claim</button> : <Glyph name="check" size={19} />}
    </section>
    {recoverable ? <button className="recover-line" onClick={() => { if (restores > 0) { setRecoverable(false); setRestores(restores - 1); setStreak(17) } }}><span>Restore 17</span><small>{restores} left</small></button> : null}

    <Section title={`Мембери · ${members.length}`} action={<button className="text-action">Invite</button>}>
      <div className="member-list">
        {members.map((member) => {
          const role = roles.find((item) => item.id === member.highestRoleId)
          return <button className="member-row" key={member.id} onClick={() => navigate({ id: "member", params: { memberId: member.id } })}>
            <span className="member-avatar">{member.name.slice(0, 1).toUpperCase()}</span>
            <span className="member-row__copy"><strong>{member.name}</strong><small>{member.owner ? "Owner" : role?.name ?? "Member"}</small></span>
            {role ? <span className="role-dot" style={{ background: role.color }} /> : null}<span className="row__chevron">›</span>
          </button>
        })}
      </div>
    </Section>

    <div className="account-tools">
      <button onClick={() => navigate({ id: "roles" })}><Glyph name="roles" size={21} /><span>Ролі</span><small>{roles.length}</small></button>
      <button onClick={() => navigate({ id: "account-wallet" })}><Glyph name="wallet" size={21} /><span>Wallet</span><small>history</small></button>
    </div>

    <AnimatePresence>{switcher ? <BottomSheet title="Аккаунти" onClose={() => setSwitcher(false)}><div className="account-switcher">{accounts.map((account) => <button key={account.id} className={account.id === activeAccount.id ? "is-active" : ""} onClick={() => { setActiveAccount(account); setSwitcher(false) }}><CristalixHead skinUrl={account.skinUrl} size={42} /><span><strong>{account.nickname}</strong><small>{account.role}</small></span>{account.id === activeAccount.id ? <Glyph name="check" size={17} /> : null}</button>)}</div><button className="secondary-action"><Glyph name="plus" size={17} /> Додати аккаунт</button></BottomSheet> : null}</AnimatePresence>
  </div>
}

function MemberPage({ back, params, members, setMembers, roles }: AccountFeatureProps & { members: Member[]; setMembers: (members: Member[]) => void; roles: AccountRole[] }) {
  const member = members.find((item) => item.id === params?.memberId) ?? members[0]
  const toggleRole = (roleId: string) => {
    if (member.owner) return
    setMembers(members.map((item) => item.id !== member.id ? item : { ...item, roleIds: item.roleIds.includes(roleId) ? item.roleIds.filter((id) => id !== roleId) : [...item.roleIds, roleId], highestRoleId: item.roleIds.includes(roleId) ? item.highestRoleId : roleId }))
  }
  return <div className="screen">
    <PageHeader title={member.name} eyebrow={member.owner ? "OWNER" : "MEMBER"} back={back} />
    <section className="member-hero"><span className="member-hero__avatar">{member.name.slice(0, 1).toUpperCase()}</span><div><strong>{member.name}</strong><small>з {member.joinedAt}</small></div></section>
    <Section title="Ролі"><div className="permission-list">{roles.map((role) => <div className="permission-row" key={role.id}><span className="role-dot" style={{ background: role.color }} /><span><strong>{role.name}</strong><small>rank {role.hierarchyRank}</small></span><Toggle checked={member.roleIds.includes(role.id)} onChange={() => toggleRole(role.id)} label={role.name} /></div>)}</div></Section>
    {!member.owner ? <div className="danger-zone"><button onClick={() => setMembers(members.filter((item) => item.id !== member.id))}><Glyph name="trash" size={17} />Видалити з аккаунта</button></div> : null}
  </div>
}

function RolesPage({ back, navigate, roles, setRoles }: AccountFeatureProps & { roles: AccountRole[]; setRoles: (roles: AccountRole[]) => void }) {
  const move = (index: number, direction: -1 | 1) => {
    const next = [...roles]
    const target = index + direction
    if (target < 0 || target >= next.length || next[index].locked) return
    const [item] = next.splice(index, 1); next.splice(target, 0, item)
    setRoles(next.map((role, roleIndex) => ({ ...role, hierarchyRank: (next.length - roleIndex) * 10 })))
  }
  return <div className="screen"><PageHeader title="Ролі" back={back} actions={<IconButton icon="plus" label="Створити роль" onClick={() => navigate({ id: "role-editor", params: { roleId: "new" } })} />} />
    <div className="role-list">{roles.map((role, index) => <article className="role-row" key={role.id}><button className="role-row__main" onClick={() => navigate({ id: "role-editor", params: { roleId: role.id } })}><span className="role-swatch" style={{ borderColor: role.color }} /><span><strong>{role.name}</strong><small>{Object.values(role.permissions).filter(Boolean).length} permissions</small></span><b>{role.hierarchyRank}</b></button>{!role.locked ? <span className="role-order"><button onClick={() => move(index, -1)}>↑</button><button onClick={() => move(index, 1)}>↓</button></span> : null}</article>)}</div>
  </div>
}

function RoleEditorPage({ back, params, roles, setRoles }: AccountFeatureProps & { roles: AccountRole[]; setRoles: (roles: AccountRole[]) => void }) {
  const existing = roles.find((role) => role.id === params?.roleId)
  const [name, setName] = useState(existing?.name ?? "")
  const [color, setColor] = useState(existing?.color ?? "#8D9490")
  const [permissions, setPermissions] = useState<Record<RolePermission, boolean>>(existing?.permissions ?? Object.fromEntries(permissionGroups.flatMap((group) => group.permissions).map((permission) => [permission, false])) as Record<RolePermission, boolean>)
  const save = () => {
    if (!name.trim()) return
    if (existing) setRoles(roles.map((role) => role.id === existing.id ? { ...role, name: name.trim(), color, permissions } : role))
    else setRoles([...roles, { id: `role-${Date.now()}`, name: name.trim(), color, hierarchyRank: 20, permissions }])
    back()
  }
  return <div className="screen"><PageHeader title={existing ? existing.name : "Нова роль"} back={back} />
    <div className="form-stack role-editor"><Field label="Назва"><input value={name} maxLength={32} onChange={(event) => setName(event.target.value)} /></Field><Field label="Колір"><div className="color-field"><input type="color" value={color.slice(0, 7)} onChange={(event) => setColor(event.target.value.toUpperCase())} /><code>{color}</code></div></Field></div>
    <div className="permission-groups">{permissionGroups.map((group) => <section key={group.label}><h2>{group.label}</h2>{group.permissions.map((permission) => <div className="permission-row" key={permission}><span className="permission-name">{permission.replaceAll("_", " ").toLowerCase()}</span><Toggle checked={permissions[permission]} onChange={(checked) => setPermissions({ ...permissions, [permission]: checked })} label={permission} /></div>)}</section>)}</div>
    <div className="sticky-actions"><button className="primary-action" onClick={save}>Зберегти</button>{existing && !existing.locked ? <button className="destructive-action" onClick={() => { setRoles(roles.filter((role) => role.id !== existing.id)); back() }}>Видалити</button> : null}</div>
  </div>
}

function AccountWalletPage({ activeAccount, back }: AccountFeatureProps) {
  return <div className="screen"><PageHeader title="Wallet" eyebrow={activeAccount.nickname} back={back} /><div className="balance-grid"><div><CurrencyValue currency="COIN" value={activeAccount.coin} /><small>Coin</small></div><div><CurrencyValue currency="PRIME" value={activeAccount.prime} /><small>Prime</small></div></div><Section title="Історія"><div className="transaction-list">{accountTransactions.map((transaction) => <div className="transaction-row" key={transaction.id}><span className={`transaction-sign ${transaction.type === "CREDIT" ? "is-credit" : ""}`}>{transaction.type === "CREDIT" ? "+" : "−"}</span><span><strong>{transaction.source.replaceAll("_", " ").toLowerCase()}</strong><small>{transaction.createdAt}</small></span><CurrencyValue currency={transaction.currency} value={transaction.amount} compact /></div>)}</div></Section></div>
}
