import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import { accountTransactions, feed, initialMembers, initialRoles, permissionGroups } from "../mockData"
import type { Account, AccountRole, Member, RolePermission, Route } from "../model"
import { permissionLabels, transactionSourceLabels } from "../presentation"
import { CristalixHead } from "../components/CristalixHead"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { ChoiceGrid, CurrencyValue, Field, IconButton, PageHeader, Section, Toggle } from "../shared/Primitives"

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

  return (
    <AccountHome
      {...props}
      members={members}
      setMembers={setMembers}
      roles={roles}
      inviteCount={invites.filter((invite) => invite.active).length}
    />
  )
}

function AccountHome({ activeAccount, navigate, members, setMembers, roles, inviteCount }: AccountFeatureProps & { members: Member[]; setMembers: (members: Member[]) => void; roles: AccountRole[]; inviteCount: number }) {
  const [rewardClaimed, setRewardClaimed] = useState(false)
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null)
  const [rolesOpen, setRolesOpen] = useState(false)
  const [railProgress, setRailProgress] = useState(0.5)
  const railRef = useRef<HTMLDivElement>(null)

  const drops = feed
    .filter((event) => event.kind === "ITEM_DROP" && event.detail.toLowerCase() === activeAccount.nickname.toLowerCase())
    .slice(0, 3)

  const modules = [
    {
      id: "daily",
      icon: "daily" as const,
      title: "Щоденна нагорода",
      meta: rewardClaimed ? "Отримано" : "Готова",
      tone: "lime",
      action: () => setRewardClaimed(true),
    },
    {
      id: "roles",
      icon: "roles" as const,
      title: "Ролі",
      meta: `${roles.length}`,
      tone: "plum",
      action: () => navigate({ id: "roles" }),
    },
    {
      id: "invites",
      icon: "invite" as const,
      title: "Інвайти",
      meta: `${inviteCount}`,
      tone: "amber",
      action: () => navigate({ id: "account-invites" }),
    },
    {
      id: "personalization",
      icon: "spark" as const,
      title: "Персоналізації",
      meta: "Account",
      tone: "plum",
      action: () => navigate({ id: "personalization-owned" }),
    },
    {
      id: "settings",
      icon: "settings" as const,
      title: "Налаштування",
      meta: "Account",
      tone: "neutral",
      action: () => navigate({ id: "account-settings" }),
    },
  ]

  useEffect(() => {
    const rail = railRef.current
    if (!rail) return

    const frame = window.requestAnimationFrame(() => {
      const segment = rail.scrollWidth / 3
      rail.scrollLeft = segment
    })

    return () => window.cancelAnimationFrame(frame)
  }, [])

  const handleRailScroll = () => {
    const rail = railRef.current
    if (!rail || !rail.scrollWidth) return

    const segment = rail.scrollWidth / 3
    if (!segment) return

    if (rail.scrollLeft < segment * 0.45) {
      rail.scrollLeft += segment
      return
    }

    if (rail.scrollLeft > segment * 1.55) {
      rail.scrollLeft -= segment
      return
    }

    setRailProgress((rail.scrollLeft % segment) / segment)
  }

  const selectedMember = members.find((member) => member.id === selectedMemberId) ?? null

  const toggleMemberRole = (member: Member, roleId: string) => {
    if (member.owner) return

    const roleIds = member.roleIds.includes(roleId)
      ? member.roleIds.filter((id) => id !== roleId)
      : [...member.roleIds, roleId]

    const highestRoleId = roles
      .filter((role) => roleIds.includes(role.id))
      .sort((a, b) => b.hierarchyRank - a.hierarchyRank)[0]?.id ?? null

    setMembers(members.map((item) => item.id === member.id ? { ...item, roleIds, highestRoleId } : item))
  }

  const kickMember = (member: Member) => {
    if (member.owner) return

    setMembers(members.filter((item) => item.id !== member.id))
    setSelectedMemberId(null)
    setRolesOpen(false)
  }

  return (
    <div className="screen account-home-screen root-screen">
      <header className="account-home-header">
        <button className="account-home-identity" onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}>
          <CristalixHead skinUrl={activeAccount.skinUrl} size={54} />
          <span>
            <strong>{activeAccount.nickname}</strong>
            <small><i />{activeAccount.role}</small>
          </span>
          <Glyph name="external" size={14} />
        </button>
      </header>

      <button className="account-coin-card" onClick={() => navigate({ id: "account-wallet" })}>
        <span className="account-coin-card__label">Баланс аккаунта</span>
        <span className="account-coin-card__value">
          <CurrencyMark currency="COIN" size={30} />
          <b>{activeAccount.coin.toLocaleString("uk-UA")}</b>
        </span>
        <span className="account-coin-card__footer">
          <Glyph name="clock" size={16} />
          Історія транзакцій
          <b>›</b>
        </span>
      </button>

      <section className="home-block home-block--rail">
        <div className="home-block__head">
          <h2>Швидкий доступ</h2>
          <span>гортай</span>
        </div>
        <div className="quick-rail-shell" style={{ "--rail-progress": railProgress } as React.CSSProperties}>
          <div ref={railRef} className="quick-rail" onScroll={handleRailScroll}>
            {[...modules, ...modules, ...modules].map((module, index) => (
              <button key={`${module.id}-${index}`} className={`quick-module quick-module--${module.tone}`} onClick={module.action}>
                <span className="quick-module__icon"><Glyph name={module.icon} size={22} /></span>
                <span className="quick-module__copy">
                  <strong>{module.title}</strong>
                  <small>{module.meta}</small>
                </span>
              </button>
            ))}
          </div>
        </div>
      </section>

      <section className="home-panel members-panel">
        <div className="home-panel__head">
          <span>
            <Glyph name="members" size={19} />
            <strong>Учасники</strong>
            <small>{members.length}/{activeAccount.maxMembers}</small>
          </span>
          <button onClick={() => navigate({ id: "account-invites" })} aria-label="Запрошення">
            <Glyph name="invite" size={18} />
          </button>
        </div>

        <div className="member-home-list">
          {members.map((member, index) => {
            const role = roles.find((item) => item.id === member.highestRoleId)
            const selected = selectedMemberId === member.id
            const status = index === 0 ? "онлайн" : index === 1 ? "7 хв тому" : index === 2 ? "34 хв тому" : "15 серп."

            return (
              <div className="member-home-wrap" key={member.id}>
                <button
                  className={`member-home-row ${selected ? "is-selected" : ""}`}
                  onClick={() => {
                    setSelectedMemberId(selected ? null : member.id)
                    setRolesOpen(false)
                  }}
                >
                  <span className="member-home-avatar">{member.name.slice(0, 1).toUpperCase()}</span>
                  <span className="member-home-copy">
                    <strong>{member.name}</strong>
                    <small style={{ "--role-color": role?.color } as React.CSSProperties}>{role?.name ?? "Учасник"}</small>
                  </span>
                  <span className={`member-home-status ${index === 0 ? "is-online" : ""}`}>{status}</span>
                </button>

                <AnimatePresence>
                  {selected && selectedMember ? (
                    <motion.div
                      className="member-context"
                      initial={{ opacity: 0, y: -6, scale: 0.98 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      exit={{ opacity: 0, y: -4, scale: 0.985 }}
                    >
                      <button onClick={() => navigate({ id: "member", params: { memberId: member.id } })}>
                        <Glyph name="person" size={17} />
                        <span>Профіль</span>
                        <b>›</b>
                      </button>

                      <button onClick={() => setRolesOpen((value) => !value)}>
                        <Glyph name="roles" size={17} />
                        <span>Ролі</span>
                        <b>{rolesOpen ? "⌃" : "›"}</b>
                      </button>

                      <AnimatePresence initial={false}>
                        {rolesOpen ? (
                          <motion.div
                            className="member-context__roles"
                            initial={{ opacity: 0, height: 0 }}
                            animate={{ opacity: 1, height: "auto" }}
                            exit={{ opacity: 0, height: 0 }}
                          >
                            {roles.filter((role) => !role.locked || member.owner).map((role) => (
                              <button
                                key={role.id}
                                className={member.roleIds.includes(role.id) ? "is-active" : ""}
                                disabled={member.owner}
                                onClick={() => toggleMemberRole(member, role.id)}
                              >
                                <i style={{ background: role.color }} />
                                <span>{role.name}</span>
                                {member.roleIds.includes(role.id) ? <Glyph name="check" size={14} /> : null}
                              </button>
                            ))}
                          </motion.div>
                        ) : null}
                      </AnimatePresence>

                      {!member.owner ? (
                        <button className="member-context__danger" onClick={() => kickMember(member)}>
                          <Glyph name="close" size={17} />
                          <span>Кікнути</span>
                        </button>
                      ) : null}
                    </motion.div>
                  ) : null}
                </AnimatePresence>
              </div>
            )
          })}
        </div>

        <button className="home-panel__footer" onClick={() => navigate({ id: "roles" })}>
          Керування ролями
          <span>›</span>
        </button>
      </section>

      <section className="home-panel drops-panel">
        <div className="home-panel__head">
          <span>
            <Glyph name="item-drop" size={19} />
            <strong>Останні дропи</strong>
          </span>
          <button onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}>Усі <span>›</span></button>
        </div>

        <div className="account-drop-list">
          {drops.map((drop) => (
            <button key={drop.id} onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}>
              <span className={`drop-mark drop-mark--${(drop.rarity ?? "default").toLowerCase()}`}><Glyph name="item-drop" size={17} /></span>
              <span>
                <strong>{drop.title}</strong>
                <small>{drop.rarity ?? drop.meta}</small>
              </span>
              <time>{drop.relative}</time>
              <b>›</b>
            </button>
          ))}
        </div>
      </section>
    </div>
  )
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

  return (
    <div className="screen member-detail-screen">
      <PageHeader title={member.name} back={back} />
      <section className="member-hero member-hero--redesign">
        <span className="member-hero__avatar">{member.name.slice(0, 1).toUpperCase()}</span>
        <div><strong>{member.name}</strong><small>Приєднався {member.joinedAt}</small></div>
      </section>
      <Section title="Ролі">
        <div className="member-role-grid">
          {roles.map((role) => (
            <button
              key={role.id}
              className={member.roleIds.includes(role.id) ? "is-active" : ""}
              disabled={member.owner}
              onClick={() => toggleRole(role.id)}
            >
              <i style={{ background: role.color }} />
              <span>{role.name}</span>
              {member.roleIds.includes(role.id) ? <Glyph name="check" size={14} /> : null}
            </button>
          ))}
        </div>
      </Section>
      {!member.owner ? (
        <div className="danger-zone"><button onClick={() => { setMembers(members.filter((item) => item.id !== member.id)); back() }}><Glyph name="close" size={17} />Кікнути</button></div>
      ) : null}
    </div>
  )
}

