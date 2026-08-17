import { AnimatePresence, motion } from "framer-motion"
import { useMemo, useState } from "react"
import { accountTransactions, feed, initialMembers, initialRoles, permissionGroups } from "../mockData"
import type { Account, AccountRole, Member, RolePermission, Route } from "../model"
import { permissionLabels, transactionSourceLabels } from "../presentation"
import { CristalixHead } from "../components/CristalixHead"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { ChoiceGrid, CurrencyValue, Field, IconButton, PageHeader, Section, Toggle } from "../shared/Primitives"

type Invite = {
  id: string
  code: string
  expires: string
  active: boolean
}

type Props = {
  activeAccount: Account
  setActiveAccount: (account: Account) => void
  accounts: Account[]
  setAccounts: (accounts: Account[]) => void
  navigate: (route: Route) => void
  back: () => void
  routeId: Route["id"]
  params?: Record<string, string>
}

export function AccountFeatureFinal(props: Props) {
  const [members, setMembers] = useState<Member[]>(initialMembers)
  const [roles, setRoles] = useState<AccountRole[]>(initialRoles)
  const [invites, setInvites] = useState<Invite[]>([
    { id: "invite-1", code: "MN-7Q4X-2K9", expires: "19 серп. · 18:00", active: true },
  ])

  if (props.routeId === "member") {
    return <MemberPage {...props} members={members} setMembers={setMembers} roles={roles} />
  }

  if (props.routeId === "roles") {
    return <RolesPage {...props} roles={roles} setRoles={setRoles} />
  }

  if (props.routeId === "role-editor") {
    return <RoleEditorPage {...props} roles={roles} setRoles={setRoles} />
  }

  if (props.routeId === "account-wallet") {
    return <AccountWalletPage {...props} />
  }

  if (props.routeId === "account-invites") {
    return <InvitesPage {...props} invites={invites} setInvites={setInvites} />
  }

  if (props.routeId === "account-settings") {
    return <AccountSettingsPage {...props} roles={roles} members={members} setMembers={setMembers} />
  }

  if (props.routeId === "account-create") {
    return <AccountCreatePage {...props} />
  }

  if (props.routeId === "account-join") {
    return <AccountJoinPage {...props} />
  }

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

function AccountHome({ activeAccount, navigate, members, setMembers, roles, inviteCount }: Props & {
  members: Member[]
  setMembers: (members: Member[]) => void
  roles: AccountRole[]
  inviteCount: number
}) {
  const [rewardClaimed, setRewardClaimed] = useState(false)
  const [currentStreak, setCurrentStreak] = useState(18)
  const [recoverable, setRecoverable] = useState(true)
  const [recoverableStreak] = useState(17)
  const [restoresRemaining, setRestoresRemaining] = useState(2)
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null)
  const [rolesOpen, setRolesOpen] = useState(false)

  const drops = useMemo(
    () => feed
      .filter((event) => event.kind === "ITEM_DROP" && event.detail.toLowerCase() === activeAccount.nickname.toLowerCase())
      .slice(0, 3),
    [activeAccount.nickname],
  )

  const selectedMember = members.find((member) => member.id === selectedMemberId) ?? null

  const toggleMemberRole = (member: Member, roleId: string) => {
    if (member.owner) return

    const roleIds = member.roleIds.includes(roleId)
      ? member.roleIds.filter((id) => id !== roleId)
      : [...member.roleIds, roleId]

    const highestRoleId = roles
      .filter((role) => roleIds.includes(role.id))
      .sort((left, right) => right.hierarchyRank - left.hierarchyRank)[0]?.id ?? null

    setMembers(members.map((item) => item.id === member.id ? { ...item, roleIds, highestRoleId } : item))
  }

  const kickMember = (member: Member) => {
    if (member.owner) return

    setMembers(members.filter((item) => item.id !== member.id))
    setSelectedMemberId(null)
    setRolesOpen(false)
  }

  const claimDaily = () => {
    if (rewardClaimed) return

    setRewardClaimed(true)
    setCurrentStreak((value) => value + 1)
  }

  const restoreDaily = () => {
    if (!recoverable || restoresRemaining <= 0) return

    setCurrentStreak(recoverableStreak)
    setRecoverable(false)
    setRestoresRemaining((value) => value - 1)
  }

  return (
    <div className="screen account-final-screen root-screen">
      <header className="account-final-header">
        <button
          className="account-final-identity"
          onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}
        >
          <CristalixHead skinUrl={activeAccount.skinUrl} size={54} />
          <span>
            <small>Активний аккаунт</small>
            <strong>{activeAccount.nickname}</strong>
            <em>{activeAccount.role}</em>
          </span>
          <Glyph name="external" size={15} />
        </button>
      </header>

      <button className="account-final-wallet" onClick={() => navigate({ id: "account-wallet" })}>
        <span className="account-final-wallet__mark"><CurrencyMark currency="COIN" size={25} /></span>
        <span className="account-final-wallet__copy">
          <small>COIN</small>
          <strong>{activeAccount.coin.toLocaleString("uk-UA")}</strong>
        </span>
        <span className="account-final-wallet__history">
          <Glyph name="clock" size={15} />
          Історія
          <b>›</b>
        </span>
      </button>

      <section className="account-action-section">
        <div className="account-action-section__head"><h2>Аккаунт</h2></div>
        <div className="account-action-grid">
          <article className={`daily-reward-tile ${rewardClaimed ? "is-claimed" : ""}`}>
            <div className="daily-reward-tile__top">
              <span className="daily-flame"><FlameMark /></span>
              <span className="daily-streak">
                <strong>{currentStreak}</strong>
                <small>днів поспіль</small>
              </span>
              {rewardClaimed ? <span className="daily-state"><Glyph name="check" size={15} />Отримано</span> : null}
            </div>

            <div className="daily-reward-tile__reward">
              <span><CurrencyMark currency="COIN" size={14} /><b>320–520</b></span>
              <span><CurrencyMark currency="PRIME" size={13} /><b>4%</b></span>
            </div>

            <div className="daily-reward-tile__actions">
              <button className="daily-primary" disabled={rewardClaimed} onClick={claimDaily}>
                {rewardClaimed ? "До завтра" : "Забрати"}
              </button>
              {recoverable ? (
                <button className="daily-restore" onClick={restoreDaily}>
                  Відновити {recoverableStreak}
                  <small>{restoresRemaining}×</small>
                </button>
              ) : null}
            </div>
          </article>

          <StaticAction
            icon="roles"
            title="Ролі"
            meta={`${roles.length}`}
            tone="plum"
            onClick={() => navigate({ id: "roles" })}
          />
          <StaticAction
            icon="invite"
            title="Інвайти"
            meta={`${inviteCount}`}
            tone="amber"
            onClick={() => navigate({ id: "account-invites" })}
          />
          <StaticAction
            icon="spark"
            title="Персоналізації"
            meta="Account"
            tone="lime"
            onClick={() => navigate({ id: "personalization-owned", params: { target: "ACCOUNT" } })}
          />
          <StaticAction
            icon="settings"
            title="Налаштування"
            meta="Account"
            tone="neutral"
            onClick={() => navigate({ id: "account-settings" })}
          />
        </div>
      </section>

      <section className="home-panel members-panel members-panel--final">
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
                      className="member-context member-context--final"
                      initial={{ opacity: 0, y: -6, scale: 0.98 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      exit={{ opacity: 0, y: -4, scale: 0.985 }}
                    >
                      <button onClick={() => navigate({ id: "public-profile", params: { accountId: member.id } })}>
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
      </section>

      <section className="home-panel drops-panel drops-panel--final">
        <div className="home-panel__head">
          <span>
            <Glyph name="item-drop" size={19} />
            <strong>Останні дропи</strong>
          </span>
          <button onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}>Усі <span>›</span></button>
        </div>

        <div className="account-drop-list">
          {drops.length ? drops.map((drop) => (
            <button key={drop.id} onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}>
              <span className={`drop-mark drop-mark--${(drop.rarity ?? "default").toLowerCase()}`}><Glyph name="item-drop" size={17} /></span>
              <span>
                <strong>{drop.title}</strong>
                <small>{drop.rarity ?? drop.meta}</small>
              </span>
              <time>{drop.relative}</time>
              <b>›</b>
            </button>
          )) : <p className="inline-hint inline-hint--left">Дропів ще немає</p>}
        </div>
      </section>
    </div>
  )
}

