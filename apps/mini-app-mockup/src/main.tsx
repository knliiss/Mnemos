import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import AppFinal from "./AppFinal"
import "./styles.css"
import "./feature-details.css"
import "./event-details.css"
import "./account-details.css"
import "./bets-details.css"
import "./skin-details.css"
import "./visual-system.css"
import "./redesign-2026.css"
import "./final-revision.css"
import "./final-fixes.css"
import "./backend-alignment.css"
import "./polish-pass.css"
import "./polish-layout-overrides.css"
import "./visual-final-pass.css"
import "./workspace-final-polish.css"
import "./interaction-final.css"
import "./visual-audit-fixes.css"
import "./event-fact-final-fix.css"
import "./asset-system-polish.css"
import "./asset-system-fixes.css"
import "./global-event-raster-polish.css"
import "./event-surface-tones.css"
import "./event-surface-final.css"
import "./root-layout-final-fixes.css"
import "./requested-ui-fixes.css"
import "./requested-ui-corrections.css"
import "./product-ux-overhaul.css"
import "./notification-pack-overhaul.css"
import "./review-overhaul.css"
import "./bet-admin-overhaul.css"
import "./staff-layout-overhaul.css"
import "./ux-round-two.css"
import "./wallet-overhaul.css"
import "./role-overhaul.css"
import "./role-final-fixes.css"
import "./account-next-overhaul.css"
import "./account-next-final.css"
import "./ui-regression-fixes.css"
import "./product-v4.css"
import "./product-v5.css"
import "./product-v5-fixes.css"
import "./product-v5-hotfix.css"
import "./account-action-illustration-pass.css"
import "./typography-pass.css"
import "./mascot-pass.css"
import "./cohesive-ui-overhaul.css"
import "./cohesive-ui-overhaul-fixes.css"
import "./cohesive-ui-visual-audit-fixes.css"
import "./product-rethink.css"
import "./product-rethink-audit-fixes.css"
import "./product-round-three.css"
import "./product-round-three-fixes.css"
import "./product-round-four.css"
import "./product-round-four-fixes.css"
import "./product-round-five.css"
import "./product-round-five-affordance-fix.css"

const criticalArtwork = [
  "/ui-illustrations/account-invites.svg",
  "/ui-illustrations/account-settings.svg",
  "/ui-illustrations/daily-claim.svg",
  "/ui-illustrations/account-activity.svg",
  "/ui-illustrations/account-owner-crown.svg",
  "/ui-illustrations/workspace-creator.svg",
  "/ui-illustrations/workspace-admin.svg",
  "/ui-illustrations/admin-users.svg",
  "/ui-illustrations/admin-review.svg",
  "/ui-illustrations/admin-bets.svg",
  "/ui-illustrations/admin-badges.svg",
  "/ui-illustrations/mnemos-user-mark.svg",
  "/ui-illustrations/mnemos-mascot-cat.svg",
  "/ui-illustrations/mnemos-mascot-search.svg",
  "/ui-illustrations/mnemos-mascot-event.svg",
  "/ui-illustrations/mnemos-mascot-404.svg",
  "/ui-illustrations/creator-studio-hero.svg",
  "/ui-illustrations/creator-nick.svg",
  "/ui-illustrations/creator-theme.svg",
  "/ui-illustrations/creator-frame.svg",
  "/ui-illustrations/creator-effect.svg",
  "/ui-illustrations/creator-notification.svg",
  "/event-icons/sword.png",
  "/event-icons/aura.png",
  "/event-icons/pet.png",
  "/event-icons/book.png",
  "/event-icons/jewelry.png",
  "/event-icons/booster-luck.png",
  "/event-icons/booster-money.png",
  "/event-icons/booster-damage.png",
  "/event-icons/booster-power.png",
  "/event-icons/dungeon.svg",
  "/event-icons/raid.png",
] as const

function preloadArtwork(src: string) {
  return new Promise<void>((resolve) => {
    const image = new Image()

    image.decoding = "async"
    image.onload = () => {
      image.decode()
        .catch(() => undefined)
        .finally(resolve)
    }
    image.onerror = () => resolve()
    image.src = src
  })
}

function renderApp() {
  createRoot(document.getElementById("root")!).render(
    <StrictMode>
      <AppFinal />
    </StrictMode>,
  )
}

async function bootstrap() {
  const preload = Promise.allSettled(criticalArtwork.map(preloadArtwork))
  const timeout = new Promise<void>((resolve) => window.setTimeout(resolve, 2200))

  await Promise.race([preload, timeout])
  renderApp()
}

void bootstrap()