function RolesPage({ back, navigate, roles, setRoles }: AccountFeatureProps & { roles: AccountRole[]; setRoles: (roles: AccountRole[]) => void }) {
  const [reordering, setReordering] = useState(false)

  const move = (index: number, direction: -1 | 1) => {
    const next = [...roles]
    const target = index + direction

    if (target < 0 || target >= next.length || next[index].locked || next[target].locked) return

    const [item] = next.splice(index, 1)
    next.splice(target, 0, item)
    setRoles(next.map((role, roleIndex) => ({ ...role, hierarchyRank: (next.length - roleIndex) * 10 })))
  }

  return (
    <div className="screen roles-screen-redesign">
      <PageHeader
        title="Ролі"
        back={back}
        actions={<IconButton icon="plus" label="Створити роль" onClick={() => navigate({ id: "role-editor", params: { roleId: "new" } })} />}
      />
      <div className="roles-toolbar">
        <span>Вище в списку = вищий пріоритет</span>
        <button className={reordering ? "is-active" : ""} onClick={() => setReordering((value) => !value)}>{reordering ? "Готово" : "Порядок"}</button>
      </div>
      <div className="role-stack">
        {roles.map((role, index) => (
          <article key={role.id}>
            <button className="role-stack__main" onClick={() => navigate({ id: "role-editor", params: { roleId: role.id } })}>
              <i style={{ background: role.color }} />
              <span><strong>{role.name}</strong><small>{Object.values(role.permissions).filter(Boolean).length} дозволів</small></span>
              {role.locked ? <small>системна</small> : <b>›</b>}
            </button>
            {reordering && !role.locked ? (
              <span className="role-stack__order"><button onClick={() => move(index, -1)} aria-label="Вище">↑</button><button onClick={() => move(index, 1)} aria-label="Нижче">↓</button></span>
            ) : null}
          </article>
        ))}
      </div>
    </div>
  )
}

