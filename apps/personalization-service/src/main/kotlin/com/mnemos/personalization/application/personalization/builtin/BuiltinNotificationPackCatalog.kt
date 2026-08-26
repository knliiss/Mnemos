package com.mnemos.personalization.application.personalization.builtin

object BuiltinNotificationPackCatalog {
    val definitions: List<BuiltinPersonalizationDefinition> =
        buildList {
            addAll(BuiltinNotificationPacksCore.definitions)
            addAll(BuiltinNotificationPacksCulture.definitions)
            addAll(BuiltinNotificationPacksAtmosphere.definitions)
            addAll(BuiltinNotificationPacksThemes.definitions)
        }
}