function StaticAction({ icon, title, meta, tone, onClick }: {
  icon: "roles" | "invite" | "spark" | "settings"
  title: string
  meta: string
  tone: "lime" | "amber" | "plum" | "neutral"
  onClick: () => void
}) {
  return (
    <button className={`static-account-action static-account-action--${tone}`} onClick={onClick}>
      <span className="static-account-action__icon"><Glyph name={icon} size={21} /></span>
      <span><strong>{title}</strong><small>{meta}</small></span>
      <b>›</b>
    </button>
  )
}

function FlameMark() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M13.9 2.8c.5 3-1.1 4.3-2.5 5.7-1.2 1.2-2.4 2.4-2.4 4.3 0 1.5 1 2.6 2.4 2.6 1.8 0 2.8-1.5 2.5-3.4 2.2 1.2 3.6 3.2 3.6 5.4 0 3-2.4 5-5.6 5-3.8 0-6.4-2.7-6.4-6.3 0-4.6 3.2-7.1 5.5-9.3 1.3-1.2 2.2-2.3 2.9-4Z" />
    </svg>
  )
}

function MemberPage({ back, params, members, setMembers, roles }: Props & {
  members: Member[]
  setMembers: (members: Member[]) => void
  roles: AccountRole[]
}) {
  const member = members.find((item) => item.id === params?.memberId) ?? members[0]

  const toggleRole = (roleId: string) => {
    if (member.owner) return

    const roleIds = member.roleIds.includes(roleId)
      ? member.roleIds.filter((id) => id !== roleId)
      : [...member.roleIds, roleId]

    const highestRoleId = roles
      .filter((role) => roleIds.includes(role.id))
      .sort((left, right) => right.hierarchyRank - left.hierarchyRank)[0]?.id ?? null

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
        <div className="danger-zone">
          <button onClick={() => { setMembers(members.filter((item) => item.id !== member.id)); back() }}>
            <Glyph name="close" size={17} />
            Кікнути
          </button>
        </div>
      ) : null}
    </div>
  )
}

