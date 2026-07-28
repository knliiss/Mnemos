import {useState} from "react"
import {ChevronDown, Link2, Plus, UserRound} from "lucide-react"
import {useTranslation} from "react-i18next"
import {useNavigate} from "react-router-dom"
import {MinecraftHead} from "@/entities/account/ui/MinecraftHead"
import {Segmented} from "@/shared/ui/Segmented"
import {useApp} from "@/app/providers/AppProvider"
import {executeAction} from "@/shared/errors/errorHandler"
import {notifications} from "@/shared/notifications/notificationCenter"

export function LoginPage() {
    const {t} = useTranslation()
    const navigate = useNavigate()
    const {availableAccounts, selectAccount, registerAccount} = useApp()
    const [tab, setTab] = useState<"create" | "invite">("create")
    const [nickname, setNickname] = useState("")
    const [invite, setInvite] = useState("")
    const [open, setOpen] = useState(true)
    const [busy, setBusy] = useState(false)

    async function activate(id: string) {
        setBusy(true)
        await executeAction(async () => {
            await selectAccount(id)
            navigate("/")
        })
        setBusy(false)
    }

    async function create() {
        const cristalixNickname = nickname.trim()
        if (!cristalixNickname) {
            notifications.info("Введите ник аккаунта")
            return
        }
        setBusy(true)
        await executeAction(async () => {
            await registerAccount(cristalixNickname)
            navigate("/")
        })
        setBusy(false)
    }

    return <div className="login-page">
        <div className="login-brand">
            <div className="brand-mark">M</div>
            <h1>Mnemos</h1><p>{t("account.noActive")}</p></div>
        <section className="panel account-picker">
            <button className="picker-title" onClick={() => setOpen(value => !value)}>
                <span>{t("account.available")}</span><ChevronDown size={18} className={open ? "rotated" : ""}/></button>
            {open && <div className="available-list">{availableAccounts.map(account => <button key={account.id}
                                                                                               disabled={busy}
                                                                                               onClick={() => activate(account.id)}>
                <MinecraftHead
                    size={40}/><span><strong>{account.cristalixNickname}</strong><small>{account.cristalixUuid}</small></span><UserRound
                size={17}/></button>)}</div>}
        </section>
        <section className="panel login-form">
            <Segmented<"create" | "invite"> value={tab} onChange={setTab}
                                            items={[{value: "create", label: t("account.create")}, {
                                                value: "invite",
                                                label: t("account.invite")
                                            }]}/>
            {tab === "create" ? <><label>{t("account.nickname")}<input value={nickname}
                                                                       onChange={event => setNickname(event.target.value)}
                                                                       placeholder="Knalis"/></label>
                <button className="primary-button" disabled={busy || !nickname.trim()} onClick={() => create()}>
                    <Plus size={17}/>{t("account.createAccount")}</button>
            </> : <><label>{t("account.inviteCode")}<input value={invite}
                                                           onChange={event => setInvite(event.target.value)}
                                                           placeholder="00000000-0000-0000-0000-000000000000"/></label>
                <button className="primary-button" disabled={!invite.trim()}
                        onClick={() => console.info("Invite account join", invite)}><Link2
                    size={17}/>{t("account.connect")}</button>
            </>}
        </section>
    </div>
}

