import type { InternalIdentity, ModerationSanction, WalletAdjustment } from "./model"
import { currentUser } from "./mockData"

export const internalIdentities: InternalIdentity[] = [
  {
    userId: "0198f6ce-e934-7000-8000-000000000021",
    username: "north",
    telegramId: 504_881_726,
    accounts: [
      {
        accountId: "0198f6ce-e934-7000-8000-000000000121",
        cristalixUuid: "9bcb0018-baa2-4dde-b360-10dc98c60418",
        cristalixNickname: "north",
        active: true,
      },
      {
        accountId: "0198f6ce-e934-7000-8000-000000000122",
        cristalixUuid: "a241edc0-bfc8-4a3d-9cf8-3e1d59fe9752",
        cristalixNickname: "north_old",
        active: false,
      },
    ],
  },
  {
    userId: "0198f6ce-e934-7000-8000-000000000022",
    username: "vex",
    telegramId: 718_406_392,
    accounts: [
      {
        accountId: "0198f6ce-e934-7000-8000-000000000131",
        cristalixUuid: "ad0c9864-7190-4e96-bc12-6a28f9ef0e7d",
        cristalixNickname: "vex",
        active: true,
      },
    ],
  },
  {
    userId: "0198f6ce-e934-7000-8000-000000000023",
    username: "mare",
    telegramId: 238_117_904,
    accounts: [
      {
        accountId: "0198f6ce-e934-7000-8000-000000000141",
        cristalixUuid: "f32198b8-39f7-4b21-8b7d-f6c348695e21",
        cristalixNickname: "mare",
        active: true,
      },
    ],
  },
]

export const sanctionsByUser: Record<string, ModerationSanction[]> = {
  "0198f6ce-e934-7000-8000-000000000021": [
    {
      id: "0198f6ce-e934-7000-8000-000000000201",
      userId: "0198f6ce-e934-7000-8000-000000000021",
      type: "NOTIFICATION_SUSPENSION",
      reason: "Спам сповіщеннями",
      internalNote: "Повторне порушення",
      createdByUserId: currentUser.id,
      createdAt: "17 серп. · 18:40",
      expiresAt: "18 серп. · 18:40",
      status: "ACTIVE",
    },
  ],
}

export const walletAdjustmentsByTarget: Record<string, WalletAdjustment[]> = {
  "0198f6ce-e934-7000-8000-000000000021": [
    {
      operationId: "0198f6ce-e934-7000-8000-000000000301",
      requestId: "0198f6ce-e934-7000-8000-000000000311",
      targetType: "USER",
      targetId: "0198f6ce-e934-7000-8000-000000000021",
      actorUserId: currentUser.id,
      currency: "PRIME",
      type: "CREDIT",
      amount: 25,
      reason: "Компенсація",
      status: "COMPLETED",
      walletTransactionId: "0198f6ce-e934-7000-8000-000000000321",
      createdAt: "16 серп. · 12:20",
    },
  ],
  "0198f6ce-e934-7000-8000-000000000121": [
    {
      operationId: "0198f6ce-e934-7000-8000-000000000302",
      requestId: "0198f6ce-e934-7000-8000-000000000312",
      targetType: "ACCOUNT",
      targetId: "0198f6ce-e934-7000-8000-000000000121",
      actorUserId: currentUser.id,
      currency: "COIN",
      type: "DEBIT",
      amount: 400,
      reason: "Коригування",
      status: "COMPLETED",
      walletTransactionId: "0198f6ce-e934-7000-8000-000000000322",
      createdAt: "15 серп. · 19:05",
    },
  ],
}