function RolesPage({ back, navigate, roles, setRoles }: Props & { roles: AccountRole[]; setRoles: (roles: AccountRole[]) => void }) {
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
        <span>Вище = вищий пріоритет</span>
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
              <span className="role-stack__order">
                <button onClick={() => move(index, -1)} aria-label="Вище">↑</button>
                <button onClick={() => move(index, 1)} aria-label="Нижче">↓</button>
              </span>
            ) : null}
          </article>
        ))}
      </div>
    </div>
  )
}

function RoleEditorPage({ back, params, roles, setRoles }: Props & { roles: AccountRole[]; setRoles: (roles: AccountRole[]) => void }) {
  const existing = roles.find((role) => role.id === params?.roleId)
  const [name, setName] = useState(existing?.name ?? "")
  const [color, setColor] = useState(existing?.color ?? "#9B927F")
  const emptyPermissions = Object.fromEntries(
    permissionGroups.flatMap((group) => group.permissions).map((permission) => [permission, false]),
  ) as Record<RolePermission, boolean>
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
        <Field label="Колір">
          <div className="color-field">
            <input type="color" value={color.slice(0, 7)} onChange={(event) => setColor(event.target.value.toUpperCase())} />
            <code>{color}</code>
          </div>
        </Field>
      </div>
      <div className="permission-groups permission-groups--redesign">
        {permissionGroups.map((group) => (
          <section key={group.label}>
            <h2>{group.label}</h2>
            {group.permissions.map((permission) => (
              <div className="permission-row" key={permission}>
                <span className="permission-name">{permissionLabels[permission]}</span>
                <Toggle
                  checked={permissions[permission]}
                  onChange={(checked) => setPermissions({ ...permissions, [permission]: checked })}
                  label={permissionLabels[permission]}
                />
              </div>
            ))}
          </section>
        ))}
      </div>
      <div className="sticky-actions">
        <button className="primary-action" onClick={save}>Зберегти</button>
        {existing && !existing.locked ? (
          <button className="destructive-action" onClick={() => { setRoles(roles.filter((role) => role.id !== existing.id)); back() }}>Видалити</button>
        ) : null}
      </div>
    </div>
  )
}

