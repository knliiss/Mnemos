export type BootstrapLanguage = "en" | "ru" | "uk"

const DEFAULT_BOOTSTRAP_LANGUAGE: BootstrapLanguage = "ru"

export function resolveBootstrapLanguage(): BootstrapLanguage {
  return DEFAULT_BOOTSTRAP_LANGUAGE
}