function RoleEditorPage({ back, params, roles, setRoles }: AccountFeatureProps & { roles: AccountRole[]; setRoles: (roles: AccountRole[]) => void }) {
  const existing = roles.find((role) => role.id === params?.roleId)
  const [name, setName] = useState(existing?.name ?? "")
  const [color, setColor] = useState(existing?.color ?? "#9B927F")
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

  return (
    <div className="screen role-editor-redesign">
      <PageHeader title={existing ? existing.name : "Нова роль"} back={back} />
      <div className="role-preview-line"><i style={{ background: color }} /><strong>{name || "Назва ролі"}</strong></div>
      <div className="form-stack role-editor">
        <Field label="Назва"><input value={name} maxLength={32} onChange={(event) => setName(event.target.value)} /></Field>
        <Field label="Колір"><div className="color-field"><input type="color" value={color.slice(0, 7)} onChange={(event) => setColor(event.target.value.toUpperCase())} /><code>{color}</code></div></Field>
      </div>
      <div className="permission-groups permission-groups--redesign">
        {permissionGroups.map((group) => (
          <section key={group.label}>
            <h2>{group.label}</h2>
            {group.permissions.map((permission) => (
              <div className="permission-row" key={permission}>
                <span className="permission-name">{permissionLabels[permission]}</span>
                <Toggle checked={permissions[permission]} onChange={(checked) => setPermissions({ ...permissions, [permission]: checked })} label={permissionLabels[permission]} />
              </div>
            ))}
          </section>
        ))}
      </div>
      <div className="sticky-actions">
        <button className="primary-action" onClick={save}>Зберегти</button>
        {existing && !existing.locked ? <button className="destructive-action" onClick={() => { setRoles(roles.filter((role) => role.id !== existing.id)); back() }}>Видалити</button> : null}
      </div>
    </div>
  )
}

