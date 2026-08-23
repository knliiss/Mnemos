const TELEGRAM_INIT_DATA_FRAGMENT_PARAMETER = "telegram_init_data"

export function createTelegramLoginRedirectUrl(loginUrl: string, initData: string): string {
  const url = new URL(loginUrl)
  const fragmentParameters = new URLSearchParams()

  fragmentParameters.set(TELEGRAM_INIT_DATA_FRAGMENT_PARAMETER, initData)
  url.hash = fragmentParameters.toString()

  return url.toString()
}
