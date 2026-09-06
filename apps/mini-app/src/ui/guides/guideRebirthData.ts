export type GuideRebirthBoostStat =
  | "POWER"
  | "DAMAGE"
  | "LUCK"
  | "MONEY"
  | "ATTACK_SPEED"
  | "RELIC_EXP"

export type GuideRebirthStep = {
  rank: number
  nextRank: number
  location: number
  requirement: string
  growth: string
  boosts: Array<{ stat: GuideRebirthBoostStat; amount: string }>
}

export const GUIDE_REBIRTH_STEPS: GuideRebirthStep[] = [
  { rank: 1, nextRank: 2, location: 1, requirement: "75К", growth: "x1", boosts: [] },
  { rank: 2, nextRank: 3, location: 1, requirement: "2.5М", growth: "x3", boosts: [] },
  { rank: 3, nextRank: 4, location: 2, requirement: "87.5М", growth: "x2.6", boosts: [] },
  { rank: 4, nextRank: 5, location: 2, requirement: "3.23В", growth: "x2.8", boosts: [] },
  { rank: 5, nextRank: 6, location: 2, requirement: "126B", growth: "x2.6", boosts: [] },
  { rank: 6, nextRank: 7, location: 3, requirement: "5.2Т", growth: "x2", boosts: [] },
  { rank: 7, nextRank: 8, location: 3, requirement: "223Т", growth: "x2", boosts: [] },
  { rank: 8, nextRank: 9, location: 4, requirement: "10Qa", growth: "x2.1", boosts: [] },
  { rank: 9, nextRank: 10, location: 4, requirement: "500Qa", growth: "x2.5", boosts: [] },
  { rank: 10, nextRank: 11, location: 5, requirement: "24.5Qi", growth: "x2.2", boosts: [] },
  { rank: 11, nextRank: 12, location: 5, requirement: "1.24Sx", growth: "x2", boosts: [] },
  { rank: 12, nextRank: 13, location: 5, requirement: "65Sx", growth: "x2.3", boosts: [] },
  { rank: 13, nextRank: 14, location: 6, requirement: "2.7Sp", growth: "x2", boosts: [] },
  { rank: 14, nextRank: 15, location: 6, requirement: "68Sp", growth: "x4", boosts: [] },
  { rank: 15, nextRank: 16, location: 6, requirement: "8.69Oc", growth: "x2.4", boosts: [] },
  { rank: 16, nextRank: 17, location: 7, requirement: "522.99Oc", growth: "x3", boosts: [] },
  { rank: 17, nextRank: 18, location: 7, requirement: "31.36No", growth: "x2.2", boosts: [] },
  { rank: 18, nextRank: 19, location: 7, requirement: "2.02Dc", growth: "x2.2", boosts: [] },
  { rank: 19, nextRank: 20, location: 8, requirement: "137.9Dc", growth: "x2.15", boosts: [] },
  { rank: 20, nextRank: 21, location: 8, requirement: "9.86Ud", growth: "x2.46", boosts: [] },
  { rank: 21, nextRank: 22, location: 9, requirement: "700Ud", growth: "x2.3", boosts: [] },
  { rank: 22, nextRank: 23, location: 9, requirement: "57.2Dd", growth: "x2.33", boosts: [] },
  { rank: 23, nextRank: 24, location: 9, requirement: "4.84Td", growth: "x2.25", boosts: [] },
  { rank: 24, nextRank: 25, location: 9, requirement: "426.6Td", growth: "x3", boosts: [] },
  { rank: 25, nextRank: 26, location: 10, requirement: "33.88Qad", growth: "x8.8", boosts: [] },
  { rank: 26, nextRank: 27, location: 10, requirement: "3.23Qid", growth: "x2.9", boosts: [] },
  { rank: 27, nextRank: 28, location: 10, requirement: "359.1Qid", growth: "x3.1", boosts: [] },
  { rank: 28, nextRank: 29, location: 11, requirement: "37.04Sx", growth: "x3", boosts: [] },
  { rank: 29, nextRank: 30, location: 11, requirement: "3.88Spd", growth: "x3.25", boosts: [{ stat: "POWER", amount: "10" }] },
  { rank: 30, nextRank: 31, location: 11, requirement: "420.09Spd", growth: "x2.9", boosts: [{ stat: "POWER", amount: "20" }] },
  { rank: 31, nextRank: 32, location: 12, requirement: "66.63Ocd", growth: "x3", boosts: [{ stat: "MONEY", amount: "5" }, { stat: "POWER", amount: "30" }] },
  { rank: 32, nextRank: 33, location: 12, requirement: "5.31Nod", growth: "x3", boosts: [{ stat: "ATTACK_SPEED", amount: "10" }] },
  { rank: 33, nextRank: 34, location: 13, requirement: "616.7Nod", growth: "x3", boosts: [{ stat: "POWER", amount: "80" }] },
  { rank: 34, nextRank: 35, location: 13, requirement: "76.45Vg", growth: "x3", boosts: [{ stat: "POWER", amount: "80" }] },
  { rank: 35, nextRank: 36, location: 14, requirement: "10.09Uvg", growth: "x2.96", boosts: [{ stat: "LUCK", amount: "10" }] },
  { rank: 36, nextRank: 37, location: 14, requirement: "1.42Dvg", growth: "x3", boosts: [{ stat: "ATTACK_SPEED", amount: "20" }] },
  { rank: 37, nextRank: 38, location: 14, requirement: "214.99Dvg", growth: "x3", boosts: [{ stat: "POWER", amount: "120" }] },
  { rank: 38, nextRank: 39, location: 15, requirement: "34.29Tvg", growth: "x3", boosts: [{ stat: "DAMAGE", amount: "50" }, { stat: "POWER", amount: "135" }] },
  { rank: 39, nextRank: 40, location: 15, requirement: "6Qavg", growth: "x3.41", boosts: [{ stat: "MONEY", amount: "5" }, { stat: "POWER", amount: "50" }] },
  { rank: 40, nextRank: 41, location: 15, requirement: "761Qavg", growth: "x2.64", boosts: [{ stat: "LUCK", amount: "20" }, { stat: "POWER", amount: "60" }] },
  { rank: 41, nextRank: 42, location: 16, requirement: "37.21Qivg", growth: "x4.28", boosts: [{ stat: "RELIC_EXP", amount: "100" }, { stat: "POWER", amount: "70" }] },
  { rank: 42, nextRank: 43, location: 16, requirement: "2.66Sxvg", growth: "x3.17", boosts: [{ stat: "POWER", amount: "150" }] },
  { rank: 43, nextRank: 44, location: 16, requirement: "502.6Sxvg", growth: "x2.65", boosts: [{ stat: "DAMAGE", amount: "150" }, { stat: "POWER", amount: "90" }] },
  { rank: 44, nextRank: 45, location: 17, requirement: "124.99Spvg", growth: "x3.76", boosts: [{ stat: "POWER", amount: "200" }] },
  { rank: 45, nextRank: 46, location: 17, requirement: "25.66Ocvg", growth: "x2.12", boosts: [{ stat: "DAMAGE", amount: "150" }, { stat: "POWER", amount: "270" }] },
  { rank: 46, nextRank: 47, location: 18, requirement: "5.09Nov", growth: "x5", boosts: [{ stat: "DAMAGE", amount: "220" }, { stat: "POWER", amount: "320" }] },
  { rank: 47, nextRank: 48, location: 18, requirement: "1.2Tt", growth: "x3.38", boosts: [{ stat: "POWER", amount: "275" }] },
  { rank: 48, nextRank: 49, location: 19, requirement: "267.99Tt", growth: "x3.31", boosts: [{ stat: "MONEY", amount: "5" }, { stat: "POWER", amount: "200" }] },
  { rank: 49, nextRank: 50, location: 19, requirement: "50.8Unt", growth: "x3.28", boosts: [{ stat: "POWER", amount: "250" }] },
  { rank: 50, nextRank: 51, location: 20, requirement: "12.59Dtt", growth: "x2.39", boosts: [{ stat: "DAMAGE", amount: "250" }, { stat: "POWER", amount: "300" }] },
  { rank: 51, nextRank: 52, location: 20, requirement: "2.83Ttt", growth: "x2", boosts: [{ stat: "DAMAGE", amount: "200" }, { stat: "POWER", amount: "300" }] },
  { rank: 52, nextRank: 53, location: 21, requirement: "623.9Ttt", growth: "x4", boosts: [{ stat: "MONEY", amount: "5" }, { stat: "POWER", amount: "200" }] },
  { rank: 53, nextRank: 54, location: 21, requirement: "147.5Qut", growth: "x2.87", boosts: [{ stat: "DAMAGE", amount: "250" }, { stat: "POWER", amount: "350" }] },
  { rank: 54, nextRank: 55, location: 21, requirement: "30.03Qat", growth: "x2.73", boosts: [{ stat: "LUCK", amount: "10" }, { stat: "POWER", amount: "400" }] },
  { rank: 55, nextRank: 56, location: 22, requirement: "14.5Sst", growth: "x3.49", boosts: [{ stat: "MONEY", amount: "7.5" }, { stat: "DAMAGE", amount: "900" }, { stat: "POWER", amount: "800" }] },
  { rank: 56, nextRank: 57, location: 22, requirement: "1.56Spt", growth: "x3.56", boosts: [{ stat: "DAMAGE", amount: "140" }, { stat: "POWER", amount: "110" }] },
  { rank: 57, nextRank: 58, location: 23, requirement: "7Oct", growth: "x2.53", boosts: [{ stat: "DAMAGE", amount: "1К" }, { stat: "POWER", amount: "1.7К" }] },
  { rank: 58, nextRank: 59, location: 23, requirement: "1.54Nvt", growth: "x3.76", boosts: [{ stat: "DAMAGE", amount: "2.2К" }, { stat: "POWER", amount: "1.25К" }] },
  { rank: 59, nextRank: 60, location: 24, requirement: "341.8Nvt", growth: "x2.92", boosts: [{ stat: "POWER", amount: "250" }] },
  { rank: 60, nextRank: 61, location: 24, requirement: "74.95Qdt", growth: "x4.5", boosts: [{ stat: "POWER", amount: "250" }] },
  { rank: 61, nextRank: 62, location: 25, requirement: "1.68Qdt", growth: "x3.93", boosts: [{ stat: "POWER", amount: "250" }] },
  { rank: 62, nextRank: 63, location: 25, requirement: "363.81Qqt", growth: "x3.16", boosts: [{ stat: "POWER", amount: "250" }] },
  { rank: 63, nextRank: 64, location: 26, requirement: "82.92Sqt", growth: "x4.61", boosts: [{ stat: "POWER", amount: "250" }] },
  { rank: 64, nextRank: 65, location: 26, requirement: "1.7Sgt", growth: "x3.94", boosts: [{ stat: "POWER", amount: "250" }] },
  { rank: 65, nextRank: 66, location: 26, requirement: "345Sgt", growth: "x5.19", boosts: [{ stat: "POWER", amount: "250" }] },
  { rank: 66, nextRank: 67, location: 27, requirement: "84.5Oi", growth: "x1", boosts: [{ stat: "POWER", amount: "250" }] },
  { rank: 67, nextRank: 68, location: 27, requirement: "1.93Noi", growth: "x3.75", boosts: [{ stat: "DAMAGE", amount: "70К" }, { stat: "POWER", amount: "20.5К" }] },
]