function InvitesPage({ back, invites, setInvites }: AccountFeatureProps & { invites: Invite[]; setInvites: (invites: Invite[]) => void }) {
  const [expires, setExpires] = useState<"NONE" | "1H" | "1D" | "7D">("1D")

  const create = () => {
    const code = `MN-${Math.random().toString(36).slice(2, 6).toUpperCase()}-${Math.random().toString(36).slice(2, 5).toUpperCase()}`
    const label = { NONE: "без строку", "1H": "через 1 год", "1D": "через 24 год", "7D": "через 7 днів" }[expires]
    setInvites([{ id: `invite-${Date.now()}`, code, expires: label, active: true }, ...invites])
  }

  return (
    <div className="screen invites-screen-redesign">
      <PageHeader title="Інвайти" back={back} />
      <div className="form-stack invite-create">
        <Field label="Строк дії"><ChoiceGrid value={expires} options={["NONE", "1H", "1D", "7D"] as const} onChange={setExpires} labels={{ NONE: "Без строку", "1H": "1 година", "1D": "24 години", "7D": "7 днів" }} /></Field>
        <button className="primary-action" onClick={create}>Створити інвайт</button>
      </div>
      <Section title="Активні">
        <div className="invite-list">
          {invites.filter((invite) => invite.active).map((invite) => (
            <article key={invite.id}>
              <span><strong>{invite.code}</strong><small>{invite.expires}</small></span>
              <button aria-label="Копіювати" onClick={() => navigator.clipboard?.writeText(invite.code)}>Копіювати</button>
              <button aria-label="Відкликати" className="micro-action micro-action--danger" onClick={() => setInvites(invites.map((item) => item.id === invite.id ? { ...item, active: false } : item))}><Glyph name="trash" size={15} /></button>
            </article>
          ))}
        </div>
      </Section>
    </div>
  )
}

