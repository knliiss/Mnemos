package com.mnemos.personalization.domain.personalization.model

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

data class NotificationTemplate(
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
    val variants: List<NotificationTemplateVariant> = emptyList(),
)

data class NotificationTemplateVariant(
    val id: String,
    val conditions: List<NotificationTemplateCondition>,
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
) {
    init {
        require(VARIANT_ID.matches(id)) {
            "Notification template variant id must contain only letters, numbers, dashes and underscores"
        }
        require(conditions.isNotEmpty()) {
            "Notification template variant must contain at least one condition"
        }
        require(conditions.map(NotificationTemplateCondition::field).distinct().size == conditions.size) {
            "Notification template variant cannot define the same condition field more than once"
        }
    }

    companion object {
        private val VARIANT_ID = Regex("^[a-zA-Z0-9_-]{1,48}$")
    }
}

data class NotificationTemplateCondition(
    val field: NotificationPackVariantField,
    val value: String,
) {
    init {
        require(value in field.allowedValues) {
            "Unsupported value $value for notification variant field $field"
        }
    }
}

data class NotificationValueForms(
    val default: String,
    val forms: Map<String, String> = emptyMap(),
) {
    init {
        requireValidValue(default, "default")
        require(forms.size <= 8) { "Notification value can define at most 8 named forms" }

        forms.forEach { (name, value) ->
            require(FORM_NAME.matches(name)) {
                "Notification value form name must contain only letters, numbers and underscores"
            }
            require(name != DEFAULT_FORM) {
                "Notification value form 'default' is reserved"
            }
            requireValidValue(value, name)
        }
    }

    fun resolve(form: String?): String? =
        if (form == null || form == DEFAULT_FORM) {
            default
        } else {
            forms[form]
        }

    private fun requireValidValue(
        value: String,
        description: String,
    ) {
        require(value.isNotBlank()) { "Notification value form $description cannot be blank" }
        require(value.length <= MAX_VALUE_LENGTH) { "Notification value form $description is too long" }
    }

    companion object {
        private const val DEFAULT_FORM = "default"
        private const val MAX_VALUE_LENGTH = 128
        private val FORM_NAME = Regex("^[a-zA-Z][a-zA-Z0-9_]{0,31}$")
    }
}

enum class NotificationTemplateKey {
    ITEM_DROP,
    BOOSTER,
    DUNGEON,
    RAID,
    GLOBAL,
    PING,
    PLAYER_OFFLINE,
    ;

    val contractType: NotificationPackTemplateType
        get() = NotificationPackTemplateType.valueOf(name)

    val allowedPlaceholders: Set<String>
        get() = contractType.allowedPlaceholders
}

fun NotificationPackValueKey.requireRawValue(rawValue: String) {
    require(rawValue in allowedValues) {
        "Unsupported notification value $rawValue for $this"
    }
}
