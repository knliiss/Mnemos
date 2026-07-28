import {Navigate, Route, Routes} from "react-router-dom"
import {useApp} from "@/app/providers/AppProvider"
import {AppLayout} from "@/app/layouts/AppLayout"
import {LoginPage} from "@/features/account-access/ui/LoginPage"
import {AccountPage} from "@/pages/account/AccountPage"
import {BetsPage} from "@/pages/bets/BetsPage"
import {CommentsPage} from "@/pages/comments/CommentsPage"
import {AdminPage, NewsPage, NotificationsPage, PersonalizationPage, ProposalsPage} from "@/pages/content/ContentPages"
import {HistoryPage} from "@/pages/history/HistoryPage"
import {HomePage} from "@/pages/home/HomePage"
import {SearchPage} from "@/pages/search/SearchPage"
import {SettingsPage} from "@/pages/settings/SettingsPage"
import {LoadingPage} from "@/pages/loading/LoadingPage"

export function AppRouter() {
    const {loading, account} = useApp()
    if (loading) return <LoadingPage label="Mnemos"/>
    return <Routes>
        <Route path="/login" element={account ? <Navigate to="/" replace/> : <LoginPage/>}/>
        <Route element={account ? <AppLayout/> : <Navigate to="/login" replace/>}>
            <Route index element={<HomePage/>}/><Route path="history" element={<HistoryPage/>}/><Route path="search" element={<SearchPage/>}/><Route path="account" element={<AccountPage/>}/><Route path="settings" element={<SettingsPage/>}/>
        </Route>
        <Route path="account/:id" element={<AccountPage/>}/><Route path="comments/:id?" element={<CommentsPage/>}/><Route path="bets" element={<BetsPage/>}/><Route path="personalization" element={<PersonalizationPage/>}/><Route path="news" element={<NewsPage/>}/><Route path="proposals" element={<ProposalsPage/>}/><Route path="notifications" element={<NotificationsPage/>}/><Route path="admin" element={<AdminPage/>}/><Route path="*" element={<Navigate to="/" replace/>}/>
    </Routes>
}
