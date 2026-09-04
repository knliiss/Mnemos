type Tab = {
  id: string
  label: string
  badge?: number | string
}

type Props = {
  tabs: Tab[]
  activeTab: string
  ariaLabel: string
  onChange(tabId: string): void
}

export function ModerationDetailTabs({ tabs, activeTab, ariaLabel, onChange }: Props) {
  return (
    <nav className="moderation-detail-tabs" role="tablist" aria-label={ariaLabel}>
      {tabs.map((tab) => (
        <button
          key={tab.id}
          type="button"
          role="tab"
          aria-selected={activeTab === tab.id}
          className={activeTab === tab.id ? "is-active" : ""}
          onClick={() => onChange(tab.id)}
        >
          <span>{tab.label}</span>
          {tab.badge !== undefined ? <em>{tab.badge}</em> : null}
        </button>
      ))}
    </nav>
  )
}