function InvitesPage({ back, invites, setInvites }: Props & { invites: Invite[]; setInvites: (invites: Invite[]) => void }) {
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
        <Field label="Строк дії">
          <ChoiceGrid
            value={expires}
            options={["NONE", "1H", "1D", "7D"] as const}
            onChange={setExpires}
            labels={{ NONE: "Без строку", "1H": "1 година", "1D": "24 години", "7D": "7 днів" }}
          />
        </Field>
        <button className="primary-action" onClick={create}>Створити інвайт</button>
      </div>
      <Section title="Активні">
        <div className="invite-list">
          {invites.filter((invite) => invite.active).map((invite) => (
            <article key={invite.id}>
              <span><strong>{invite.code}</strong><small>{invite.expires}</small></span>
              <button aria-label="Копіювати" onClick={() => navigator.clipboard?.writeText(invite.code)}>Копіювати</button>
              <button
                aria-label="Відкликати"
                className="micro-action micro-action--danger"
                onClick={() => setInvites(invites.map((item) => item.id === invite.id ? { ...item, active: false } : item))}
              >
                <Glyph name="trash" size={15} />
              </button>
            </article>
          ))}
        </div>
      </Section>
    </div>
  )
}

function AccountSettingsPage({ activeAccount, setActiveAccount, accounts, setAccounts, back, roles, members, setMembers }: Props & {
  roles: AccountRole[]
  members: Member[]
  setMembers: (members: Member[]) => void
}) {
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

  return (
    <div className="screen">
      <PageHeader title="Налаштування аккаунта" eyebrow={activeAccount.nickname} back={back} />
      <Section title="Базова роль">
        <ChoiceGrid
          value={baseRoleId}
          options={roles.filter((role) => !role.locked).map((role) => role.id)}
          onChange={setBaseRoleId}
          labels={Object.fromEntries(roles.map((role) => [role.id, role.name]))}
        />
      </Section>
      {activeAccount.owner ? (
        <Section title="Власник">
          <Field label="Передати аккаунт">
            <ChoiceGrid
              value={transferTo}
              options={members.filter((member) => !member.owner).map((member) => member.id)}
              onChange={setTransferTo}
              labels={Object.fromEntries(members.map((member) => [member.id, member.name]))}
            />
          </Field>
          <button className="secondary-action full-action" onClick={transfer}>Передати</button>
        </Section>
      ) : null}
      <Section title="Стан">
        {activeAccount.owner ? (
          active ? <button className="destructive-action" onClick={() => patchAccount({ active: false })}>Деактивувати</button>
            : <button className="primary-action" onClick={() => patchAccount({ active: true })}>Активувати</button>
        ) : <span className="inline-hint inline-hint--left">Вихід з аккаунта доступний власнику контексту через membership flow.</span>}
      </Section>
    </div>
  )
}

function AccountCreatePage({ back, activeAccount, accounts, setAccounts, setActiveAccount }: Props) {
  const [nickname, setNickname] = useState("")
  const clean = nickname.trim()
  const valid = clean.length >= 4 && clean.length <= 20

  const create = () => {
    if (!valid) return

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

  return (
    <div className="screen">
      <PageHeader title="Новий аккаунт" back={back} />
      <div className="form-stack account-create">
        <Field label="Cristalix nickname" hint="4–20 символів">
          <input autoFocus value={nickname} minLength={4} maxLength={20} onChange={(event) => setNickname(event.target.value)} />
        </Field>
        <button className="primary-action" disabled={!valid} onClick={create}>Прив'язати</button>
      </div>
    </div>
  )
}

function AccountJoinPage({ back, activeAccount, accounts, setAccounts, setActiveAccount }: Props) {
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

  return (
    <div className="screen">
      <PageHeader title="Приєднатись" back={back} />
      <div className="form-stack account-create">
        <Field label="Код запрошення" hint="MN-XXXX-XXX">
          <input
            autoFocus
            autoCapitalize="characters"
            spellCheck={false}
            value={inviteCode}
            maxLength={11}
            onChange={(event) => setInviteCode(event.target.value.toUpperCase())}
          />
        </Field>
        <button className="primary-action" disabled={!valid} onClick={join}>Приєднатись</button>
      </div>
    </div>
  )
}

function AccountWalletPage({ activeAccount, back }: Props) {
  return (
    <div className="screen">
      <PageHeader title="Гаманець" eyebrow={activeAccount.nickname} back={back} />
      <div className="balance-grid balance-grid--single"><div><CurrencyValue currency="COIN" value={activeAccount.coin} /></div></div>
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
