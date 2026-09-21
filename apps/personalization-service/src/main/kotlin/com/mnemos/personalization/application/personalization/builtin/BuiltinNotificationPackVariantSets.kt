package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.condition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.variant
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateVariant

object BuiltinNotificationPackVariantSets {
    data class VariantSet(
        val itemDrop: List<NotificationTemplateVariant>,
    )

    fun forPack(catalogKey: String): VariantSet? = sets[catalogKey]

    private val sets: Map<String, VariantSet> =
        mapOf(
            "notification.dep" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🚀",
                                title = "ИКСЫ",
                                body = "{player} выбил секретное оружие «{item}». Вот ради этого деп и делался.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🦄",
                                title = "Инвестиция ожила",
                                body = "У {player} появился секретный питомец «{item}». Актив буквально начал ходить за владельцем.",
                            ),
                        ),
                ),
            "notification.night-shift" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🗡️",
                                title = "Вот теперь проснулся",
                                body = "{player} выбил секретное оружие «{item}». Такой будильник уже не проигнорируешь.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🧸",
                                title = "Можно спать не одному",
                                body = "У {player} появился секретный питомец «{item}». Теперь хотя бы есть с кем досыпать.",
                            ),
                        ),
                ),
            "notification.suspicious-luck" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🗡️",
                                title = "Таблицу шансов сюда",
                                body = "{player} выбил секретное оружие «{item}». Это уже подозрительно.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐇",
                                title = "Любимчик RNG",
                                body = "Секретный питомец «{item}» достался {player}. Ну конечно.",
                            ),
                        ),
                ),
            "notification.hunter" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🗡️",
                                title = "Оружие достойно охотника",
                                body = "{player} нашёл секретное оружие «{item}». Теперь охота станет короче.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐺",
                                title = "Хищник признал своего",
                                body = "Секретный питомец «{item}» теперь идёт рядом с {player}.",
                            ),
                        ),
                ),
            "notification.critical" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "⚔️",
                                title = "OVERCRIT",
                                body = "{player} выбил секретное оружие «{item}». Цифры уже не помещаются.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐾",
                                title = "Критический компаньон",
                                body = "У {player} появился секретный питомец «{item}». Даже спутник прокнул крит.",
                            ),
                        ),
                ),
            "notification.night-city" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🗡️",
                                title = "Чёрный рынок вооружился",
                                body = "{player} получил секретное оружие «{item}». В открытом эфире об этом не говорят.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐈‍⬛",
                                title = "Ночной спутник",
                                body = "Секретный питомец «{item}» теперь следует за {player} по неоновым улицам.",
                            ),
                        ),
                ),
            "notification.guild" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "⚔️",
                                title = "Клинок мастера",
                                body = "{player} добыл секретное оружие «{item}». Такое достойно отдельной витрины.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐉",
                                title = "Новый фамильяр гильдии",
                                body = "Секретный питомец «{item}» теперь числится за {player} и всей гильдией.",
                            ),
                        ),
                ),
            "notification.dzhamshut" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🪚",
                                title = "Арматура элитная",
                                body = "{player} нашёл секретное оружие «{item}». На стройке пригодится всё.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐐",
                                title = "На объекте новый сотрудник",
                                body = "Секретный питомец «{item}» теперь работает рядом с {player}.",
                            ),
                        ),
                ),
            "notification.shbshnik" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🗡️",
                                title = "ШБ вооружилось",
                                body = "{player} достал секретное оружие «{item}». Теперь из-под стола лучше не выманивать.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐾",
                                title = "Под столом теперь двое",
                                body = "Секретный питомец «{item}» поселился рядом с {player}.",
                            ),
                        ),
                ),
            "notification.kaif" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🗡️",
                                title = "Красиво выпало",
                                body = "{player} выбил секретное оружие «{item}». Идеально легло в настроение.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐾",
                                title = "Кайф теперь бегает рядом",
                                body = "Секретный питомец «{item}» теперь сопровождает {player}.",
                            ),
                        ),
                ),
            "notification.gospodin" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "⚔️",
                                title = "Клинок для господина",
                                body = "Секретное оружие «{item}» теперь принадлежит господину {player}.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🦚",
                                title = "Достойный спутник",
                                body = "Секретный питомец «{item}» удостоился сопровождать господина {player}.",
                            ),
                        ),
                ),
            "notification.lavash" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🔪",
                                title = "Шеф положил нож в заказ",
                                body = "{player} получил секретное оружие «{item}». Осторожно, начинка острая.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐈",
                                title = "На кухне появился любимчик",
                                body = "Секретный питомец «{item}» теперь крутится рядом с {player}.",
                            ),
                        ),
                ),
            "notification.zaebalo" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🤬",
                                title = "И ради этого столько фарма",
                                body = "{player} выбил секретное оружие «{item}». Теперь можно злиться чуть тише.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🫠",
                                title = "Ещё и питомец, заебись",
                                body = "У {player} появился секретный питомец «{item}». Ладно, это хотя бы мило.",
                            ),
                        ),
                ),
            "notification.casino" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "💎",
                                title = "ДЖЕКПОТ",
                                body = "{player} сорвал секретное оружие «{item}». Заведение просит больше не приходить.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐇",
                                title = "Джекпот ещё и бегает",
                                body = "{player} выиграл секретного питомца «{item}». Крупье делает вид, что всё нормально.",
                            ),
                        ),
                ),
            "notification.support" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🔧",
                                title = "Критический hotfix",
                                body = "Секретное оружие «{item}» успешно доставлено {player}. Никто не знает почему.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐛",
                                title = "Баг стал фичей и питомцем",
                                body = "Секретный питомец «{item}» теперь прикреплён к {player}. Не трогаем, пока работает.",
                            ),
                        ),
                ),
            "notification.gossip" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🗡️",
                                title = "Говорят, у него ЭТО оружие",
                                body = "У {player} появилось секретное оружие «{item}». Через минуту об этом знает весь сервер.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐈",
                                title = "А ты видел его нового питомца?",
                                body = "Секретный питомец «{item}» появился у {player}. Только никому.",
                            ),
                        ),
                ),
            "notification.commentator" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "⚔️",
                                title = "ВОТ ЭТО ОРУЖИЕ!",
                                body = "{player} выбивает секретное оружие «{item}»! Трибуны встают!",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐎",
                                title = "НОВЫЙ ЛЮБИМЕЦ ПУБЛИКИ!",
                                body = "Секретный питомец «{item}» появляется у {player}! Великолепный момент!",
                            ),
                        ),
                ),
            "notification.deadline" to
                VariantSet(
                    itemDrop =
                        listOf(
                            item(
                                id = "secret-sword",
                                rarity = "SECRET",
                                itemType = "SWORD",
                                emoji = "🔥",
                                title = "Hotfix, который почему-то работает",
                                body = "{player} выбил секретное оружие «{item}». Главное — не спрашивать, как успели.",
                            ),
                            item(
                                id = "secret-pet",
                                rarity = "SECRET",
                                itemType = "PET",
                                emoji = "🐇",
                                title = "Даже питомец успел раньше нас",
                                body = "Секретный питомец «{item}» уже у {player}. Кто-то явно быстрее команды.",
                            ),
                        ),
                )
        )

    private fun item(
        id: String,
        rarity: String,
        itemType: String?,
        emoji: String,
        title: String,
        body: String,
    ): NotificationTemplateVariant =
        variant(
            id = id,
            conditions =
                buildList {
                    add(condition(NotificationPackVariantField.ITEM_RARITY, rarity))
                    itemType?.let { add(condition(NotificationPackVariantField.ITEM_TYPE, it)) }
                },
            emoji = emoji,
            title = title,
            body = body,
            compact = title,
            aggregateTitle = title,
        )
}
