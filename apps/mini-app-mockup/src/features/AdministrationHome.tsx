import { reviewQueue } from "../mockData"
import type { Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"

export function AdministrationHome({ navigate, back }: { navigate: (route: Route) => void; back: () => void }) {
  return (
    <div className="screen administration-home-screen administration-home-screen--final">
      <PageHeader title="Адміністрування" back={back} />

      <section className="workspace-intro workspace-intro--admin">
        <span className="workspace-intro__icon"><Glyph name="admin" size={23} /></span>
        <div>
          <strong>Системні інструменти</strong>
          <small>Функції, доступні ADMIN / SUPER_ADMIN</small>
        </div>
      </section>

      <div className="workspace-grid workspace-grid--final">
        <button onClick={() => navigate({ id: "moderation" })}>
          <span className="workspace-grid__icon workspace-grid__icon--danger"><Glyph name="members" size={20} /></span>
          <span><strong>Користувачі</strong><small>Internal info, санкції, wallet adjustments</small></span>
          <b>›</b>
        </button>

        <button onClick={() => navigate({ id: "personalization-review" })}>
          <span className="workspace-grid__icon workspace-grid__icon--plum"><Glyph name="creator" size={20} /></span>
          <span><strong>Персоналізації</strong><small>{reviewQueue.length} на перевірці</small></span>
          <b>›</b>
        </button>

        <button onClick={() => navigate({ id: "bet-admin" })}>
          <span className="workspace-grid__icon workspace-grid__icon--amber"><Glyph name="bet" size={20} /></span>
          <span><strong>Ставки</strong><small>Результати та item races</small></span>
          <b>›</b>
        </button>

        <button onClick={() => navigate({ id: "badge-admin" })}>
          <span className="workspace-grid__icon"><Glyph name="spark" size={20} /></span>
          <span><strong>Бейджі</strong><small>Створення та видача</small></span>
          <b>›</b>
        </button>
      </div>
    </div>
  )
}
