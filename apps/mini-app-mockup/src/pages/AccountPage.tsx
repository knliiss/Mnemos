import { motion } from "framer-motion"
import { Check, Copy, Plus, Shield, UserRound } from "lucide-react"
import { useState } from "react"
import { CristalixHead } from "../components/CristalixHead"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import type { MockAccount } from "../types"

const initialMembers = [
  { id: "m1", name: "knalis", role: "Owner", mark: "K" },
  { id: "m2", name: "north", role: "Admin", mark: "N" },
  { id: "m3", name: "vex", role: "Member", mark: "V" },
]

export function AccountPage({
  activeAccount,
  onBack,
  onNotify,
}: {
  activeAccount: MockAccount
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [members, setMembers] = useState(initialMembers)
  const [inviteCreated, setInviteCreated] = useState(false)

  const createInvite = () => {
    setInviteCreated(true)
    onNotify("Invite link створено")
  }

  const addMockMember = () => {
    if (members.length >= 5) {
      onNotify("Досягнуто ліміт учасників")
      return
    }

    setMembers((current) => [...current, { id: `m${current.length + 1}`, name: "mare", role: "Member", mark: "M" }])
    onNotify("Учасника додано")
  }

  return (
    <PageFrame page="account">
      <PageHeader title="Аккаунт" eyebrow="CONTEXT" onBack={onBack} />

      <section className="account-detail-hero">
        <CristalixHead skinUrl={activeAccount.skinUrl} size={76} />
        <div>
          <ScopeBadge scope="account" />
          <h2>{activeAccount.nickname}</h2>
          <span>{activeAccount.shortId} · {activeAccount.role}</span>
        </div>
      </section>

      <section className="detail-section">
        <div className="detail-section-heading">
          <h2>Учасники</h2>
          <span className="detail-counter">{members.length} / 5</span>
        </div>

        <div className="member-list">
          {members.map((member) => (
            <article key={member.id} className="member-row">
              <span className="member-avatar">{member.mark}</span>
              <div><strong>{member.name}</strong><span>{member.role}</span></div>
              <span className={`role-pill role-pill--${member.role.toLowerCase()}`}>{member.role}</span>
            </article>
          ))}
        </div>

        <div className="account-actions-grid">
          <motion.button onClick={addMockMember} whileTap={{ scale: 0.98 }}>
            <Plus size={16} strokeWidth={1.8} />
            <span>Учасник</span>
          </motion.button>
          <motion.button onClick={createInvite} whileTap={{ scale: 0.98 }}>
            {inviteCreated ? <Check size={16} strokeWidth={2} /> : <Copy size={16} strokeWidth={1.8} />}
            <span>{inviteCreated ? "Створено" : "Invite"}</span>
          </motion.button>
        </div>
      </section>

      <section className="detail-section">
        <div className="detail-section-heading"><h2>Ролі</h2><Shield size={16} strokeWidth={1.7} /></div>
        <div className="role-list">
          <div><span className="role-color role-color--owner" /><strong>Owner</strong><small>Усі дозволи</small></div>
          <div><span className="role-color role-color--admin" /><strong>Admin</strong><small>Керування аккаунтом</small></div>
          <div><span className="role-color role-color--member" /><strong>Member</strong><small>Базовий доступ</small></div>
        </div>
      </section>

      <section className="account-status-card">
        <UserRound size={17} strokeWidth={1.7} />
        <div><strong>Активний</strong><span>Цей аккаунт використовується як поточний context</span></div>
        <span className="status-dot" />
      </section>
    </PageFrame>
  )
}
