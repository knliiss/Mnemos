import { useState } from "react"
import { initialMembers, initialRoles } from "../mockData"
import type { Account, Route } from "../model"
import { ChoiceGrid, Field, PageHeader, Section } from "../shared/Primitives"

type Props = {
  activeAccount: Account
  setActiveAccount: (account: Account) => void
  accounts: Account[]
  setAccounts: (accounts: Account[]) => void
  navigate: (route: Route) => void
  back: () => void
}

export function AccountSettingsFinal({ activeAccount, setActiveAccount, accounts, setAccounts, navigate, back }: Props) {
  const [baseRoleId, setBaseRoleId] = useState(initialRoles.find((role) => !role.locked)?.id ?? initialRoles[initialRoles.length - 1].id)
  const [transferTo, setTransferTo] = useState(initialMembers.find((member) => !member.owner)?.id ?? "")
  const active = activeAccount.active !== false

  const patchAccount = (patch: Partial<Account>) => {
    const updated = { ...activeAccount, ...patch }
    setAccounts(accounts.map((account) => account.id === activeAccount.id ? updated : account))
    setActiveAccount(updated)
  }

  const transfer = () => {
    if (!activeAccount.owner || !transferTo) return

    patchAccount({ owner: false, role: "Учасник" })
  }

  const deactivate = () => {
    if (!activeAccount.owner || !active) return

    const deactivated = { ...activeAccount, active: false }
    const updatedAccounts = accounts.map((account) => account.id === activeAccount.id ? deactivated : account)
    const next = updatedAccounts.find((account) => account.id !== activeAccount.id && account.active !== false)

    setAccounts(updatedAccounts)
    if (next) setActiveAccount(next)
    navigate({ id: "account" })
  }

  const reactivate = () => {
    if (!activeAccount.owner || active) return

    patchAccount({ active: true })
  }

  const leave = () => {
    if (activeAccount.owner) return

    const remaining = accounts.filter((account) => account.id !== activeAccount.id)
    const next = remaining.find((account) => account.active !== false)

    setAccounts(remaining)
    if (next) setActiveAccount(next)
    navigate({ id: "account" })
  }

  return (
    <div className="screen account-settings-final">
      <PageHeader title="Налаштування аккаунта" eyebrow={activeAccount.nickname} back={back} />

      <Section title="Базова роль">
        <ChoiceGrid
          value={baseRoleId}
          options={initialRoles.filter((role) => !role.locked).map((role) => role.id)}
          onChange={setBaseRoleId}
          labels={Object.fromEntries(initialRoles.map((role) => [role.id, role.name]))}
        />
      </Section>

      {activeAccount.owner ? (
        <Section title="Власник">
          <Field label="Передати аккаунт">
            <ChoiceGrid
              value={transferTo}
              options={initialMembers.filter((member) => !member.owner).map((member) => member.id)}
              onChange={setTransferTo}
              labels={Object.fromEntries(initialMembers.map((member) => [member.id, member.name]))}
            />
          </Field>
          <button className="secondary-action full-action" disabled={!transferTo} onClick={transfer}>Передати</button>
        </Section>
      ) : null}

      <Section title="Стан">
        <div className="account-lifecycle-actions">
          {activeAccount.owner && active ? <button className="destructive-action" onClick={deactivate}>Деактивувати</button> : null}
          {activeAccount.owner && !active ? <button className="primary-action" onClick={reactivate}>Активувати</button> : null}
          {!activeAccount.owner ? <button className="destructive-action" onClick={leave}>Вийти з аккаунта</button> : null}
        </div>
      </Section>
    </div>
  )
}
