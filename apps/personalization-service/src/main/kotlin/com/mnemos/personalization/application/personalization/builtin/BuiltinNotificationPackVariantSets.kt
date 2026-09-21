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
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🚀",
                        "ИКСЫ",
                        "{player} выбил секретное оружие «{item}». Вот ради этого деп и делался.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🦄",
                        "Инвестиция ожила",
                        "У {player} появился секретный питомец «{item}». Актив буквально начал ходить за владельцем.",
                        ),
                        ),
                ),
            "notification.night-shift" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🗡️",
                        "Вот теперь проснулся",
                        "{player} выбил секретное оружие «{item}». Такой будильник уже не проигнорируешь.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🧸",
                        "Можно спать не одному",
                        "У {player} появился секретный питомец «{item}». Теперь хотя бы есть с кем досыпать.",
                        ),
                        ),
                ),
            "notification.suspicious-luck" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🗡️",
                        "Таблицу шансов сюда",
                        "{player} выбил секретное оружие «{item}». Это уже подозрительно.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐇",
                        "Любимчик RNG",
                        "Секретный питомец «{item}» достался {player}. Ну конечно.",
                        ),
                        ),
                ),
            "notification.hunter" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🗡️",
                        "Оружие достойно охотника",
                        "{player} нашёл секретное оружие «{item}». Теперь охота станет короче.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐺",
                        "Хищник признал своего",
                        "Секретный питомец «{item}» теперь идёт рядом с {player}.",
                        ),
                        ),
                ),
            "notification.critical" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "⚔️",
                        "OVERCRIT",
                        "{player} выбил секретное оружие «{item}». Цифры уже не помещаются.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐾",
                        "Критический компаньон",
                        "У {player} появился секретный питомец «{item}». Даже спутник прокнул крит.",
                        ),
                        ),
                ),
            "notification.night-city" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🗡️",
                        "Чёрный рынок вооружился",
                        "{player} получил секретное оружие «{item}». В открытом эфире об этом не говорят.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐈‍⬛",
                        "Ночной спутник",
                        "Секретный питомец «{item}» теперь следует за {player} по неоновым улицам.",
                        ),
                        ),
                ),
            "notification.guild" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "⚔️",
                        "Клинок мастера",
                        "{player} добыл секретное оружие «{item}». Такое достойно отдельной витрины.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐉",
                        "Новый фамильяр гильдии",
                        "Секретный питомец «{item}» теперь числится за {player} и всей гильдией.",
                        ),
                        ),
                ),
            "notification.dzhamshut" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🪚",
                        "Арматура элитная",
                        "{player} нашёл секретное оружие «{item}». На стройке пригодится всё.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐐",
                        "На объекте новый сотрудник",
                        "Секретный питомец «{item}» теперь работает рядом с {player}.",
                        ),
                        ),
                ),
            "notification.shbshnik" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🗡️",
                        "ШБ вооружилось",
                        "{player} достал секретное оружие «{item}». Теперь из-под стола лучше не выманивать.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐾",
                        "Под столом теперь двое",
                        "Секретный питомец «{item}» поселился рядом с {player}.",
                        ),
                        ),
                ),
            "notification.kaif" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🗡️",
                        "Красиво выпало",
                        "{player} выбил секретное оружие «{item}». Идеально легло в настроение.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐾",
                        "Кайф теперь бегает рядом",
                        "Секретный питомец «{item}» теперь сопровождает {player}.",
                        ),
                        ),
                ),
            "notification.gospodin" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "⚔️",
                        "Клинок для господина",
                        "Секретное оружие «{item}» теперь принадлежит господину {player}.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🦚",
                        "Достойный спутник",
                        "Секретный питомец «{item}» удостоился сопровождать господина {player}.",
                        ),
                        ),
                ),
            "notification.lavash" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🔪",
                        "Шеф положил нож в заказ",
                        "{player} получил секретное оружие «{item}». Осторожно, начинка острая.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐈",
                        "На кухне появился любимчик",
                        "Секретный питомец «{item}» теперь крутится рядом с {player}.",
                        ),
                        ),
                ),
            "notification.zaebalo" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🤬",
                        "И ради этого столько фарма",
                        "{player} выбил секретное оружие «{item}». Теперь можно злиться чуть тише.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🫠",
                        "Ещё и питомец, заебись",
                        "У {player} появился секретный питомец «{item}». Ладно, это хотя бы мило.",
                        ),
                        ),
                ),
            "notification.casino" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "💎",
                        "ДЖЕКПОТ",
                        "{player} сорвал секретное оружие «{item}». Заведение просит больше не приходить.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐇",
                        "Джекпот ещё и бегает",
                        "{player} выиграл секретного питомца «{item}». Крупье делает вид, что всё нормально.",
                        ),
                        ),
                ),
            "notification.support" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🔧",
                        "Критический hotfix",
                        "Секретное оружие «{item}» успешно доставлено {player}. Никто не знает почему.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐛",
                        "Баг стал фичей и питомцем",
                        "Секретный питомец «{item}» теперь прикреплён к {player}. Не трогаем, пока работает.",
                        ),
                        ),
                ),
            "notification.gossip" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🗡️",
                        "Говорят, у него ЭТО оружие",
                        "У {player} появилось секретное оружие «{item}». Через минуту об этом знает весь сервер.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐈",
                        "А ты видел его нового питомца?",
                        "Секретный питомец «{item}» появился у {player}. Только никому.",
                        ),
                        ),
                ),
            "notification.commentator" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "⚔️",
                        "ВОТ ЭТО ОРУЖИЕ!",
                        "{player} выбивает секретное оружие «{item}»! Трибуны встают!",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐎",
                        "НОВЫЙ ЛЮБИМЕЦ ПУБЛИКИ!",
                        "Секретный питомец «{item}» появляется у {player}! Великолепный момент!",
                        ),
                        ),
                ),
            "notification.deadline" to
                VariantSet(
                    itemDrop =
                        listOf(
                        item(
                        "secret-sword",
                        "SECRET",
                        "SWORD",
                        "🔥",
                        "Hotfix, который почему-то работает",
                        "{player} выбил секретное оружие «{item}». Главное — не спрашивать, как успели.",
                        ),
                        item(
                        "secret-pet",
                        "SECRET",
                        "PET",
                        "🐇",
                        "Даже питомец успел раньше нас",
                        "Секретный питомец «{item}» уже у {player}. Кто-то явно быстрее команды.",
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
