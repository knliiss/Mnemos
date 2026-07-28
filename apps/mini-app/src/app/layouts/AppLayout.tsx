import {Outlet} from "react-router-dom"
import {BottomNav} from "@/widgets/navigation/BottomNav"

export function AppLayout() {
    return <div className="app-shell">
        <main className="page"><Outlet/></main>
        <BottomNav/></div>
}
