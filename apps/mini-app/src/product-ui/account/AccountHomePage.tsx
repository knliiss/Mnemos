import { useEffect } from "react"
import { useProductData } from "../data/ProductDataProvider"
import type { Route } from "../model"
import { AccountHome } from "./AccountHome"

type Props = {
  navigate: (route: Route) => void
  back: () => void
  initialMemberId?: string | null
}

export function AccountHomePage({ navigate, back, initialMemberId }: Props) {
  const product = useProductData()
  const activeAccount = product.activeAccount

  useEffect(() => {
    if (!activeAccount || initialMemberId) {
      return
    }

    void product.refreshEvents({
      types: ["ITEM_DROP"],
      droppedFor: activeAccount.nickname,
      sort: "NEWEST",
      limit: 3,
    })
  }, [activeAccount, initialMemberId, product.refreshEvents])

  return <AccountHome navigate={navigate} back={back} initialMemberId={initialMemberId} />
}
