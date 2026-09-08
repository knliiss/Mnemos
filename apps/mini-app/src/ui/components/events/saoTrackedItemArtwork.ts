export type SaoItemAtlas = {
  path: string
  columns: number
  rows: number
}

export type SaoItemAtlasSprite = {
  column: number
  row: number
  atlas: SaoItemAtlas
}

export const SAO_ITEM_ATLAS_PATH = "/event-icons/sao-tracked-items.webp"
export const SAO_ITEM_ATLAS_COLUMNS = 16
export const SAO_ITEM_ATLAS_ROWS = 11
export const SAO_MODEL_ITEM_ATLAS_PATH = "/event-icons/sao-tracked-item-models-inventory-v2.webp"
export const SAO_MODEL_ITEM_ATLAS_COLUMNS = 8
export const SAO_MODEL_ITEM_ATLAS_ROWS = 8
export const SAO_AURA_ITEM_ATLAS_PATH = "/guides/entities/aura-inventory-atlas.webp"
export const SAO_AURA_ITEM_ATLAS_COLUMNS = 8
export const SAO_AURA_ITEM_ATLAS_ROWS = 5

const TRACKED_ITEM_ATLAS: SaoItemAtlas = {
  path: SAO_ITEM_ATLAS_PATH,
  columns: SAO_ITEM_ATLAS_COLUMNS,
  rows: SAO_ITEM_ATLAS_ROWS,
}

const MODEL_ITEM_ATLAS: SaoItemAtlas = {
  path: SAO_MODEL_ITEM_ATLAS_PATH,
  columns: SAO_MODEL_ITEM_ATLAS_COLUMNS,
  rows: SAO_MODEL_ITEM_ATLAS_ROWS,
}

const AURA_ITEM_ATLAS: SaoItemAtlas = {
  path: SAO_AURA_ITEM_ATLAS_PATH,
  columns: SAO_AURA_ITEM_ATLAS_COLUMNS,
  rows: SAO_AURA_ITEM_ATLAS_ROWS,
}

const AURA_ITEM_ATLAS_INDEX: Record<string, number> = {
  auraAwareness: 0,
  auraBlack: 1,
  auraBone: 2,
  auraChristmas: 3,
  auraConfrontation: 4,
  auraDeep: 5,
  auraDefence: 6,
  auraDragon: 7,
  auraEath: 8,
  auraExhaustion: 9,
  auraFire: 10,
  auraFlame: 11,
  auraForest: 12,
  auraFury: 13,
  auraGoodness: 14,
  auraHumility: 15,
  auraIce: 16,
  auraIceBreathe: 17,
  auraKnowledge: 18,
  auraLava: 19,
  auraLeaf: 20,
  auraLight: 21,
  auraLightning: 22,
  auraMagic: 23,
  auraNature: 24,
  auraPumpkin: 25,
  auraRebirth: 26,
  auraScience: 27,
  auraSnowflake: 28,
  auraSpace: 29,
  auraSword: 30,
  auraTools: 31,
  auraVampirism: 32,
  auraVoodoo: 33,
  auraWater: 34,
}

