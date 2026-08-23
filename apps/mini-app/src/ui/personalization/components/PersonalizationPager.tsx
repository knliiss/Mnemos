import { useTranslation } from "react-i18next"

type PersonalizationPagerProps = {
  page: number
  pageCount: number
  onChange: (page: number) => void
}

export function PersonalizationPager({ page, pageCount, onChange }: PersonalizationPagerProps) {
  const { t } = useTranslation()

  return (
    <nav className="catalog-pager" aria-label={t("personalization.pages")}>
      <button type="button" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        ‹
      </button>
      <span>
        {page + 1} / {pageCount}
      </span>
      <button type="button" disabled={page >= pageCount - 1} onClick={() => onChange(page + 1)}>
        ›
      </button>
    </nav>
  )
}
