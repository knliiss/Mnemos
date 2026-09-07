import { Glyph } from "../shared/MnemosIcon"

type Props = {
  value: string
  placeholder: string
  clearLabel: string
  onChange: (value: string) => void
}

export function GuideSearch({ value, placeholder, clearLabel, onChange }: Props) {
  return (
    <label className="guides-search guides-search--app-pattern">
      <Glyph name="search" size={18} />
      <input
        type="search"
        value={value}
        onInput={(event) => onChange(event.currentTarget.value)}
        placeholder={placeholder}
        aria-label={placeholder}
        autoCapitalize="off"
        autoComplete="off"
        autoCorrect="off"
        spellCheck={false}
        enterKeyHint="search"
      />
      {value ? (
        <button type="button" onClick={() => onChange("")} aria-label={clearLabel}>
          <Glyph name="close" size={14} />
        </button>
      ) : null}
    </label>
  )
}
