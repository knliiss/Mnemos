package com.mnemos.personalization.application.personalization.builtin

object BuiltinPersonalizationCatalog {
    val definitions: List<BuiltinPersonalizationDefinition> =
        buildList {
            addAll(BuiltinNotificationPackCatalog.definitions)
            addAll(BuiltinVisualPersonalizationCatalog.definitions)
        }
}
