// @vitest-environment jsdom

import { render, screen } from "@testing-library/react"
import { describe, expect, it, vi } from "vitest"
import type {
  NotificationPackContent,
  NotificationTemplate,
  NotificationTemplateKey,
  Personalization,
} from "../../model"
import { PersonalizationDetails } from "./PersonalizationPreview"

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}))

describe("PersonalizationDetails", () => {
  it("does not hydrate missing API content from a magic personalization id", () => {
    const item: Personalization = {
      id: "p-1",
      type: "NOTIFICATION_PACK",
      target: "USER",
      name: "Unavailable pack",
      creator: "@creator",
      createdByCurrentUser: false,
      currency: "PRIME",
      price: 100,
      status: "PUBLISHED",
      owned: false,
      active: false,
      preview: "Unavailable pack",
    }

    render(<PersonalizationDetails item={item} />)

    expect(screen.getByText("personalization.preview.missingTitle")).toBeTruthy()
    expect(screen.getByText("personalization.preview.missingCopy")).toBeTruthy()
    expect(screen.queryByText("Нові події")).toBeNull()
  })

  it("renders all six notification templates in the full pack showcase", () => {
    const item = notificationPackPersonalization()

    render(<PersonalizationDetails item={item} />)

    expect(screen.getByText("creator.editor.notificationTypes.ITEM_DROP")).toBeTruthy()
    expect(screen.getByText("creator.editor.notificationTypes.BOOSTER")).toBeTruthy()
    expect(screen.getByText("creator.editor.notificationTypes.DUNGEON")).toBeTruthy()
    expect(screen.getByText("creator.editor.notificationTypes.RAID")).toBeTruthy()
    expect(screen.getByText("creator.editor.notificationTypes.GLOBAL")).toBeTruthy()
    expect(screen.getByText("creator.editor.notificationTypes.PLAYER_OFFLINE")).toBeTruthy()
    expect(screen.getByLabelText("Player offline")).toBeTruthy()
  })
})

function notificationPackPersonalization(): Personalization {
  return {
    id: "p-notification-pack",
    type: "NOTIFICATION_PACK",
    target: "USER",
    name: "Notification pack",
    creator: "@creator",
    createdByCurrentUser: false,
    currency: "PRIME",
    price: 100,
    status: "PUBLISHED",
    owned: false,
    active: false,
    preview: "Notification pack",
    content: notificationPackContent(),
  }
}

function notificationPackContent(): NotificationPackContent {
  const keys: NotificationTemplateKey[] = [
    "ITEM_DROP",
    "BOOSTER",
    "DUNGEON",
    "RAID",
    "GLOBAL",
    "PLAYER_OFFLINE",
  ]

  return {
    kind: "NOTIFICATION_PACK",
    mixedAggregateTitle: "Mixed notifications",
    templates: Object.fromEntries(keys.map((key) => [key, notificationTemplate(key)])) as Record<
      NotificationTemplateKey,
      NotificationTemplate
    >,
    valueMappings: {},
  }
}

function notificationTemplate(key: NotificationTemplateKey): NotificationTemplate {
  return {
    emoji: null,
    soloTitle: `${key} title`,
    soloBody: `${key} body {player} {realm} {lastSeen}`,
    compact: `${key} compact`,
    aggregateTitle: `${key} aggregate`,
    variants: [],
  }
}
