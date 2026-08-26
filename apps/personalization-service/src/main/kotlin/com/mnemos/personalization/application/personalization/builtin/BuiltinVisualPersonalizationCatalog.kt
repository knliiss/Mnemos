package com.mnemos.personalization.application.personalization.builtin

object BuiltinVisualPersonalizationCatalog {
    val definitions: List<BuiltinPersonalizationDefinition> =
        buildList {
            addAll(BuiltinNickStyles.definitions)
            addAll(BuiltinProfileThemes.definitions)
            addAll(BuiltinAvatarFrames.definitions)
            addAll(BuiltinProfileEffects.definitions)
        }
}
