// @vitest-environment jsdom

import type { ReactNode } from "react"
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { PersonalizationReviewDetail } from "./PersonalizationReviewDetail"

const personalizationId = "019c11d1-bb2a-7000-8000-000000000302"

const mocks = vi.hoisted(() => ({
  listManagedPersonalizations: vi.fn(),
  listPersonalizationReviewQueue: vi.fn(),
  unlistPersonalization: vi.fn(),
  relistPersonalization: vi.fn(),
  approvePersonalization: vi.fn(),
  rejectPersonalization: vi.fn(),
  handleError: vi.fn(),
}))

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}))

vi.mock("@/shared/api/personalizationModerationApi", () => ({
  listManagedPersonalizations: mocks.listManagedPersonalizations,
  listPersonalizationReviewQueue: mocks.listPersonalizationReviewQueue,
  unlistPersonalization: mocks.unlistPersonalization,
  relistPersonalization: mocks.relistPersonalization,
  approvePersonalization: mocks.approvePersonalization,
  rejectPersonalization: mocks.rejectPersonalization,
}))

vi.mock("@/shared/errors/errorHandler", () => ({
  handleError: mocks.handleError,
}))

vi.mock("../../data/MnemosDataProvider", () => ({
  useMnemosData: () => ({
    user: {
      id: "019c11d1-bb2a-7000-8000-000000000301",
      username: "admin",
    },
  }),
}))

vi.mock("../../personalization/data/personalizationMappers", () => ({
  toPersonalization: (response: { status?: string; price?: number }) => ({
    id: "019c11d1-bb2a-7000-8000-000000000302",
    name: "Moderated Theme",
    creator: "creator",
    type: "PROFILE_THEME",
    target: "ACCOUNT",
    status: response.status ?? "PUBLISHED",
    currency: "COIN",
    price: response.price ?? 1200,
    content: null,
  }),
}))

vi.mock("../../components/personalization/PersonalizationPreview", () => ({
  PersonalizationDetails: () => <div data-testid="details" />,
  PersonalizationVisual: () => <div data-testid="visual" />,
}))

vi.mock("../../personalization/components/NotificationPackTelegramPreview", () => ({
  NotificationPackTelegramPreview: () => <div data-testid="notification-preview" />,
}))

vi.mock("../../personalization/personalizationPresentation", () => ({
  personalizationIllustrations: {
    NOTIFICATION_PACK: "/notification-pack.svg",
  },
}))

vi.mock("../../shared/MnemosIcon", () => ({
  CurrencyMark: () => <span aria-hidden="true" />,
  Glyph: () => <span aria-hidden="true" />,
}))

vi.mock("../../shared/Primitives", () => ({
  Field: ({ children }: { children: ReactNode }) => <div>{children}</div>,
  PageHeader: ({ title }: { title: string }) => <header>{title}</header>,
}))

vi.mock("../../system/FeatureEmpty", () => ({
  FeatureEmpty: ({ title }: { title: string }) => <div>{title}</div>,
}))

vi.mock("../../system/FeatureLoading", () => ({
  FeatureLoading: () => <div>loading</div>,
}))

beforeEach(() => {
  Object.values(mocks).forEach((mock) => mock.mockReset())

  mocks.listManagedPersonalizations.mockResolvedValue([
    {
      id: personalizationId,
      origin: "CREATOR",
      status: "PUBLISHED",
      price: 1200,
    },
  ])
  mocks.listPersonalizationReviewQueue.mockResolvedValue([
    {
      id: personalizationId,
      origin: "CREATOR",
      status: "IN_REVIEW",
      price: 1200,
    },
  ])
  mocks.unlistPersonalization.mockResolvedValue({})
  mocks.relistPersonalization.mockResolvedValue({})
  mocks.approvePersonalization.mockResolvedValue({})
  mocks.rejectPersonalization.mockResolvedValue({})
})

afterEach(() => {
  cleanup()
})