function AccountSettingsPage({ activeAccount, setActiveAccount, accounts, setAccounts, back, roles, members, setMembers }: AccountFeatureProps & { roles: AccountRole[]; members: Member[]; setMembers: (members: Member[]) => void }) {
  const [baseRoleId, setBaseRoleId] = useState(roles.find((role) => !role.locked)?.id ?? roles[roles.length - 1].id)
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
    patchAccount({ owner: false, role: "Учасник" })
  }

  const deactivate = () => {
    if (!activeAccount.owner) return

    const deactivated = { ...activeAccount, active: false }
    const updatedAccounts = accounts.map((account) => account.id === activeAccount.id ? deactivated : account)
    const next = updatedAccounts.find((account) => account.id !== activeAccount.id && account.active !== false)

    setAccounts(updatedAccounts)
    setActiveAccount(next ?? deactivated)
    back()
  }

  const leave = () => {
    if (activeAccount.owner) return

    const remaining = accounts.filter((account) => account.id !== activeAccount.id)
    const next = remaining.find((account) => account.active !== false)

    setAccounts(remaining)
    if (next) setActiveAccount(next)
    back()
  }

  return (
    <div className="screen">
      <PageHeader title="Налаштування аккаунта" eyebrow={activeAccount.nickname} back={back} />
      <Section title="Базова роль"><ChoiceGrid value={baseRoleId} options={roles.filter((role) => !role.locked).map((role) => role.id)} onChange={setBaseRoleId} labels={Object.fromEntries(roles.map((role) => [role.id, role.name]))} /></Section>
      {activeAccount.owner ? <Section title="Власник"><Field label="Передати аккаунт"><ChoiceGrid value={transferTo} options={members.filter((member) => !member.owner).map((member) => member.id)} onChange={setTransferTo} labels={Object.fromEntries(members.map((member) => [member.id, member.name]))} /></Field><button className="secondary-action full-action" onClick={transfer}>Передати</button></Section> : null}
      <Section title="Стан"><div className="settings-actions">{activeAccount.owner ? active ? <button className="destructive-action" onClick={deactivate}>Деактивувати</button> : <button className="primary-action" onClick={() => patchAccount({ active: true })}>Активувати</button> : <button className="destructive-action" onClick={leave}>Вийти з аккаунта</button>}</div></Section>
    </div>
  )
}

function AccountCreatePage({ back, activeAccount, accounts, setAccounts, setActiveAccount }: AccountFeatureProps) {
  const [nickname, setNickname] = useState("")

  const create = () => {
    const clean = nickname.trim()
    if (clean.length < 4 || clean.length > 20) return

    const id = crypto.randomUUID()
    const account: Account = {
      id,
      cristalixUuid: id,
      nickname: clean,
      role: "Власник",
      skinUrl: activeAccount.skinUrl,
      memberCount: 1,
      maxMembers: 6,
      owner: true,
      active: true,
      coin: 0,
    }

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
      role: "Учасник",
      skinUrl: activeAccount.skinUrl,
      memberCount: 5,
      maxMembers: 8,
      owner: false,
      active: true,
      coin: 0,
    }

    setAccounts([...accounts, account])
    setActiveAccount(account)
    back()
  }

  return <div className="screen"><PageHeader title="Приєднатись" back={back} /><div className="form-stack account-create"><Field label="Код інвайту" hint="MN-XXXX-XXX"><input autoFocus autoCapitalize="characters" spellCheck={false} value={inviteCode} maxLength={11} onChange={(event) => setInviteCode(event.target.value.toUpperCase())} /></Field><button className="primary-action" disabled={!valid} onClick={join}>Приєднатись</button></div></div>
}

function AccountWalletPage({ activeAccount, back }: AccountFeatureProps) {
  return (
    <div className="screen wallet-detail-screen">
      <PageHeader title="COIN" eyebrow={activeAccount.nickname} back={back} />
      <div className="single-balance-card"><CurrencyValue currency="COIN" value={activeAccount.coin} /></div>
      <Section title="Історія">
        <div className="transaction-list">
          {accountTransactions.map((transaction) => (
            <div className="transaction-row" key={transaction.id}>
              <span className={`transaction-sign ${transaction.type === "CREDIT" ? "is-credit" : ""}`}>{transaction.type === "CREDIT" ? "+" : "−"}</span>
              <span><strong>{transactionSourceLabels[transaction.source]}</strong><small>{transaction.createdAt}</small></span>
              <CurrencyValue currency="COIN" value={transaction.amount} compact />
            </div>
          ))}
        </div>
      </Section>
    </div>
  )
}
