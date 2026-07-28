import type {Account} from "@/shared/types/domain"

export function AccountName({account, compact = false}: { account: Account; compact?: boolean }) {
    return <div className={`account-name ${compact ? "compact" : ""}`}
                style={{color: account.personalization.nicknameColor}}>
        <span>{account.personalization.prefix}</span><strong>{account.cristalixNickname}</strong><span>{account.personalization.suffix}</span>{account.personalization.badge &&
        <em>{account.personalization.badge}</em>}</div>
}
