const PET_TEXTURE_BASE_URL = "https://rustorage.c7x.dev/butowka/sao/global/img/pet"
const PET_ITEM_ID = /^(?:pet|petd)_l\d+_\d+$/

export function resolveSaoPetInventoryTextureUrl(localizationKey?: string): string | null {
  if (!localizationKey?.startsWith("sao.item.") || !localizationKey.endsWith(".name")) {
    return null
  }

  const itemId = localizationKey.slice("sao.item.".length, -".name".length)

  if (!PET_ITEM_ID.test(itemId)) {
    return null
  }

  return `${PET_TEXTURE_BASE_URL}/${itemId}.png`
}
