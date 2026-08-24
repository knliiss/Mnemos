const TELEGRAM_INIT_DATA_PARAMETER = "telegram_init_data"

export type TelegramLoginPostRequest = {
  action: string
  parameters: URLSearchParams
}

export function createTelegramLoginPostRequest(
  loginUrl: string,
  initData: string,
): TelegramLoginPostRequest {
  const url = new URL(loginUrl)
  const parameters = new URLSearchParams(url.search)

  parameters.set(TELEGRAM_INIT_DATA_PARAMETER, initData)

  url.search = ""
  url.hash = ""

  return {
    action: url.toString(),
    parameters,
  }
}

export function submitTelegramLoginRequest(loginUrl: string, initData: string): void {
  const request = createTelegramLoginPostRequest(loginUrl, initData)
  const form = document.createElement("form")

  form.method = "post"
  form.action = request.action
  form.style.display = "none"

  for (const [name, value] of request.parameters) {
    const input = document.createElement("input")

    input.type = "hidden"
    input.name = name
    input.value = value

    form.appendChild(input)
  }

  document.body.appendChild(form)
  form.submit()
}