const ITEM_ATLAS_INDEX: Record<string, number> = {
  auraBone: 0,
  auraHumility: 1,
  auraSnowflake: 2,
  auraSpace: 3,
  auraSword: 4,
  auraTools: 5,
  book_improvement: 6,
  book_l13_5: 7,
  book_l14_5: 8,
  book_l15_6: 9,
  book_l16_6: 10,
  book_l16_7: 11,
  book_l17_5: 12,
  book_l17_6: 13,
  book_l17_7: 14,
  book_l18_5: 15,
  book_l18_6: 16,
  book_l19_5: 17,
  book_l19_6: 18,
  book_l20_5: 19,
  book_l20_6: 20,
  book_l21_5: 21,
  book_l21_6: 22,
  book_l22_5: 23,
  book_l22_6: 24,
  book_l23_5: 25,
  book_l23_6: 26,
  book_l24_5: 27,
  book_l24_6: 28,
  book_l25_5: 29,
  book_l25_6: 30,
  book_l26_5: 31,
  book_l26_6: 32,
  book_l27_5: 33,
  book_l27_6: 34,
  book_l28_5: 35,
  book_l28_6: 36,
  book_l29_5: 37,
  book_l29_6: 38,
  book_l30_5: 39,
  book_l30_6: 40,
  book_l31_5: 41,
  book_l31_6: 42,
  book_l32_5: 43,
  book_l32_6: 44,
  book_p_8: 45,
  book_p_9: 46,
  d1_284: 47,
  d1_324: 48,
  d1_404: 49,
  d1_444: 50,
  d1_484: 51,
  d1_503: 52,
  d1_504: 53,
  d2_124: 54,
  d2_164: 55,
  d2_204: 56,
  d2_244: 57,
  d2_284: 58,
  d2_324: 59,
  d2_364: 60,
  d2_404: 61,
  d2_44: 62,
  d2_444: 63,
  d2_484: 64,
  d2_504: 65,
  d2_84: 66,
  d3_124: 67,
  d3_164: 68,
  d3_244: 69,
  d3_284: 70,
  d3_324: 71,
  d3_364: 72,
  d3_404: 73,
  d3_44: 74,
  d3_444: 75,
  d3_484: 76,
  d3_84: 77,
  d4_124: 78,
  d4_164: 79,
  d4_204: 80,
  d4_244: 81,
  d4_284: 82,
  d4_324: 83,
  d4_364: 84,
  d4_404: 85,
  d4_44: 86,
  d4_444: 87,
  d4_84: 88,
  l10_10: 89,
  l10_11: 90,
  l10_12: 91,
  l11_10: 92,
  l11_11: 93,
  l11_12: 94,
  l12_10: 95,
  l12_11: 96,
  l12_12: 97,
  l13_10: 98,
  l13_11: 99,
  l13_12: 100,
  l14_10: 101,
  l14_11: 102,
  l14_12: 103,
  l15_10: 104,
  l15_11: 105,
  l15_12: 106,
  l16_10: 107,
  l16_11: 108,
  l16_12: 109,
  l17_10: 110,
  l17_11: 111,
  l17_12: 112,
  l18_10: 113,
  l18_11: 114,
  l18_12: 115,
  l19_10: 116,
  l19_11: 117,
  l19_12: 118,
  l1_8: 119,
  l1_9: 120,
  l20_10: 121,
  l20_11: 122,
  l20_12: 123,
  l21_10: 124,
  l21_11: 125,
  l21_12: 126,
  l22_10: 127,
  l22_11: 128,
  l22_12: 129,
  l23_10: 130,
  l23_11: 131,
  l23_12: 132,
  l24_10: 133,
  l24_11: 134,
  l24_12: 135,
  l25_10: 136,
  l25_11: 137,
  l25_12: 138,
  l26_10: 139,
  l26_11: 140,
  l26_12: 141,
  l27_10: 142,
  l27_11: 143,
  l27_12: 144,
  l28_10: 145,
  l28_11: 146,
  l28_12: 147,
  l2_10: 148,
  l2_9: 149,
  l3_10: 150,
  l3_9: 151,
  l4_10: 152,
  l4_9: 153,
  l5_10: 154,
  l5_9: 155,
  l6_10: 156,
  l6_9: 157,
  l7_10: 158,
  l7_11: 159,
  l8_10: 160,
  l8_11: 161,
  l9_10: 162,
  l9_11: 163,
  l9_12: 164,
  d3_504: 165,
}

const MODEL_ITEM_ATLAS_INDEX: Record<string, number> = {
  l10_11: 0,
  l10_12: 1,
  l11_11: 2,
  l11_12: 3,
  l12_11: 4,
  l12_12: 5,
  l13_11: 6,
  l13_12: 7,
  l14_11: 8,
  l14_12: 9,
  l15_11: 10,
  l15_12: 11,
  l16_11: 12,
  l16_12: 13,
  l17_11: 14,
  l17_12: 15,
  l18_11: 16,
  l18_12: 17,
  l19_11: 18,
  l19_12: 19,
  l1_8: 20,
  l1_9: 21,
  l20_11: 22,
  l20_12: 23,
  l21_10: 24,
  l21_11: 25,
  l21_12: 26,
  l22_10: 27,
  l22_11: 28,
  l22_12: 29,
  l23_10: 30,
  l23_11: 31,
  l23_12: 32,
  l24_10: 33,
  l24_11: 34,
  l24_12: 35,
  l25_10: 36,
  l25_11: 37,
  l25_12: 38,
  l26_10: 39,
  l26_11: 40,
  l26_12: 41,
  l27_10: 42,
  l27_11: 43,
  l27_12: 44,
  l28_10: 45,
  l28_11: 46,
  l28_12: 47,
  l2_10: 48,
  l3_10: 49,
  l4_10: 50,
  l5_10: 51,
  l6_10: 52,
  l7_10: 53,
  l7_11: 54,
  l8_11: 55,
  l9_10: 56,
  l9_11: 57,
  l9_12: 58,
}

export function resolveSaoItemAtlasSprite(localizationKey?: string): SaoItemAtlasSprite | null {
  if (!localizationKey?.startsWith("sao.item.") || !localizationKey.endsWith(".name")) {
    return null
  }

  const itemId = localizationKey.slice("sao.item.".length, -".name".length)
  const auraIndex = AURA_ITEM_ATLAS_INDEX[itemId]

  if (auraIndex !== undefined) {
    return toSprite(auraIndex, AURA_ITEM_ATLAS)
  }

  const modelIndex = MODEL_ITEM_ATLAS_INDEX[itemId]

  if (modelIndex !== undefined) {
    return toSprite(modelIndex, MODEL_ITEM_ATLAS)
  }

  const index = ITEM_ATLAS_INDEX[itemId]

  if (index === undefined) {
    return null
  }

  return toSprite(index, TRACKED_ITEM_ATLAS)
}

function toSprite(index: number, atlas: SaoItemAtlas): SaoItemAtlasSprite {
  return {
    column: index % atlas.columns,
    row: Math.floor(index / atlas.columns),
    atlas,
  }
}