describe("PersonalizationReviewDetail", () => {
  it("requires explicit confirmation before approving a personalization", async () => {
    const back = vi.fn()

    render(
      <PersonalizationReviewDetail
        personalizationId={personalizationId}
        view="REVIEW"
        back={back}
      />,
    )

    fireEvent.click(await screen.findByText("personalization.review.approve"))

    expect(mocks.approvePersonalization).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", {
      name: "personalization.review.decisionConfirmTitle",
    })
    const confirmButton = screen.getByText("personalization.review.confirmApprove")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirmButton)
    })

    fireEvent.click(confirmButton)

    await waitFor(() => {
      expect(mocks.approvePersonalization).toHaveBeenCalledWith(personalizationId, "COIN", 1200)
      expect(back).toHaveBeenCalledTimes(1)
    })

    expect(dialog).toBeDefined()
  })

  it("requires explicit confirmation before rejecting a personalization", async () => {
    const back = vi.fn()

    render(
      <PersonalizationReviewDetail
        personalizationId={personalizationId}
        view="REVIEW"
        back={back}
      />,
    )

    const reason = await screen.findByPlaceholderText("personalization.review.rejectionPlaceholder")
    fireEvent.change(reason, { target: { value: "Needs a cleaner preview" } })
    fireEvent.click(screen.getByText("personalization.review.reject"))

    expect(mocks.rejectPersonalization).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", {
      name: "personalization.review.decisionConfirmTitle",
    })
    const confirmButton = screen.getByText("personalization.review.confirmReject")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirmButton)
    })

    fireEvent.click(confirmButton)

    await waitFor(() => {
      expect(mocks.rejectPersonalization).toHaveBeenCalledWith(
        personalizationId,
        "Needs a cleaner preview",
      )
      expect(back).toHaveBeenCalledTimes(1)
    })

    expect(dialog).toBeDefined()
  })

  it("dismisses a prepared moderation decision with Escape", async () => {
    render(
      <PersonalizationReviewDetail
        personalizationId={personalizationId}
        view="REVIEW"
        back={vi.fn()}
      />,
    )

    fireEvent.click(await screen.findByText("personalization.review.approve"))

    const dialog = screen.getByRole("alertdialog", {
      name: "personalization.review.decisionConfirmTitle",
    })
    fireEvent.keyDown(dialog, { key: "Escape" })

    expect(
      screen.queryByRole("alertdialog", {
        name: "personalization.review.decisionConfirmTitle",
      }),
    ).toBeNull()
    expect(mocks.approvePersonalization).not.toHaveBeenCalled()
  })

  it("requires explicit confirmation before unlisting from the marketplace", async () => {
    const back = vi.fn()

    render(
      <PersonalizationReviewDetail
        personalizationId={personalizationId}
        view="PUBLISHED"
        back={back}
      />,
    )

    const unlistButton = await screen.findByText("personalization.review.unlist")
    fireEvent.click(unlistButton)

    expect(mocks.unlistPersonalization).not.toHaveBeenCalled()

    const confirmation = screen.getByRole("alertdialog", {
      name: "personalization.review.unlistConfirmTitle",
    })
    const confirmButton = screen.getByText("personalization.review.unlist")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirmButton)
    })

    fireEvent.click(confirmButton)

    await waitFor(() => {
      expect(mocks.unlistPersonalization).toHaveBeenCalledWith(personalizationId)
      expect(back).toHaveBeenCalledTimes(1)
    })

    expect(confirmation).toBeDefined()
  })

  it("dismisses marketplace unlisting confirmation with Escape", async () => {
    render(
      <PersonalizationReviewDetail
        personalizationId={personalizationId}
        view="PUBLISHED"
        back={vi.fn()}
      />,
    )

    fireEvent.click(await screen.findByText("personalization.review.unlist"))

    fireEvent.keyDown(
      screen.getByRole("alertdialog", {
        name: "personalization.review.unlistConfirmTitle",
      }),
      { key: "Escape" },
    )

    expect(
      screen.queryByRole("alertdialog", {
        name: "personalization.review.unlistConfirmTitle",
      }),
    ).toBeNull()
    expect(mocks.unlistPersonalization).not.toHaveBeenCalled()
  })

  it("requires explicit confirmation before relisting in the marketplace", async () => {
    mocks.listManagedPersonalizations.mockResolvedValue([
      {
        id: personalizationId,
        origin: "CREATOR",
        status: "UNLISTED",
        price: 1200,
      },
    ])
    const back = vi.fn()

    render(
      <PersonalizationReviewDetail
        personalizationId={personalizationId}
        view="UNLISTED"
        back={back}
      />,
    )

    fireEvent.click(await screen.findByText("personalization.review.relist"))

    expect(mocks.relistPersonalization).not.toHaveBeenCalled()

    const dialog = screen.getByRole("alertdialog", {
      name: "personalization.review.relistConfirmTitle",
    })
    const confirmButton = screen.getByText("personalization.review.relist")

    await waitFor(() => {
      expect(document.activeElement).toBe(confirmButton)
    })

    fireEvent.click(confirmButton)

    await waitFor(() => {
      expect(mocks.relistPersonalization).toHaveBeenCalledWith(personalizationId)
      expect(back).toHaveBeenCalledTimes(1)
    })

    expect(dialog).toBeDefined()
  })
})
