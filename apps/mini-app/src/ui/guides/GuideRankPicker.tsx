import { useEffect, useMemo, useRef, useState } from "react"

type Props = {
  label: string
  value: number
  min: number
  max: number
  rankName: (rank: number) => string
  searchPlaceholder: string
  closeLabel: string
  onChange: (rank: number) => void
}

export function GuideRankPicker({
  label,
  value,
  min,
  max,
  rankName,
  searchPlaceholder,
  closeLabel,
  onChange,
}: Props) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState("")
  const searchRef = useRef<HTMLInputElement>(null)
  const triggerRef = useRef<HTMLButtonElement>(null)
  const ranks = useMemo(
    () => Array.from({ length: max - min + 1 }, (_, index) => min + index),
    [max, min],
  )
  const filteredRanks = useMemo(() => {
    const normalizedQuery = query.trim().toLocaleLowerCase()

    if (!normalizedQuery) {
      return ranks
    }

    return ranks.filter((rank) => {
      const name = rankName(rank).toLocaleLowerCase()

      return name.includes(normalizedQuery) || String(rank).includes(normalizedQuery)
    })
  }, [query, rankName, ranks])

  useEffect(() => {
    if (!open) {
      return
    }

    const timer = window.setTimeout(() => searchRef.current?.focus(), 0)
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setOpen(false)
        triggerRef.current?.focus()
      }
    }

    window.addEventListener("keydown", onKeyDown)

    return () => {
      window.clearTimeout(timer)
      window.removeEventListener("keydown", onKeyDown)
    }
  }, [open])

  const selectRank = (rank: number) => {
    onChange(rank)
    setQuery("")
    setOpen(false)
    window.setTimeout(() => triggerRef.current?.focus(), 0)
  }

  return (
    <div className="guide-rank-picker">
      <small>{label}</small>
      <button
        ref={triggerRef}
        type="button"
        className="guide-rank-picker__trigger"
        aria-haspopup="dialog"
        aria-expanded={open}
        onClick={() => setOpen(true)}
      >
        <span className="guide-rank-picker__number">#{value}</span>
        <span className="guide-rank-picker__name">{rankName(value)}</span>
        <span className="guide-rank-picker__chevron" aria-hidden="true" />
      </button>

      {open ? (
        <div
          className="guide-rank-picker__backdrop"
          onMouseDown={(event) => {
            if (event.currentTarget === event.target) {
              setOpen(false)
            }
          }}
        >
          <section
            className="guide-rank-picker__dialog"
            role="dialog"
            aria-modal="true"
            aria-label={label}
          >
            <header>
              <span>
                <small>{label}</small>
                <strong>
                  #{value} · {rankName(value)}
                </strong>
              </span>
              <button type="button" onClick={() => setOpen(false)} aria-label={closeLabel}>
                <span aria-hidden="true">×</span>
              </button>
            </header>

            <label className="guide-rank-picker__search">
              <span className="guides-search__mark" aria-hidden="true" />
              <input
                ref={searchRef}
                type="search"
                value={query}
                onInput={(event) => setQuery(event.currentTarget.value)}
                placeholder={searchPlaceholder}
                aria-label={searchPlaceholder}
                autoCapitalize="off"
                autoComplete="off"
                autoCorrect="off"
                spellCheck={false}
              />
            </label>

            <div className="guide-rank-picker__list" role="listbox" aria-label={label}>
              {filteredRanks.map((rank) => (
                <button
                  key={rank}
                  type="button"
                  role="option"
                  aria-selected={rank === value}
                  className={
                    rank === value
                      ? "guide-rank-picker__option is-selected"
                      : "guide-rank-picker__option"
                  }
                  onClick={() => selectRank(rank)}
                >
                  <span>#{rank}</span>
                  <strong>{rankName(rank)}</strong>
                </button>
              ))}
            </div>
          </section>
        </div>
      ) : null}
    </div>
  )
}
